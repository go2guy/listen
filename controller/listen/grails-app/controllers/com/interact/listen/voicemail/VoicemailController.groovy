package com.interact.listen.voicemail

import grails.converters.*
import grails.plugins.springsecurity.Secured
import com.interact.listen.*
import com.interact.listen.pbx.*
import com.interact.listen.voicemail.Voicemail
import javax.servlet.http.HttpServletResponse as HSR
import org.joda.time.Duration
import org.joda.time.LocalTime
import org.joda.time.format.PeriodFormatterBuilder

//@Licensed(ListenFeature.VOICEMAIL) // TODO licensed annotation
@Secured(['ROLE_VOICEMAIL_USER'])
class VoicemailController {
    static allowedMethods = [
        index: 'GET',
        delete: 'POST',
        download: 'GET',
        inbox: 'GET',
        newCount: 'GET',
        pollingList: 'GET',
        saveSettings: 'POST',
        sendTestEmail: 'POST',
        sendTestSms: 'POST',
        settings: 'GET',
        toggleStatus: 'POST',
        setStatus: 'GET'
    ]

    def audioDownloadService
    def cloudToDeviceService
    def deleteVoicemailService
    def historyService
    def messageLightService
    def newVoicemailCountService
    def springSecurityService
    def voicemailNotificationService

    def index = {
        redirect(action: 'inbox')
    }

    def delete = {
        def preserve = [:]
        if(params.sort) preserve.sort = params.sort
        if(params.order) preserve.order = params.order
        if(params.offset) preserve.offset = params.offset
        if(params.max) preserve.max = params.max

        // TODO log errors

        def voicemail = Voicemail.get(params.id)
        if(!voicemail) {
            flash.errorMessage = 'Voicemail not found'
            redirect(action: 'inbox', params: preserve)
            return
        }

        def user = springSecurityService.getCurrentUser()
        if(voicemail.owner != user) {
            redirect(controller: 'login', action: 'denied')
            return
        }

        deleteVoicemailService.deleteVoicemail(voicemail)

        flash.successMessage = 'Voicemail deleted'
        redirect(action: 'inbox', params: preserve)
    }

    def download = {
        def preserve = [:]
        if(params.sort) preserve.sort = params.sort
        if(params.order) preserve.order = params.order
        if(params.offset) preserve.offset = params.offset
        if(params.max) preserve.max = params.max

        def voicemail = Voicemail.get(params.id)
        if(!voicemail) {
            flash.errorMessage = 'Voicemail not found'
            redirect(action: 'inbox', params: preserve)
            return
        }

        audioDownloadService.download(voicemail.audio, response)
        historyService.downloadedVoicemail(voicemail)
    }

    def inbox = {
        def user = springSecurityService.getCurrentUser()

        params.max = Math.min(params.max ? params.int('max') : 25, 100)
        params.sort = params.sort ? params.sort : 'dateCreated'
        params.order = params.order ? params.order : 'desc'
        def list = Voicemail.findAllByOwner(user, params)
        def count = Voicemail.countByOwner(user)

        list.each {
            it.audio.uri = getMp3Uri(it.audio.uri)
        }

        render(view: 'inbox', model: [voicemailList: list, voicemailTotal: count])
    }

    // ajax
    def newCount = {
        render(contentType: 'application/json') {
            count = newVoicemailCountService.count()
        }
    }

