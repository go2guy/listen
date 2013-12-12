package com.interact.listen.pbx

import org.apache.log4j.Logger

class ExtensionService {
    def cloudToDeviceService
    def historyService
    def ldapService
    def messageLightService
    def spotCommunicationService
    def springSecurityService

    Extension create(def params, def checkPermission = true) {
        log.debug "Attempt to create extension [${params}]"
        def user = springSecurityService.getCurrentUser()
        if (user)
            log.debug "We got the current user [${user.username}]"
        else
            log.debug "We dont have a current user"
        if(checkPermission && !user.hasRole('ROLE_ORGANIZATION_ADMIN')) {
            // TODO better exception type
            log.warn "This action is not allowed"
            throw new AssertionError('Action not allowed')
        }

        // This tokenization was added because NewNet put XXX-XXX-XXXX xEXT in the AD
        def tokenList = params.number.tokenize('x')
        if (tokenList.size() > 1) {
            params.number = tokenList[1]
            log.debug "Setting extension number to [${params.number}]"
        } else {
            log.debug "Using extension number provided [${params.number}]"
        }
        
        log.debug "Now actually create the extension for [${params}]"
        def extension = new Extension(params)

        if(extension.validate() && extension.save()) {
            log.debug "We've create a new extension now cloud to device"
            cloudToDeviceService.sendContactSync()
            log.debug "We've create a new extension now message light"
            messageLightService.toggle(extension)
            log.debug "We've create a new extension now add extension to ldap"
            ldapService.addExtension(extension.owner, extension.number)
            log.debug "We've create a new extension now add history service"
            historyService.createdExtension(extension)
            log.debug "finished history service"
        } else {
            log.error "Failed to create extension due to errors [${extension.errors}]"
        }

        log.debug "Successfully created extension [${extension}]"
        return extension
    }

    void delete(Extension extension) {
        def user = springSecurityService.getCurrentUser()
        if(!user.hasRole('ROLE_ORGANIZATION_ADMIN')) {
            // TODO better exception type
            throw new AssertionError('Action not allowed')
        }

        extension.delete()
        
        historyService.deletedExtension(extension)
        ldapService.removeExtension(extension.owner, extension.number)
        cloudToDeviceService.sendContactSync()
        messageLightService.toggle(extension.number, extension.ip, false)
    }

    Extension update(Extension extension, def params) {
        def user = springSecurityService.getCurrentUser()

        def originalNumber = extension.number
        def originalIp = extension.ip
        def originalForwardedTo = extension.forwardedTo
        def originalOwner = extension.owner

        if(user.hasRole('ROLE_ORGANIZATION_ADMIN')) {
            extension.properties = params
        } else {
            extension.properties['forwardedTo', 'greeting'] = params
        }

        if(extension.validate() && extension.save()) {
            cloudToDeviceService.sendContactSync()

            if(originalNumber != extension.number) {
                ldapService.changeExtension(extension.owner, originalNumber, extension.number)
            }

            if(originalNumber != extension.number || originalIp != extension.ip) {
                messageLightService.toggle(originalNumber, originalIp, false)
                messageLightService.toggle(extension)
            }

            if(originalNumber != extension.number || originalOwner != extension.owner) {
                def fake = new Expando(number: originalNumber,
                                       owner: originalOwner)
                historyService.deletedExtension(fake)
                historyService.createdExtension(extension)
            }

            if(originalIp != extension.ip) {
                historyService.changedExtensionIpAddress(extension, originalIp)
            }

            if(originalForwardedTo != extension.forwardedTo) {
                if(extension.forwardedTo != null) {
                    historyService.forwardedExtension(extension)
                } else {
                    historyService.unforwardedExtension(extension)
                }
            }
        }

        return extension
    }
}
