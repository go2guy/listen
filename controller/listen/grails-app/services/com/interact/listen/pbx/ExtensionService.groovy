package com.interact.listen.pbx

class ExtensionService {
    def cloudToDeviceService
    def historyService
    def ldapService
    def messageLightService
    def spotCommunicationService
    def springSecurityService

    Extension create(def params, def checkPermission = true) {
        def user = springSecurityService.getCurrentUser()
        if(checkPermission && !user.hasRole('ROLE_ORGANIZATION_ADMIN')) {
            // TODO better exception type
            throw new AssertionError('Action not allowed')
        }

        def extension = new Extension(params)
        if(extension.validate() && extension.save()) {
            cloudToDeviceService.sendContactSync()
            messageLightService.toggle(extension)
            ldapService.addExtension(extension.owner, extension.number)
            historyService.createdExtension(extension)
        }

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
