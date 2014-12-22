package com.interact.listen.pbx

import com.interact.listen.Organization
import org.joda.time.DateTime
import javax.servlet.http.HttpServletResponse as HSR
import org.apache.log4j.Logger

class Result {
    Extension extension
    SipPhone sipPhone
}

class RegResponse {
    Integer returnCode
    Extension extension
}

class ExtensionService {
    def cloudToDeviceService
    def historyService
    def ldapService
    def messageLightService
    def spotCommunicationService
    def springSecurityService
    static transactional = false

    void register(def params, def checkPermission = true) {
        log.debug "register extension with params [${params}]"
    }

    Result create(def params, Organization organization, def checkPermission = true) {
        log.debug "Attempt to create extension [${params}]"
        def result = new Result()
        def user = springSecurityService.getCurrentUser()
        if (user)
            log.debug "We got the current user [${user.username}]"
        else
            log.debug "We don't have a current user"

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

        try {
            result.extension = new Extension(params)
            result.extension.extLength = organization.extLength

            params.cseq = 0
            params.dateRegistered = null

            result.sipPhone = new SipPhone(params)
            result.sipPhone.organization = organization
            result.sipPhone.extension = result.extension
            result.sipPhone.passwordConfirm = params?.passwordConfirm
            result.sipPhone.ip = null
            result.sipPhone.dateRegistered = null

            log.debug "Now actually create the sipPhone extension for [${params}]"
            log.debug("sipPhone info Ext Test        [${result.extension.sipPhone?.username}]")
            log.debug("sipPhone info id              [${result.sipPhone?.id}]")
            log.debug("sipPhone info ext id          [${result.sipPhone?.extension.id}]")
            log.debug("sipPhone info org id          [${result.sipPhone?.organization.id}]")
            log.debug("sipPhone info username        [${result.sipPhone?.username}]")
            log.debug("sipPhone info password        [${result.sipPhone?.password}]")
            log.debug("sipPhone info passwordConfirm [${result.sipPhone?.passwordConfirm}]")
            log.debug("sipPhone info ip              [${result.sipPhone?.ip}]")
            log.debug("sipPhone info cseq            [${result.sipPhone?.cseq}]")
            log.debug("sipPhone info date reg        [${result.sipPhone?.dateRegistered}]")

            result.extension.sipPhone = result.sipPhone
            log.debug "Now actually create the extension for [${params}]"
            if (result.extension.sipPhone.validate() && result.extension.validate() && result.extension.save(flush: true)) {
                log.debug "We've create a new extension now cloud to device"
                cloudToDeviceService.sendContactSync()

                log.debug "We've create a new extension now add extension to ldap"
                ldapService.addExtension(result.extension.owner, result.extension.number)

                log.debug "We've create a new extension now add history service"
                historyService.createdExtension(result.extension)

                log.debug "finished history service"
            } else {
                log.error "Failed to create extension due to errors [${result.extension.errors}]"
                return result
            }
            log.debug "Successfully created extension [${result.extension}]"
            return result
        }catch ( Exception e ) {
            log.debug("Caught exception [${e}]")
        }
        return result
    }

    void delete(Extension extension) {
        def user = springSecurityService.getCurrentUser()
        if(!user.hasRole('ROLE_ORGANIZATION_ADMIN')) {
            // TODO better exception type
            throw new AssertionError('Action not allowed')
        }

        extension.delete(flush: true)
        
        historyService.deletedExtension(extension)
        ldapService.removeExtension(extension.owner, extension.number)
        cloudToDeviceService.sendContactSync()
        messageLightService.toggle(extension.number, extension.ip, false)
    }

