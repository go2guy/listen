package com.interact.listen

import com.interact.listen.PhoneNumberType
import com.interact.listen.conferencing.Conference
import com.interact.listen.conferencing.PinType
import com.interact.listen.voicemail.VoicemailPreferences

class UserCreationService {
    static scope = 'singleton'
    static transactional = true

    def cloudToDeviceService
    def createPhoneNumberService
    def historyService
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

        def roles = ['ROLE_VOICEMAIL_USER', 'ROLE_CONFERENCE_USER', 'ROLE_FINDME_USER']
        if(isOperator) {
            roles << 'ROLE_ORGANIZATION_ADMIN'
            roles << 'ROLE_ATTENDANT_ADMIN'
        }

        def user = create(params, organization, roles)
        if(!user.errors.hasErrors()) {
            if(params.extension) {
                def p = [
                    isPublic: true,
                    number: params.extension,
                    supportsMessageLight: true,
                    type: PhoneNumberType.EXTENSION,
                    'owner.id': user.id
                ]
                def phoneNumber = createPhoneNumberService.createPhoneNumberByOperator(p)
                if(!phoneNumber.hasErrors()) {
                    user.addToPhoneNumbers(phoneNumber)
                }
            }

            // TODO default passcode should be configurable?
            new VoicemailPreferences(passcode: '1234', user: user).save()
            createDefaultConference(user)
        }

        cloudToDeviceService.sendContactSync()
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
        user.enabled = true

        // TODO passwords need to be salted
        // TODO allow Role configuration via user create/edit screens

        if(user.validate() && user.save()) {
            roles.each {
                UserRole.create(user, Role.findByAuthority(it))
            }
            historyService.createdUser(user)
        }

        return user
    }

    private def createDefaultConference(user) {
        def conference = new Conference(description: "${user.realName}'s Conference",
                                        owner: user).save()
        PinType.values().each {
            randomPinGeneratorService.createConferencePin(conference, it).save()
        }
    }
}
