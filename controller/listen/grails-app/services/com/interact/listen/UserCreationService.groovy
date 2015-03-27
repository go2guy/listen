package com.interact.listen

import com.interact.listen.conferencing.Conference
import com.interact.listen.acd.*
import com.interact.listen.conferencing.PinType
import com.interact.listen.voicemail.VoicemailPreferences
import org.apache.log4j.Logger

class UserCreationService {
    def cloudToDeviceService
    def extensionService
    def historyService
    def ldapService
    def randomPinGeneratorService
    def springSecurityService

    def createCustodian(def params) {
        return create(params, null, ['ROLE_CUSTODIAN'])
    }

    def createOperator(def params, def organization) {
        return createStandardUser(params, organization, true)
    }

    def createUser(def params, def organization) {
        return createStandardUser(params, organization, false)
    }

    private def createStandardUser(def params, def organization, boolean isOperator) {
        // application roles are granted to users even if the feature for the
        // roles is not licensed. this ensures that users will be able to access
        // the application if the feature is licensed later.

        def roles = ['ROLE_VOICEMAIL_USER', 'ROLE_CONFERENCE_USER', 'ROLE_FINDME_USER', 'ROLE_FAX_USER']
        if(isOperator) {
            roles << 'ROLE_ORGANIZATION_ADMIN'
            roles << 'ROLE_ATTENDANT_ADMIN'
        }

        log.debug "Create standard user with params [${params}]"
        def user = create(params, organization, roles)
        if(!user.errors.hasErrors()) {
            log.debug "create user didn't have errors, lets LDAP"
            ldapService.addUser(user)

            // TODO default passcode should be configurable?
            new VoicemailPreferences(passcode: '1234', user: user).save()
            createDefaultConference(user)
        } else {
            log.error "user create had errors! [${user.errors}]"
        }

        try {
            log.debug "Attempt cloudtodeviceServcie"
            cloudToDeviceService.sendContactSync()
        } catch (Exception e) {
            log.error "Exception caught from cloud to device service [${e}]"
        }

        return user
    }

    private def create(def params, def organization, def roles) {
        def user = new User()
        user.properties['username', 'realName', 'pass', 'confirm', 'emailAddress'] = params
        if(user.pass?.trim()?.length() > 0) {
            // will be null if pass is blank, which should trigger validation error
            user.password = springSecurityService.encodePassword(user.pass)
        }
        
        user.organization = organization
        log.debug "We're going to create user [${user.username}] with org [${user.organization.id}][${user.organization}]"
        user.enabled = true

        // TODO passwords need to be salted
        // TODO allow Role configuration via user create/edit screens

        try
        {
            AcdUserStatus theStatus = AcdUserStatus.create(user);
            user.acdUserStatus = theStatus;

            /* def acdUserStatus = AcdUserStatus.findByOwner(user) */
            /* acdUserStatus.acdQueueStatus = AcdQueueStatus.UNAVAILABLE */

            if(user.validate())
            {
                user = user.save();
                if(user)
                {
                    roles.each
                    {
                        UserRole.create(user, Role.findByAuthority(it))
                    }
                    historyService.createdUser(user)
                }
            }
        }
        catch (Exception e)
        {
          log.error "Exception caught trying to create user [${e}]"
        }

        return user
    }

    private def createDefaultConference(user) {
        def conference = new Conference(description: "${user.realName}'s Conference",
                                        owner: user).save()
        conference.pins = []
        PinType.values().each {
            conference.pins << randomPinGeneratorService.createConferencePin(conference, it).save()
        }
        return conference
    }
}