    Result update( def params, Extension extension, Organization organization ) {
        log.debug("ExtensionService update with params [${params}]")
        def user = springSecurityService.getCurrentUser()
        def result = new Result()
        try {
            log.debug("ExtensionService lets update it")
            result.extension = extension
            result.sipPhone = SipPhone.findByExtension(extension)

            if (!result.sipPhone) {
                log.debug("sipPhone record doesn't exist for extension id [${extension.id}]")
                params.extension = extension.id
                params.cseq = 0
                params.dateRegistered = 0
                result.sipPhone = new SipPhone(params)
                result.sipPhone.organization = organization
                result.sipPhone.extension = extension
                result.sipPhone.passwordConfirm = params?.passwordConfirm
                result.sipPhone.dateRegistered = null
            } else {
                if ((params?.password && params?.passwordConfirm) || (params?.password != result.sipPhone.password) ) {
                    log.debug("updateExtension, set password")
                    result.sipPhone.password = params.password
                    result.sipPhone.passwordConfirm = params.passwordConfirm
                } else {
                    // user has not provided passwords, so lets set the password confirm to what we had in the db to pass validation
                    result.sipPhone.passwordConfirm = result.sipPhone.password
                }
                result.sipPhone.username = params?.username
            }

            log.debug("sipPhone info id              [${result.sipPhone.id}]")
            log.debug("sipPhone info ext id          [${result.sipPhone.extension}]")
            log.debug("sipPhone info org id          [${result.sipPhone.organization}]")
            log.debug("sipPhone info username        [${result.sipPhone.username}]")
            log.debug("sipPhone info password        [${result.sipPhone.password}]")
            log.debug("sipPhone info passwordConfirm [${result.sipPhone.passwordConfirm}]")
            log.debug("sipPhone info ip              [${result.sipPhone.ip}]")
            log.debug("sipPhone info cseq            [${result.sipPhone.cseq}]")
            log.debug("sipPhone info date reg        [${result.sipPhone.dateRegistered}]")

            // Now lets process the extension domain
            result.extension.sipPhone = result.sipPhone
            result.extension.extLength = organization?.extLength

            def originalNumber = extension.number
            def originalForwardedTo = extension.forwardedTo
            def originalOwner = extension.owner

            if(user.hasRole('ROLE_ORGANIZATION_ADMIN')) {
                result.extension.properties = params
            } else {
                result.extension.properties['forwardedTo', 'greeting'] = params
            }

            if(result.sipPhone.validate() && result.extension.validate() && result.extension.save(flush: true)) {
                log.debug("Extension info successfully updated")
            } else {
                log.error "Failed to update extension due to extension errors [${result.extension.errors}]"
                log.error "Failed to update extension due to sipPhone errors [${result.extension.sipPhone.errors}]"
                return result
            }

            cloudToDeviceService.sendContactSync()

            if(originalNumber != extension.number) {
                ldapService.changeExtension(result.extension.owner, originalNumber, result.extension.number)
            }

            if(originalNumber != result.extension.number) {
                messageLightService.toggle(result.extension)
            }

            if(originalNumber != result.extension.number || originalOwner != result.extension.owner) {
                def fake = new Expando(number: originalNumber,
                        owner: originalOwner)
                historyService.deletedExtension(fake)
                historyService.createdExtension(result.extension)
            }

            if(originalForwardedTo != result.extension.forwardedTo) {
                if(result.extension.forwardedTo != null) {
                    historyService.forwardedExtension(result.extension)
                } else {
                    historyService.unforwardedExtension(result.extension)
                }
            }
        } catch ( Exception e ) {
            log.error("Caught exception on extension update [${e}]")
        }
        return result
    }

