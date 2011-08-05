package com.interact.listen.voicemail

import com.interact.listen.*
import com.interact.listen.pbx.*
import grails.converters.*
import grails.plugins.springsecurity.Secured
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
        def preserve = [:]
        if(params.sort) preserve.sort = params.sort
        if(params.order) preserve.order = params.order
        if(params.offset) preserve.offset = params.offset
        if(params.max) preserve.max = params.max

        def voicemail = Voicemail.get(params.id)
        if(!voicemail) {
            flash.errorMessage = 'Voicemail not found'
            redirect(controller: 'messages', action: 'inbox', params: preserve)
            return
        }

        if(voicemail.owner != authenticatedUser) {
            redirect(controller: 'login', action: 'denied')
            return
        }

        //hard code the mp3 tag to true since the flash player needs mp3 and there is not reason to not
        //download the mp3 version from the screens as well
        audioDownloadService.download(voicemail.audio, response, true)
        historyService.downloadedVoicemail(voicemail)
    }

    def saveSettings = {
        def user = authenticatedUser
        def preferences = VoicemailPreferences.findByUser(user)
        if(!preferences) {
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

        // theres some serious wtferry going on here with the data binding.
        // - if we use preferences.properties[...] = params, the association binding for
        //   restrictions doesnt work properly
        // - if we use the bindData(preferences, params, '...'), it does work correctly
        //   but it also AUTOMATICALLY binds the associations, even if theyre not bound
        //   using bindData().
        //
        // it seems to work in its current state, but it sucks. try to avoid changing stuff
        // here, and test it well if you do.

        if(params.containsKey('transcribe')) {
            bindData(preferences, params, 'transcribe')
        }
        bindData(preferences, params, 'passcode')
        bindData(preferences, params, 'playbackOrder')
        bindData(preferences, params, 'isEmailNotificationEnabled')
        bindData(preferences, params, 'isSmsNotificationEnabled')
        bindData(preferences, params, 'recurringNotificationEnabled')
        bindData(preferences, params, 'emailNotificationAddress')

        if(params.smsNotificationNumber?.trim().length() > 0) {
            preferences.smsNotificationAddress = params.smsNotificationNumber + '@' + params.smsNotificationProvider
        } else {
            preferences.smsNotificationAddress = null
        }

        if(params.emailSource == 'current') {
            preferences.emailNotificationAddress = preferences.user.emailAddress
        }

        if(preferences.validate() && preferences.save()) {
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
