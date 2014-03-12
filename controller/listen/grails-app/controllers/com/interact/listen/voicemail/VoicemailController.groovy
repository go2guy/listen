package com.interact.listen.voicemail

import com.interact.listen.*
import com.interact.listen.pbx.*
import com.interact.listen.acd.*
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured

import javax.servlet.http.HttpServletResponse as HSR

@Secured(['ROLE_VOICEMAIL_USER'])
class VoicemailController {
    static allowedMethods = [
        download: 'GET',
        saveSettings: 'POST',
        sendTestEmail: 'POST',
        sendTestSms: 'POST',
        settings: 'GET'
    ]

    def audioDownloadService
    def historyService
    def voicemailNotificationService

    def download = {
        def user = authenticatedUser

        if ( !user ) {
          redirect(controller: 'login', action: 'denied')
          return
        }

        def preserve = [:]
        if(params.sort) preserve.sort = params.sort
        if(params.order) preserve.order = params.order
        if(params.offset) preserve.offset = params.offset
        if(params.max) preserve.max = params.max
        preserve.currentSkill = params.currentSkill ?: null

        def voicemail = Voicemail.get(params.id)
        if(!voicemail) {
            flash.errorMessage = 'Voicemail not found'
            redirect(controller: 'messages', action: 'inbox', params: preserve)
            return
        }

        // Determine whether user should be able to access the voicemail

        // are they the voicemail owner???
        def authorized = (voicemail.owner == authenticatedUser)

        if ( !authorized ) {
          // does the voicemail belong to a user designated as an acd skill voicemail user???
          if ( voicemail.owner.acdUserStatus.acdQueueStatus == AcdQueueStatus.VoicemailBox ) {
            // if so, is the current user associated with the same skill
            def voicemailSkill = UserSkill.findByUser(voicemail.owner).skill
            UserSkill.findAllByUser(user)?.each() { userSkill ->
              if ( userSkill.skill == voicemailSkill ) {
                authorized = true
              }
            }
          }
        }

        // if they're still not authorized...
        if ( !authorized ) {
          // redirect to login
          redirect(controller: 'login', action: 'denied')
        }

        //hard code the mp3 tag to true since the flash player needs mp3 and there is not reason to not
        //download the mp3 version from the screens as well
        audioDownloadService.download(voicemail.audio, response, true)
        historyService.downloadedVoicemail(voicemail)
    }

    def saveSettings = {
        log.debug "Voicemail saveSettings [${params}]"
        def user = authenticatedUser
        def preferences = VoicemailPreferences.findByUser(user)
        if(!preferences) {
            log.info "Didn't find voicemail preferences for user [${params}]"
            preferences = new VoicemailPreferences()
            preferences.user = user
        }
        
        def oldPasscode = preferences.passcode
        def oldIsEmailNotificationEnabled = preferences.isEmailNotificationEnabled
        def oldIsSmsNotificationEnabled = preferences.isSmsNotificationEnabled
        def oldRecurringNotificationsEnabled = preferences.recurringNotificationsEnabled
        def oldEmailNotificationAddress = preferences.emailNotificationAddress
        def oldSmsNotificationAddress = preferences.smsNotificationAddress

        preferences.emailTimeRestrictions.clear()
        preferences.smsTimeRestrictions.clear()

        // bind the data from params to the preferences
        preferences.properties = params

        if(params.smsNotificationNumber?.trim().length() > 0) {
            preferences.smsNotificationAddress = params.smsNotificationNumber + '@' + params.smsNotificationProvider
        } else {
            preferences.smsNotificationAddress = null
        }

        if(params.emailSource == 'current') {
            preferences.emailNotificationAddress = preferences.user.emailAddress
        }

        log.debug "Lets attempt to save preferneces [${preferences.passcode}]"
        if(preferences.validate() && preferences.save()) {
            log.debug "We saved the preferences [${params}]"
            if(oldPasscode != preferences.passcode) {
                historyService.changedVoicemailPin(user, oldPasscode, preferences.passcode)
            }

            boolean wasEmailJustEnabled = false
            if(oldIsEmailNotificationEnabled != preferences.isEmailNotificationEnabled) {
                if(preferences.isEmailNotificationEnabled) {
                    historyService.enabledNewVoicemailEmail(preferences)
                    wasEmailJustEnabled = true
                } else {
                    historyService.disabledNewVoicemailEmail(preferences)
                }
            }

            boolean wasSmsJustEnabled = false
            if(oldIsSmsNotificationEnabled != preferences.isSmsNotificationEnabled) {
                if(preferences.isSmsNotificationEnabled) {
                    historyService.enabledNewVoicemailSms(preferences)
                    wasSmsJustEnabled = true
                } else {
                    historyService.disabledNewVoicemailSms(preferences)
                }
            }

            if(oldRecurringNotificationsEnabled != preferences.recurringNotificationsEnabled) {
                if(preferences.recurringNotificationsEnabled) {
                    historyService.enabledRecurringVoicemailSms(preferences)
                } else {
                    historyService.disabledRecurringVoicemailSms(preferences)
                }
            }

            if(oldEmailNotificationAddress != preferences.emailNotificationAddress && !wasEmailJustEnabled) {
                historyService.changedNewVoicemailEmailAddress(preferences, oldEmailNotificationAddress)
            }

            if(oldSmsNotificationAddress != preferences.smsNotificationAddress && !wasSmsJustEnabled) {
                historyService.changedNewVoicemailSmsNumber(preferences, oldSmsNotificationAddress)
            }

            flash.successMessage = 'Voicemail Settings Saved'
            redirect(action: 'settings')
        } else {
            log.error "We failed saved the preferences [${params}]"
            render(view: 'settings', model: [preferences: preferences])
        }
    }

    // ajax
    def sendTestEmail = {
        def user = authenticatedUser
        def address = params.address
        if(!address || address.trim() == '') {
            address = user.emailAddress
        }

        voicemailNotificationService.sendNewVoicemailTestEmail(address)
        response.flushBuffer()
    }

    // ajax
    def sendTestSms = {
        if(!params.address || params.address.trim() == '') {
            response.sendError(HSR.BAD_REQUEST, 'Please provide an address')
            return
        }

        voicemailNotificationService.sendNewVoicemailTestSms(params.address)
        response.flushBuffer()
    }

    def settings = {
        def user = authenticatedUser
        def preferences = VoicemailPreferences.findByUser(user)
        render(view: 'settings', model: [preferences: preferences])
    }
}