    RegResponse sipRegistration(Extension extIn) {

        def regResponse = new RegResponse()
        log.debug("sipRegistration for extension [${extIn?.number}] with username [${extIn.sipPhone?.username}]")
        log.debug("sipRegistration request for [${extIn?.number}]         from IP [${extIn?.sipPhone.ip}]")
        log.debug("sipRegistration request for [${extIn?.number}]       with CSeq [${extIn?.sipPhone.cseq}]")

        def extension = Extension.findByNumber(extIn?.number)

        if (!extension) {
            log.debug("sipRegistration failed to find extension number [${extIn?.number}]")
            regResponse.extension = extIn
            regResponse.returnCode = HSR.SC_NOT_FOUND
            return regResponse
        }

        if (!extension?.sipPhone) {
            log.error("sipRegistration extension [${extension.number}] does not have sipPhone entry")
            regResponse.returnCode = HSR.SC_INTERNAL_SERVER_ERROR
            regResponse.extension = extension
            return regResponse
        }

        if (!extension.owner.enabled()) {
            log.debug("sipRegistration username [${extension.sipPhone.username}] is not enabled")
            regResponse.returnCode = HSR.SC_UNAUTHORIZED
            regResponse.extension = extension
            return regResponse
        }

        if (!extIn?.sipPhone?.username) {
            // We're not authorizing based upon user name, just ID.  We've found a match so lets get out
            log.debug("Extension validated based upon ID")
            regResponse.returnCode = HSR.SC_OK
            regResponse.extension = extension
            return regResponse
        }

        log.debug("sipRegistration username validation [${extension?.sipPhone?.username}]?=[${extIn?.sipPhone?.username}]")
        // we have a valid extension, lets see if the usernames match
        if(extension.sipPhone.username != extIn?.sipPhone?.username){
            log.debug("sipRegistration username provided does not match username of extension [${extension.sipPhone.username}]!=[${extIn.sipPhone?.username}]")
            regResponse.extension = extension
            regResponse.returnCode = HSR.SC_NOT_FOUND
            return regResponse
        } else {
            // We have a match for usernames
            log.debug("sipRegistration username provided matches username of extension [${extension.sipPhone.username}]")
        }

        if(extension.sipPhone.cseq > extIn?.sipPhone?.cseq) {
            // the CSeq we're given seems to be old compared to the one we have
            log.debug("sipRegistration CSeq provided is less than what we have in our db, ignore at this time.")
            // TODO maybe do something different if the cseq number in is less than what we have in the db
        }

        def origIp
        if(extIn.sipPhone?.ip) {
            origIp = extension.sipPhone.ip
            extension.sipPhone.ip = extIn.sipPhone.ip
            log.debug("sipRegistration request for [${extension?.number}] to update IP [${extension.sipPhone.ip}]")
        }

        if(extIn.sipPhone?.cseq) {
            extension.sipPhone.cseq = extIn.sipPhone.cseq
            log.debug("sipRegistration request for [${extension?.number}] to update CSeq [${extension.sipPhone.cseq}]")
        }

        if(extIn.sipPhone?.realName) {
            extension.sipPhone.realName = extIn.sipPhone.realName
            log.debug("sipRegistration request for [${extension?.number}] to update realname [${extension.sipPhone.realName}]")
        }

        extension.sipPhone.dateRegistered = new DateTime()
        log.debug("sipRegistration request for [${extension?.number}] to update dateReg [${extension.sipPhone.dateRegistered}]")

        log.debug("sipRegistration request for [${extension?.number}] to expire [${extIn.sipPhone?.expires}] from now")
        extension.sipPhone.dateExpires = new DateTime().plusSeconds(extIn.sipPhone.expires)
        log.debug("sipRegistration request for [${extension?.number}] to set expire date [${extension.sipPhone.dateExpires}]")

        extension.sipPhone.registered = true

        // Just to keep the domain validation happy
        extension.sipPhone.passwordConfirm = extension.sipPhone.password

        if(extension.sipPhone.validate() && extension.sipPhone.save(flush: true)) {
            log.debug("sipDeregistration of extension [${extension?.number}] successfully updated in db")
            regResponse.returnCode = HSR.SC_OK
            if (extension.sipPhone.ip != origIp) {
                log.debug "sipRegistration, we have a different IP lets notifiy of message light at [${extension.sipPhone.ip}]"
                messageLightService.toggle(extension)
            }
        } else {
            log.error "Failed to update extension [${extension?.number}] due to sipPhone errors [${extension.sipPhone.errors}]"
            regResponse.returnCode = HSR.SC_INTERNAL_SERVER_ERROR
        }

        regResponse.extension = extension
        return regResponse
    }

    RegResponse sipDeregistration(Extension extIn) {

        def regResponse = new RegResponse()
        log.debug("sipDeregistration for extension [${extIn?.number}] with username [${extIn.sipPhone?.username}]")

        def extension = Extension.findByNumber(extIn?.number)
        if (!extension) {
            log.debug("sipDeregistration failed to find extension number [${extIn?.number}]")
            regResponse.extension = extIn
            regResponse.returnCode = HSR.SC_NOT_FOUND
            return regResponse
        }

        log.debug("sipDeregistration request for [${extension?.number}] to update dateReg [${extension.sipPhone.dateRegistered}]")
        extension.sipPhone.dateRegistered = new DateTime()
        extension.sipPhone.registered = false
        extension.sipPhone.cseq = 0
        // Just to keep the domain validation happy
        extension.sipPhone.passwordConfirm = extension.sipPhone.password

        extension.sipPhone.dateExpires = new DateTime()
        log.debug("sipRegistration request for [${extension?.number}] to set expire date [${extension.sipPhone.dateExpires}]")

        if(extension.sipPhone.validate() && extension.sipPhone.save(flush: true)) {
            log.debug("sipDeregistration of extension [${extension?.number}] successfully updated in db")
            regResponse.returnCode = HSR.SC_OK
        } else {
            log.error "Failed to update extension [${extension?.number}] due to sipPhone errors [${extension.sipPhone.errors}]"
            regResponse.returnCode = HSR.SC_INTERNAL_SERVER_ERROR
        }

        regResponse.extension = extension
        return regResponse

    }
}