    def saveSettings = {
        def user = springSecurityService.getCurrentUser()
        def preferences = VoicemailPreferences.findByUser(user)
        if(!preferences) {
            preferences = new VoicemailPreferences()
            preferences.user = user
        }
        def oldPasscode = preferences.passcode

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

        bindData(preferences, params, 'transcribe')
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

            flash.successMessage = 'Voicemail Settings Saved'
            redirect(action: 'settings')
        } else {
            render(view: 'settings', model: [preferences: preferences])
        }
    }

    // ajax
    def sendTestEmail = {
        def user = springSecurityService.getCurrentUser()
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
        def user = springSecurityService.getCurrentUser()
        def preferences = VoicemailPreferences.findByUser(user)
        render(view: 'settings', model: [preferences: preferences])
    }

    def toggleStatus = {
        def preserve = [:]
        if(params.sort) preserve.sort = params.sort
        if(params.order) preserve.order = params.order
        if(params.offset) preserve.offset = params.offset
        if(params.max) preserve.max = params.max

        // TODO log errors

        def voicemail = Voicemail.get(params.id)
        if(!voicemail) {
            flash.errorMessage = 'Voicemail not found'
            redirect(action: 'inbox', params: preserve)
            return
        }

        def user = springSecurityService.getCurrentUser()
        if(voicemail.owner != user) {
            redirect(controller: 'login', action: 'denied')
            return
        }

        voicemail.isNew = !voicemail.isNew
        voicemail.save()
        cloudToDeviceService.sendVoicemailSync(voicemail.owner)
        messageLightService.toggle(voicemail.owner)

        flash.successMessage = 'Voicemail status updated'
        redirect(action: 'inbox', params: preserve)
    }
    
    // ajax
    def pollingList = {
        def user = springSecurityService.getCurrentUser()

        params.max = Math.min(params.max ? params.int('max') : 25, 100)
        params.sort = params.sort ? params.sort : 'dateCreated'
        params.order = params.order ? params.order : 'desc'
        def list = Voicemail.findAllByOwner(user, params)
        def count = Voicemail.countByOwner(user)
        def visibleIds = params.visibleIds.trim()
        if(visibleIds.length() > 0) {
            visibleIds = visibleIds.split(",")
        }
        
        def changes = [:]
        def returnList = []
        def currentIds = []
        def addToScreen = []
        def removeFromScreen = []
        def updatedVisibleIds = []
        def formatter = new PeriodFormatterBuilder()
            .printZeroNever()
            .appendMinutes()
            .appendSuffix('m, ')
            .printZeroAlways()
            .minimumPrintedDigits(1)
            .appendSeconds()
            .appendSuffix('s')
            .toFormatter()

        list.each { 
            currentIds.add(String.valueOf(it.id))
            returnList.push(id: it.id,
                            from: it.from().encodeAsHTML(),
                            dateCreated: listen.prettytime(date: it.dateCreated),
                            dateTitle: joda.format(value: it.dateCreated, style: 'LL'),
                            audio: [ uri: getMp3Uri(it.audio.uri),
                                     duration: formatter.print(it.audio.duration.toPeriod()) ],
                            isNew: it.isNew,
                            transcription: it.audio.transcription.encodeAsHTML())
        }
        visibleIds.each { updatedVisibleIds.add(it) }

        def i = 0
        visibleIds.each {
            if(!currentIds.contains(visibleIds[i])) {
                removeFromScreen.add(i)
                updatedVisibleIds.each {
                    if(it == i) {
                        updatedVisibleIds.remove(it)
                    }
                }
            }
            i++
        }

        i = 0
        currentIds.each {
            if(!updatedVisibleIds.contains(currentIds[i])) {
                addToScreen.add(i)
            }
            i++
        }

        changes["add"] = addToScreen
        changes["remove"] = removeFromScreen
        changes["list"] = returnList
        changes["currentIds"] = currentIds

        render changes as JSON
    }
    
    def setStatus = {
        def voicemail = Voicemail.get(params.id)
        if(!voicemail) {
            response.sendError(HSR.SC_NOT_FOUND, 'Voicemail not found')
            response.flushBuffer()
        }

        def user = springSecurityService.getCurrentUser()
        if(voicemail.owner != user) {
            response.sendError(HSR.SC_BAD_REQUEST, 'Current user not owner of voicemail')
            response.flushBuffer()
        }

        voicemail.isNew = params.newStatus == 'new' ? true : false
        voicemail.save()
        cloudToDeviceService.sendVoicemailSync(voicemail.owner)
        messageLightService.toggle(voicemail.owner)

        response.flushBuffer()
    }

    private String getMp3Uri(String uri) {
        if(uri.endsWith(".wav"))
        {
            uri = uri.replace(".wav", ".mp3");
        }
        
        if(!uri.endsWith(".mp3"))
        {
            uri.concat(".mp3");
        }

        return uri.encodeAsHTML();
    }
}
