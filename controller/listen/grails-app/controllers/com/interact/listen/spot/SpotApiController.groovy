package com.interact.listen.spot

import com.interact.listen.Audio
import com.interact.listen.CallData
import com.interact.listen.MobilePhone
import com.interact.listen.PhoneNumber
import com.interact.listen.TranscriptionConfiguration
import com.interact.listen.WildcardNumberMatcher
import com.interact.listen.acd.AcdCall
import com.interact.listen.acd.AcdCallStatus
import com.interact.listen.android.DeviceRegistration
import com.interact.listen.attendant.*
import com.interact.listen.attendant.action.*
import com.interact.listen.conferencing.*
import com.interact.listen.exceptions.ListenAcdException
import com.interact.listen.history.CallHistory
import com.interact.listen.license.ListenFeature
import com.interact.listen.pbx.*
import com.interact.listen.pbx.findme.*
import com.interact.listen.stats.*
import com.interact.listen.voicemail.*
import com.interact.listen.User
import com.interact.listen.DirectMessageNumber
import com.interact.listen.DirectInwardDialNumber
import com.interact.listen.OtherPhone
import com.interact.listen.Organization
import com.interact.listen.voicemail.afterhours.AfterHoursConfiguration
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.codehaus.groovy.grails.web.converters.exceptions.ConverterException
import org.joda.time.format.DateTimeFormatter

import javax.validation.constraints.Null
import java.security.InvalidParameterException
import javax.servlet.http.HttpServletResponse as HSR
import org.apache.commons.lang.StringUtils
import org.joda.time.*
import org.joda.time.format.DateTimeFormat



@Secured(['ROLE_SPOT_API'])
class SpotApiController {
    static allowedMethods = [
        addAcdCall: 'POST',
        addCallHistory: 'POST',
        addConferenceRecording: 'POST',
        addParticipant: 'POST',
        addVoicemail: 'POST',
        androidEmailContact: 'GET',
        androidEmailContacts: 'GET',
        androidGetDeviceRegistration: 'GET',
        androidNumberContact: 'GET',
        androidNumberContacts: 'GET',
        androidUpdateDeviceRegistration: 'PUT',
        androidVoicemailDownload: 'GET',
        canAccessFeature: 'GET',
        canDial: 'GET',
        deleteFindMeNumber: 'DELETE',
        deleteParticipant: 'DELETE',
        deleteVoicemail: 'DELETE',
        dial: 'GET',
        dnisLookup: 'GET',
        getAfterHoursSubscriber: 'GET',
        getConference: 'GET',
        getEnabledFeatureStatus: 'GET',
        getOrganization: 'GET',
        getParticipants: 'GET',
        getPhoneNumber: 'GET',
        getPin: 'GET',
        getTranscriptionConfiguration: 'GET',
        getUser: 'GET',
        getVoicemail: 'GET',
        listFindMeConfiguration: 'GET',
        listPhoneNumbers: 'GET',
        listUsers: 'GET',
        listVoicemails: 'GET',
        lookupAccessNumber: 'GET',
        menuAction: 'GET',
        register: 'POST',
        sipRegister: 'POST',
        setPhoneNumber: 'POST',
        updateAcdCall: 'PUT',
        updateConference: 'PUT',
        updateFindMeExpiration: 'PUT',
        updateFindMeNumber: 'PUT',
        updatePhoneNumber: 'PUT',
        updateUser: 'PUT',
        updateVoicemail: 'PUT',
        transferAcdCall: 'PUT'
    ]

    def acdService
    def audioDownloadService
    def cloudToDeviceService
    def conferenceService
    def googleAuthService
    def historyService
    def hrefParserService
    def inboxMessageService
    def licenseService
    def menuLocatorService
    def messageLightService
    def statWriterService
    def voicemailNotificationService
    def callRoutingService
    def extensionService
    def springSecurityService
    def callService

    /**
     * Add an acd call to the queue.
     */
    def addAcdCall =
    {
        if(log.isDebugEnabled())
        {
            log.debug("Entering addAcdCall");
        }

        try
        {
            def json = JSON.parse(request)
            response.status = HSR.SC_CREATED

            if(log.isDebugEnabled())
            {
                log.debug("AddAcdCall JSON: " + json);
                log.debug("Call from IVR: " + request.remoteAddr);
            }

            if(json.ani && json.dnis && json.selection && json.sessionId && json.commonCallId && json.initTime)
            {
                def formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
                DateTime initDTime = formatter.parseDateTime(json.initTime)
                acdService.acdCallAdd(json.ani, json.dnis, null, null, json.selection, null, json.sessionId, request.remoteAddr, json.commonCallId, initDTime)
            }
            else
            {
                throw new InvalidParameterException("Missing required parameter for Add Acd Call request");
            }

            response.flushBuffer()

            if(log.isDebugEnabled())
            {
                log.debug("Exiting addAcdCall.");
            }
        }
        catch(Exception e)
        {
            log.error("Exception adding ACD Call: " + e.getMessage(), e);
            response.sendError(HSR.SC_BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Request from IVR to tranfer ACD call
     */
    def transferAcdCall =
    {
        log.debug "transferAcdCall request[${request}] with params[${params}]"

        try
        {
            def json = JSON.parse(request)
            response.status = HSR.SC_CREATED

            log.debug("transferAcdCall JSON: " + json);

            if(json.sessionId && json.xferDestination && json.organization) {
                def targetSessionId = null
                if(json.targetSessionId) {
                    targetSessionId = json.targetSessionId
                }
                acdService.transferCallFromIVR(getIdFromHref(json.organization), json.sessionId, json.xferDestination, targetSessionId);
            }
            else {
                throw new InvalidParameterException("Missing required parameter for ACD Queue Update for transfer.")
            }

            response.flushBuffer();
        }
        catch(ConverterException ce)
        {
            log.error("Exception parsing transfer Acd Call request : " + ce.getMessage());
            response.sendError(HSR.SC_BAD_REQUEST, ce.getMessage());
        }
        catch(ListenAcdException lae)
        {
            log.error("Listen ACD Exception: " + lae.getMessage());
            response.sendError(HSR.SC_BAD_REQUEST, lae.getMessage());
        }
        catch(Exception e)
        {
            log.error("Exception transfering ACD Call: " + e.getMessage(), e);
            response.sendError(HSR.SC_BAD_REQUEST, e.getMessage());
        }

        log.debug("Exiting transferAcdCall()");
    }

    /**
     * Update an existing queued acd call.
     */
    def updateAcdCall =
    {
        log.debug "updateAcdCall request[${request}] with params[${params}]"

        try
        {
            def json = JSON.parse(request)
            response.status = HSR.SC_CREATED

            if(log.isDebugEnabled())
            {
                log.debug("UpdateAcdCall JSON: " + json);
            }

            if(json.sessionId && json.status && json.event)
            {
                acdService.acdCallStatusUpdate(json.sessionId, json.status, json.event);
            }
            else if(json.sessionId && json.status)
            {
                acdService.acdCallStatusUpdate(json.sessionId, json.status);
            }
            else
            {
                throw new InvalidParameterException("Missing required parameter for ACD Queue Update.")
            }

            response.flushBuffer();
        }
        catch(ConverterException ce)
        {
            log.error("Exception parsing Update Acd Call request : " + ce.getMessage());
            response.sendError(HSR.SC_BAD_REQUEST, ce.getMessage());
        }
        catch(ListenAcdException lae)
        {
            log.error("Listen ACD Exception: " + lae.getMessage());
            response.sendError(HSR.SC_BAD_REQUEST, lae.getMessage());
        }
        catch(Exception e)
        {
            log.error("Exception updating ACD Call: " + e.getMessage(), e);
            response.sendError(HSR.SC_BAD_REQUEST, e.getMessage());
        }

        if(log.isDebugEnabled())
        {
            log.debug("Exiting updateAcdCall()");
        }
    }

    def addCallHistory = {
        def formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
        def json = JSON.parse(request)
        response.status = HSR.SC_CREATED

        log.debug("addCallHistory with json [${json}]")
        def callHistory = new CallHistory()
        Organization callOrganization = Organization.get(getIdFromHref(json.organization.href));
        callHistory.dateTime = formatter.parseDateTime(json.date)
        callHistory.ani = json.ani
        callHistory.outboundAni = json.outboundAni
        callHistory.commonCallId = json.commonCallId

        String dnis = json.dnis;
        if(callOrganization != null && dnis.size() == callOrganization.getExtLength() + 1)
        {
            dnis = dnis.substring(1);
        }

        callHistory.dnis = dnis;
        callHistory.inboundDnis = json.inboundDnis
        callHistory.duration = new Duration(json.duration)
        callHistory.fromUser = User.lookupByPhoneNumberAndOrganization(json.ani, callOrganization);
        callHistory.toUser = User.lookupByPhoneNumberAndOrganization(json.dnis, callOrganization);
        callHistory.organization = callOrganization;
        callHistory.sessionId = json?.sessionId;
        callHistory.ivr = request.remoteAddr;

        if(json.result) {
            callHistory.result = json.result
        } else {
            callHistory.result = ''
        }

        if(callHistory.validate() && callHistory.save()) {
            response.flushBuffer()
        } else {
            response.sendError(HSR.SC_BAD_REQUEST, beanErrors(callHistory))
        }
    }

    def addConferenceRecording = {
        def json = JSON.parse(request)
        response.status = HSR.SC_CREATED
        log.debug "addConferencingRecording request with params [${json}]"

        File audioFile = new File(new URI(json.uri));
        //See if recording already exists
        Audio existing = Audio.findByFile(audioFile);
        if(existing != null)
        {
            log.debug("Audio file[" + json.uri + "] already exists");
            return;
        }

        Recording.withTransaction { status ->
            def audio = new Audio()
            audio.duration = new Duration(json.duration as Long)
            audio.file = new File(new URI(json.uri))
            if(!(audio.validate() && audio.save())) {
                status.setRollbackOnly()
                response.sendError(HSR.SC_BAD_REQUEST, beanErrors(audio))
            }

            def recording = new Recording()
            recording.conference = Conference.get(getIdFromHref(json.conference.href))
            recording.audio = audio

            if(!recording.conference.owner.enabled()) {
                status.setRollbackOnly()
                response.sendError(HSR.SC_FORBIDDEN)
                return
            }

            if(!(recording.validate() && recording.save())) {
                status.setRollbackOnly()
                response.sendError(HSR.SC_BAD_REQUEST, beanErrors(recording))
                return
            }
        }

        response.flushBuffer()
    }

    def addParticipant = {
        def json = JSON.parse(request)
        response.status = HSR.SC_CREATED
        
        log.debug "add conference participant json : [${json}]"

        def conference = Conference.get(getIdFromHref(json?.conference?.href))
        if(!conference) {
            response.sendError(HSR.SC_BAD_REQUEST, "Property [conference] with value [${params?.conference?.href}] references a non-existent entity")
            return
        }

        if(!conference?.owner.enabled()) {
            response.sendError(HSR.SC_FORBIDDEN)
            return
        }

        def participant = new Participant()
        Participant.withTransaction { status ->
            def audio = new Audio()
            audio.duration = new Duration(0)
            audio.transcription = ""
            if(json?.audioResource.length() == "file:".length()) {
                log.debug "No audio file is present"
            } else {
                audio.file = new File(new URI(json?.audioResource))
            }
            log.debug("audio.file [${audio?.file}]")

            if(!(audio.validate() && audio.save())) {
                status.setRollbackOnly()
                response.sendError(HSR.SC_BAD_REQUEST, beanErrors(audio))
                return
            }

            log.debug "Query participant table for: [${json.number}][${conference}]"
            def aniList = Participant.createCriteria().list(max: 5, offset: 0) {
                like('ani', "${json.number}%")
                eq('conference', conference)
            }
            log.debug "Found:  [${aniList.totalCount}]"
            
            if (aniList.totalCount != 0) {
                participant.ani = "${json.number}(${aniList.totalCount})"
                log.debug "ani already exists, alter number provided : [${participant.ani}]"
            }
            else {
                participant.ani = json.number
                log.debug "ani does not already exists, use number provided : [${participant.ani}]"
            }
            
            participant.conference = conference
            participant.isAdmin = json?.isAdmin.toBoolean()
            participant.isAdminMuted = json?.isAdminMuted.toBoolean()
            participant.isMuted = json?.isMuted.toBoolean()
            participant.isPassive = json?.isPassive.toBoolean()
            participant.recordedName = audio
            participant.sessionId = json?.sessionID

            log.debug "Attempt to save participant : [${participant.ani}]"
            
            if(!(participant.validate() && participant.save())) {
                log.error "Failed to add conference participant : [${participant.ani}]"
                status.setRollbackOnly()
                response.sendError(HSR.SC_BAD_REQUEST, beanErrors(participant))
                return
            }

            log.debug "We've saved the participant : [${participant.ani}] id [${participant.id}]"
            
            historyService.joinedConference(participant)

            if(participant.isAdmin) {
                statWriterService.send(Stat.CONFERENCE_ADMIN_JOIN)
            } else if(participant.isPassive) {
                statWriterService.send(Stat.CONFERENCE_PASSIVE_JOIN)
            } else {
                statWriterService.send(Stat.CONFERENCE_ACTIVE_JOIN)
            }

            render(contentType: 'application/json') {
                href = "/participants/${participant.id}"
                audioResource = participant.recordedName.file.toURI().toString()
                conference = {
                    href = "/conferences/${conference.id}"
                }
                id = participant.id
                isAdmin = participant.isAdmin
                isAdminMuted = participant.isAdminMuted
                isMuted = participant.isMuted
                isPassive = participant.isPassive
                number = participant.ani
                sessionID = participant.sessionId
            }
        }
    }

    def addVoicemail = {

        def json = JSON.parse(request)
        log.debug "addVoicemail with params [${json}]"
        response.status = HSR.SC_CREATED

        Voicemail.withTransaction { status ->
            def audio = new Audio()
            audio.duration = new Duration(json.duration as Long)
            audio.transcription = json.transcription
            audio.file = new File(new URI(json.uri))

            if(!(audio.validate() && audio.save())) {
                status.setRollbackOnly()
                response.sendError(HSR.SC_BAD_REQUEST, beanErrors(audio))
                return
            }
        
            def voicemail = new Voicemail()
            voicemail.ani = json.leftBy
            voicemail.audio = audio
            voicemail.owner = User.get(getIdFromHref(json.subscriber.href))

            if(!voicemail.owner.enabled()) {
                status.setRollbackOnly()
                response.sendError(HSR.SC_FORBIDDEN)
                return
            }

            if(json.forwardedBy) {
                def user = User.get(getIdFromHref(json.forwardedBy.href))
                if(!user) {
                    status.setRollbackOnly()
                    response.sendError(HSR.SC_BAD_REQUEST, "Property [forwardedBy] with value [${json.forwardedBy}] references a non-existent entity")
                    return
                }
                voicemail.forwardedBy = user
            }

            if(!(voicemail.validate() && voicemail.save())) {
                status.setRollbackOnly()
                response.sendError(HSR.SC_BAD_REQUEST, beanErrors(voicemail))
                return
            }

            cloudToDeviceService.sendVoicemailSync(voicemail.owner)
            messageLightService.toggle(voicemail.owner)

            if(voicemail.forwardedBy) {
                historyService.forwardedVoicemail(voicemail)
            } else {
                historyService.leftVoicemail(voicemail)
            }

            log.debug 'Gonna send some notifications, maybe'

            // if they arent using transcriptions, send them the notification immediately
            def preferences = VoicemailPreferences.findByUser(voicemail.owner)

            if(preferences && !preferences.transcribe) {
                log.debug 'Lets send some voice mail notifications'
                voicemailNotificationService.sendNewVoicemailEmail(voicemail)
                voicemailNotificationService.sendNewVoicemailSms(voicemail)

                def config = AfterHoursConfiguration.findByOrganization(voicemail.owner.organization)
                if(config && voicemail.owner.username == grailsApplication.config.com.interact.listen.afterHours.username && config.alternateNumber?.length() > 0) {
                    log.debug "Sending alternate-number page to ${config.alternateNumber}"
                    voicemailNotificationService.sendNewVoicemailSms(voicemail, config.alternateNumber, Stat.NEW_VOICEMAIL_SMS_ALTERNATE)
                }
            }

            renderVoicemailAsJson(voicemail)
        }
    }

    def androidEmailContact = {
        def user = springSecurityService.currentUser
        def contact = User.get(params.id)

        if(!contact || contact.organization != user.organization || !contact.enabled()) {
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }

        def json = [
            subscriberId: contact.id,
            name: contact.realName,
            emailAddress: contact.emailAddress,
            type: 'WORK'
        ]

        render json as JSON
    }

    def androidEmailContacts = {
        def user = springSecurityService.currentUser

        params.offset = params['_first'] ?: 0
        params.max = params['_max'] ?: 100

        def list = User.findAllByOrganizationAndEnabled(user.organization, true, params)
        def total = User.countByOrganizationAndEnabled(user.organization, true)

        def results = []
        list.each { u ->
            results << [
                subscriberId: u.id,
                name: u.realName,
                emailAddress: u.emailAddress,
                type: 'WORK'
            ]
        }
        def count = results.size()

        render(contentType: 'application/json') {
            delegate.count = count
            delegate.total = total
            delegate.results = results
            if(count < total) {
                if(params.offset + count < total) {
                    next = "/emailContacts?_first=${params.offset + count}&_max=${params.max}"
                }
            }
        }
    }

    def androidGetDeviceRegistration = {
        def user = springSecurityService.currentUser
        def deviceType = DeviceRegistration.DeviceType.valueOf(params.deviceType)

        def device = DeviceRegistration.findWhere(user: user, deviceType: deviceType, deviceId: params.deviceId)
        if(!device) {
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }

        String token = device?.registrationToken ?: ''
        def json = [
            enabled: String.valueOf(googleAuthService.enabled()),
            account: googleAuthService.getUsername(),
            registrationToken: token,
            registeredTypes: device.registeredTypes.collect { it.name() }
        ]

        render json as JSON
    }

    def androidNumberContact = {
        def user = springSecurityService.currentUser
        def number = PhoneNumber.get(params.id)

        if(!number || number.owner.organization != user.organization || !number.owner.enabled()) {
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }

        def json = [
            id: number.id,
            subscriberId: number.owner.id,
            name: number.owner.realName,
            number: number.number,
            type: number.type()
        ]

        render json as JSON
    }

    def androidNumberContacts = {
        def user = springSecurityService.currentUser

        params.offset = params['_first'] ?: 0
        params.max = params['_max'] ?: 100

        // FIXME untested, not sure if isPublic will work on the parent class
        // (isPublic is defined on MobileNumber, which extends PhoneNumber)
        def list = PhoneNumber.createCriteria().list(params) {
            eq('isPublic', true)
            owner {
                eq('organization', user.organization)
                eq('enabled', true)
            }
        }

        def total = PhoneNumber.createCriteria().get {
            projections {
                count('id')
            }
            eq('isPublic', true)
            owner {
                eq('organization', user.organization)
                eq('enabled', true)
            }
        }

        def results = []
        list.each { number ->
            results << [
                id: number.id,
                subscriberId: number.owner.id,
                name: number.owner.realName,
                number: number.number,
                type: number.type()
            ]
        }
        def count = results.size()

        render(contentType: 'application/json') {
            delegate.count = results.size()
            delegate.total = total
            delegate.results = results
            if(count < total) {
                if(params.offset + count < total) {
                    next = "/numberContacts?_first=${params.offset + count}&_max=${params.max}"
                }
            }
        }
    }

    def androidUpdateDeviceRegistration = {
        def user = springSecurityService.currentUser

        def json = JSON.parse(request)

        def deviceType = DeviceRegistration.DeviceType.ANDROID
        if(json.containsKey('deviceType')) {
            deviceType = DeviceRegistration.DeviceType.valueOf(json.deviceType)
        }
        def token = json.isNull('registrationToken') ? null : json.registrationToken

        def disableTypes = []
        def enableTypes = []

        if(!json.containsKey('registerTypes') && !json.containsKey('unregisterTypes')) {
            if(!token) {
                response.sendError(HSR.SC_BAD_REQUEST, 'Registration token required if not adjusting types')
                return
            }

            if(token.length() == 0) {
                disableTypes << DeviceRegistration.RegisteredType.VOICEMAIL
            } else {
                enableTypes << DeviceRegistration.RegisteredType.VOICEMAIL
            }
        } else {
            json.registerTypes.each {
                enableTypes << DeviceRegistration.RegisteredType.valueOf(it)
            }
            json.unregisterTypes.each {
                disableTypes << DeviceRegistration.RegisteredType.valueOf(it)
            }
        }

        def device = DeviceRegistration.findWhere(user: user, deviceId: json.deviceId)
        if(!device) {
            device = new DeviceRegistration()
        }

        device.user = user
        device.deviceId = json.deviceId
        device.deviceType = deviceType
        device.registrationToken = token

        enableTypes.each {
            device.addToRegisteredTypes(it)
        }
        disableTypes.each {
            device.removeFromRegisteredTypes(it)
        }
        
        if(!(device.validate() && device.save())) {
            response.sendError(HSR.SC_BAD_REQUEST, beanErrors(device))
            return
        }

        response.status = HSR.SC_OK
        response.flushBuffer()
    }

    def androidVoicemailDownload = {
        if(!params.id) {
            response.sendError(HSR.SC_BAD_REQUEST, "Missing required parameter [id]")
            return
        }

        def voicemail = Voicemail.get(params.id)
        if(!voicemail) {
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }

        def user = springSecurityService.currentUser
        if(voicemail.owner != user) {
            response.sendError(HSR.SC_UNAUTHORIZED)
            return
        }

        boolean mp3 = request.getHeader('Accept')?.equals('audio/mpeg') ? true : false
        audioDownloadService.download(voicemail.audio, response, mp3)
        historyService.downloadedVoicemail(voicemail)
    }

    def canAccessFeature = {
        def feature = ListenFeature.valueOf(params.feature)

        if(!params.organization) {
            response.sendError(HSR.SC_BAD_REQUEST, 'Missing required parameter [organization]')
            return
        }
        
        def organization = Organization.get(getIdFromHref(params.organization))
        if(!organization) {
            response.sendError(HSR.SC_BAD_REQUEST, "Organization not found with href [${params.organization}]")
            return
        }

        if(!organization.enabled) {
            response.sendError(HSR.SC_FORBIDDEN)
            return
        }

        render(contentType: 'application/json') {             
            canAccess = licenseService.canAccess(feature, organization)
        }
    }

    def canDial = {
        log.debug("canDial invoked with params [${params}]")

        boolean canDial = false;
        String outboundCallerId = "";

        def user = User.get(getIdFromHref(params.subscriber))
        if(!user?.enabled()) {
            response.sendError(HSR.SC_FORBIDDEN)
            return
        }

        def destination = params.destination
        if(!destination) {
            response.sendError(HSR.SC_BAD_REQUEST, 'Missing required parameter [destination]')
            return
        }

        Organization callersOrg = user.organization;

        //If this is to an "extension", make sure it's in their org
        if(destination.size() < 10)
        {
            //Make sure there's an extension for this number
	        // Check if extension is orgId+extensionLength
	        def length = callersOrg.id.toString().length() + callersOrg.extLength

	        if (destination.length() == length)
	        {
				destination = destination.substring(callersOrg.id.toString().length())
	        }

            List<PhoneNumber> list = PhoneNumber.createCriteria().list() {
                eq('number', destination)
                owner {
                    eq('organization', callersOrg)
                }
            }

            if(list != null && list.size() == 1)
            {
                PhoneNumber dest = list.get(0);
                log.debug("User is calling [" + dest.owner.getRealName() + "]");
                canDial = true;
            }
            else
            {
                //No extension
                log.debug("No extension[" + destination + "] in org[" + callersOrg.id + "]");
                canDial = false;
            }
        }
        else
        {
            outboundCallerId = callRoutingService.determineOutboundCallId(user);
            log.debug "Returned value from outbound callid check [${outboundCallerId}]";
            canDial = user.canDial(destination);
            log.debug("CanDial: " + canDial);
        }

        log.debug("Returning canDial[" + canDial + "], outboundCallid[" + outboundCallerId + "].");
        render(contentType: 'application/json') {
            delegate.canDial = canDial
            delegate.outboundCallid = outboundCallerId
        }
    }

    def deleteFindMeNumber = {
        def findMeNumber = FindMeNumber.get(params.id)
        if(!findMeNumber) {
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }

        if(!findMeNumber.user.enabled()) {
            response.sendError(HSR.SC_FORBIDDEN)
            return
        }

        findMeNumber.delete()
        historyService.changedFindMeNumbers(findMeNumber.user)
        
        response.flushBuffer()
    }

    def deleteParticipant = {
        log.debug "delete conference participant params : [${params}]"
        def participant = Participant.get(params.id)
        if(!participant) {
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }

        if(!participant.conference.owner.enabled()) {
            response.sendError(HSR.SC_FORBIDDEN)
            return
        }

        conferenceService.dropCaller(participant)

        def partsRemaining = conferenceService.confParticipantsRemaining(participant.conference)
        log.debug("Conference id after [${participant?.conference?.id}] has part count[${partsRemaining}]")

        response.flushBuffer()
    }

//    @Secured(['ROLE_SPOT_API', 'ROLE_VOICEMAIL_USER'])
    def deleteVoicemail = {
        def voicemail = Voicemail.get(params.id)
        if(!voicemail) {
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }

        try {
            // if this is an Android user, make sure that they are only accessing their own voicemails
            def current = springSecurityService.currentUser
            if(current && !current.hasRole('ROLE_SPOT_API') && current.id != user.id) {
                response.sendError(HSR.SC_UNAUTHORIZED)
                return
            }
        } catch(MissingPropertyException e) {
            // ignore, this is an API user
            log.debug "MissingPropertyException in deleteVoicemail API, probably an API user"
        }

        if(!voicemail.owner.enabled()) {
            response.sendError(HSR.SC_FORBIDDEN)
            return
        }

        inboxMessageService.delete(voicemail)
        response.flushBuffer()
    }

    // for a given number, determines what number should *actually* be dialed
    // (considering find me configurations, forwarding, outdialing restrictions, etc.)
    def dial =
    {
        log.debug "dial request[${request}] with params[${params}]"
        // TODO this does not consider find me configurations yet, since
        // find me has not yet been migrated. after migrating, add find me lookups

        if(!params.destination) {
            response.sendError(HSR.SC_BAD_REQUEST, 'Missing required parameter [destination]')
            return
        }

        if(!params.organization) {
            response.sendError(HSR.SC_BAD_REQUEST, 'Missing required parameter [organization]')
            return
        }

        def destination = params.destination
        def organization = Organization.get(getIdFromHref(params.organization))
        if(!organization) {
            response.sendError(HSR.SC_BAD_REQUEST, "organization not found with id [${getIdFromHref(params.organization)}]")
            return
        }

        if(!organization.enabled) {
            response.sendError(HSR.SC_BAD_REQUEST)
            return
        }

        if(destination.size() == (organization.extLength + organization.id.toString().size()))
        {
            String numberBefore = destination;
            destination = destination.substring(organization.id.toString().size());
            log.debug("Set destination to " + destination + " from " + numberBefore);
        }

        def userPhoneNumber = PhoneNumber.createCriteria().get {
            eq('number', destination)
            owner {
                eq('organization', organization)
            }
        }

        User user = userPhoneNumber?.owner

        if(!user) {
            response.sendError(HSR.SC_BAD_REQUEST, "Subscriber not found with phone number [${destination}]")
            return
        }

        if(!user.enabled()) {
            response.sendError(HSR.SC_FORBIDDEN)
            return
        }

        def groups = FindMeNumber.findAllByUserGroupedByPriority(user, false)

        def isExpired = true
        def findMePreference = FindMePreferences.findByUser(user)
        if(findMePreference)
        {
            LocalDateTime expires = findMePreference.expires.toLocalDateTime()
            if(expires.isAfter(new LocalDateTime()))
            {
                isExpired = false
            }
        }

        if(groups.size() == 0 || isExpired)
        {
            groups.clear()

            def findMeNumber = [:]
            findMeNumber.number = destination
            findMeNumber.duration = 25
            findMeNumber.enabled = true

            if(userPhoneNumber?.instanceOf(Extension))
            {
                Extension destinationExtension = (Extension)userPhoneNumber;
                if(destinationExtension.forwardedTo && userPhoneNumber.owner.canDial(destinationExtension.forwardedTo))
                {
                    findMeNumber.number = destinationExtension.forwardedTo;
                }

                findMeNumber.ip =  destinationExtension.sipPhone.ip;
                findMeNumber.number = destinationExtension.sipPhone.phoneUserId;
                log.debug("Set dial number extension to " + findMeNumber.number);
                findMeNumber.type = "extension";
            }
            else
            {
                findMeNumber.type = "external";
            }

            def findMeNumbers = []
            findMeNumbers.add(findMeNumber)
            groups.add(findMeNumbers)
        }
        else
        {
            def tempGroups = groups.clone()

            groups.each {entry ->
                def updatedNumbers = []
                entry.each
                {
                    def updatedNumber = [:]
                    updatedNumber.number = it.number
                    updatedNumber.duration = it.dialDuration
                    updatedNumber.enabled = it.isEnabled

                    PhoneNumber findMePhoneNumber = PhoneNumber.createCriteria().get {
                        eq('number', updatedNumber.number)
                        owner {
                            eq('organization', organization)
                        }
                    }

                    if(findMePhoneNumber?.instanceOf(Extension))
                    {
                        Extension findMeNumberAsExtension = (Extension)findMePhoneNumber;

                        if(findMeNumberAsExtension.forwardedTo
                            && findMePhoneNumber.owner.canDial(findMeNumberAsExtension.forwardedTo))
                        {
                            updatedNumber.number = findMeNumberAsExtension.forwardedTo;
                        }

                        updatedNumber.ip =  findMeNumberAsExtension.sipPhone.ip;
                        updatedNumber.type = "findme_extension";
                        updatedNumber.number = findMeNumberAsExtension.sipPhone.phoneUserId;
                        log.debug("Set updated number extension to " + updatedNumber.number);
                    }
                    else
                    {
                        updatedNumber.type = "findme_external";
                    }

                    if(user.canDial(updatedNumber.number))
                    {
                        updatedNumbers.add(updatedNumber)
                    }
                }

                if(updatedNumbers.size() > 0)
                {
                    //tempGroups currently has FindMeNumbers. Remove and add in the map version with less info
                    tempGroups.remove(entry);
                    tempGroups.add(updatedNumbers);
                }
                else
                {
                    tempGroups.remove(entry);
                }
            }

            groups = tempGroups
        }

        if(groups.size() == 0)
        {
            def findMeNumber = [:]
            findMeNumber.number = destination;
            findMeNumber.duration = 25
            findMeNumber.enabled = true

            def findMeNumbers = []
            findMeNumbers.add(findMeNumber)
            groups.add(findMeNumbers)
        }

        log.debug("Dial return[" + groups + "]");

        render(contentType: 'application/json') {
            groups
        }
    }

    // looks up the appropriate DNIS mapping/route for the provided number
    def dnisLookup = {
        def number = params.number
        if(!number || number.trim() == '') {
            response.sendError(HSR.SC_BAD_REQUEST, 'Missing required parameter [number]')
            return
        }

        def id = getIdFromHref(params.organization)
        def mappings = [:]
        if(id) {
            def organization = Organization.get(id)
            NumberRoute.findAllByOrganizationAndType(organization, NumberRoute.Type.INTERNAL).each {
                mappings.put(it.pattern, it)
            }
        } else {
            NumberRoute.findAllByType(NumberRoute.Type.EXTERNAL).each {
                mappings.put(it.pattern, it)
            }
        }

        def matcher = new WildcardNumberMatcher()
        def mapping = matcher.findMatch(number, mappings)
        if(mapping == null) {
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }

        render(contentType: 'application/json') {
            application = mapping.destination
            organization = '/organizations/' + mapping.organization.id
        }
    }

    def getAfterHoursSubscriber = {
        if(!params.organization) {
            response.sendError(HSR.SC_BAD_REQUEST, "Missing required parameter [organization]")
            log.warn 'Missing required parameter [organization]'
            return
        }

        def organization = Organization.get(getIdFromHref(params.organization))
        if(!organization) {
            response.sendError(HSR.SC_BAD_REQUEST, "Organization not found with href [${params.organization}]")
            log.warn 'Organization not found with href [${params.organization}]'
            return
        }

        if(!organization.enabled) {
            response.sendError(HSR.SC_FORBIDDEN)
            log.warn "Organization not enabled"
            return
        }

        def afterHoursConfig = AfterHoursConfiguration.findByOrganization(organization)
        if(!afterHoursConfig) {
            response.sendError(HSR.SC_NOT_FOUND)
            log.warn "After hours not configured"
            return
        }

        def subscriber = afterHoursConfig.mobilePhone?.owner
        if(!subscriber) {
            log.warn "Didn't find after hours user based upon mobile"
            def afterHoursUser = grailsApplication.config.com.interact.listen.afterHours.username
            log.debug "Looking for after hours user based upon configured username[${afterHoursUser}]"
            subscriber = User.findByUsername(afterHoursUser)
            if(!subscriber) {
                response.sendError(HSR.SC_NOT_FOUND)
                log.warn "After hours subscriber is not found"
                return
            }
        }
        
        if(!subscriber.enabled()) {
            response.sendError(HSR.SC_FORBIDDEN)
            log.warn "After hours subscriber is not enabled"
            return
        }

        render(contentType: 'application/json') {
            href = '/subscribers/' + subscriber.id
        }
    }

    def getConference = {
        log.debug("getConference with params [${params}]")
        def conference = Conference.get(params.id)
        if(!conference) {
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }

        if(!conference.owner.enabled()) {
            response.sendError(HSR.SC_FORBIDDEN)
            return
        }

        def entryToneCfg = grailsApplication.config.com.interact.listen.conference.entryTone?.toBoolean()
        def exitToneCfg = grailsApplication.config.com.interact.listen.conference.exitTone?.toBoolean()
        log.debug("getConference entryTone [${entryToneCfg}] exitTone [${exitToneCfg}]")

        def formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")

        render(contentType: 'application/json') {
            href = '/conferences/' + conference.id
            id = conference.id
            arcadeId = (conference.arcadeId == '' ? null : conference.arcadeId)
            description = conference.description
            isRecording = conference.isRecording
            isStarted = conference.isStarted
            startTime = (conference.startTime ? formatter.print(conference.startTime) : null)
            recordingSessionId = (conference.recordingSessionId == '' ? null : conference.recordingSessionId)
            subscriber = {
                href = '/subscribers/' + conference.owner.id
            }
            entryTone = entryToneCfg
            exitTone = exitToneCfg
        }
    }

    def getEnabledFeatureStatus = {
        if(!params.organization) {
            response.sendError(HSR.SC_BAD_REQUEST, "Missing required parameter [organization]")
            return
        }

        def organization = Organization.get(getIdFromHref(params.organization))
        if(!organization) {
            response.sendError(HSR.SC_BAD_REQUEST, "Organization not found with href [${params.organization}]")
            return
        }

        if(!organization.enabled) {
            response.sendError(HSR.SC_FORBIDDEN)
            return
        }

        def featureStatusMap = [:]
        licenseService.licensableFeatures().each {
            featureStatusMap.put(it.toString(), licenseService.canAccess(it, organization))
        }

        render(contentType: 'application/json') {
            featureStatusMap
        }
    }

    def getParticipants = {
        log.debug "getParticipants request with params [${params}]"
        if(!params.conference) {
            response.sendError(HSR.SC_BAD_REQUEST, 'Missing required parameter [conference]')
            return
        }

        def conference = Conference.get(getIdFromHref(params.conference))
        if(!conference) {
            response.sendError(HSR.SC_BAD_REQUEST, "Property [conference] with value [${params.conference}] references a non-existent entity")
            return
        }

        if(!conference.owner.enabled()) {
            response.sendError(HSR.SC_FORBIDDEN)
            return
        }

        params.offset = params['_first'] ?: 0
        params.max = params['_max'] ?: 5
        params.fields = params['_fields']
        def participants = Participant.createCriteria().list(params) {
            eq('conference', conference)
        }
        def total = Participant.createCriteria().get {
            projections {
                count('id')
            }
            eq('conference', conference)
        }

        def results = participants.inject([]) { list, p ->
            def participant = [:]
            participant.put("href", "/participants/${p.id}")
            participant.put("isAdmin", p.isAdmin)
            participant.put("isPassive", p.isPassive)
            participant.put("id", p.id)
            participant.put("sessionID", p.sessionId)
            participant.put("audioResource", p.recordedName.file.toURI().toString())
            list.add(participant)
            return list
        }
        
        render(contentType: 'application/json') {
            href = "/participants?_first=${params.offset}&_max=${params.max}${params.fields ? "&_fields=${params.fields}" : ''}&conference=/conferences/${conference.id}"
            count = participants.size()
            delegate.total = total
            delegate.results = results
        }
    }

    def getPhoneNumber = {
        log.debug "getPhoneNumber request with params [${params}]"
        def phoneNumber = PhoneNumber.get(params.id)
        if(!phoneNumber) {
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }

        if(!phoneNumber.owner.enabled()) {
            response.sendError(HSR.SC_FORBIDDEN)
            return
        }

        def result = [
            href: "/accessNumbers/${phoneNumber.id}",
            id: phoneNumber.id,
            number: phoneNumber.number,
            type: phoneNumber.type(),
            subscriber: "/subscribers/${phoneNumber.owner.id}",
            ip: (phoneNumber.instanceOf(Extension) ? phoneNumber?.sipPhone?.ip : '')
        ]

        if((phoneNumber.instanceOf(Extension)) || (phoneNumber.instanceOf(DirectInwardDialNumber))) {
            result.greetingLocation = phoneNumber?.greeting?.file?.toURI()?.toString() ?: ''
            result.forwardedTo = phoneNumber.forwardedTo ?: ''
            result.publicNumber = true
        } else if(phoneNumber.instanceOf(DirectMessageNumber)) {
            result.forwardedTo = ''
            result.greetingLocation = phoneNumber?.greeting?.file?.toURI()?.toString() ?: ''
            result.publicNumber = true
        } else if((phoneNumber.instanceOf(MobilePhone)) || (phoneNumber.instanceOf(OtherPhone))) {
            result.forwardedTo = ''
            result.greetingLocation = ''
            result.publicNumber = phoneNumber.isPublic
        } else {
            result.forwardedTo = ''
            result.greetingLocation = ''
            result.publicNumber = phoneNumber.isPublic
        }

        render(result as JSON)
    }

    def getOrganization = {
        log.debug("getOrganization with params [${params}]")
        if(!params.extension) {
            response.sendError(HSR.SC_BAD_REQUEST, 'Missing required parameter [number]')
            return
        }

        def pin = Pin.findByNumber(params.number)
        if(!pin) {
            response.sendError(HSR.SC_NOT_FOUND, "Pin not found with number [${params.number}]")
            return
        }

        if(!pin.conference.owner.enabled()) {
            response.sendError(HSR.SC_FORBIDDEN)
            return
        }

        render(contentType: 'application/json') {
            href = '/pins/' + pin.id
            conference = {
                href = '/conferences/' + pin.conference.id
            }
            id = pin.id
            number = pin.number
            type = pin.pinType.toString()
        }
    }

    def getPin = {
        log.debug("getPin with params [${params}]")
        if(!params.number) {
            response.sendError(HSR.SC_BAD_REQUEST, 'Missing required parameter [number]')
            return
        }

        def pin = Pin.findByNumber(params.number)
        if(!pin) {
            response.sendError(HSR.SC_NOT_FOUND, "Pin not found with number [${params.number}]")
            return
        }

        if(!pin.conference.owner.enabled()) {
            response.sendError(HSR.SC_FORBIDDEN)
            return
        }

        render(contentType: 'application/json') {
            href = '/pins/' + pin.id
            conference = {
                href = '/conferences/' + pin.conference.id
            }
            id = pin.id
            number = pin.number
            type = pin.pinType.toString()
        }
    }

    def getUser = {
        log.debug "getUser request with params [${params}]"
        def user = User.get(params.id)
        if(!user) {
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }

        if(!user.enabled()) {
            response.sendError(HSR.SC_FORBIDDEN)
            return
        }

        def prefs = VoicemailPreferences.findByUser(user)
        render(contentType: 'application/json') {
            href = '/subscribers/' + user.id
            realName = user.realName
            voicemailPin = prefs?.passcode
            voicemailPlaybackOrder = prefs?.playbackOrder?.name()
            isSubscribedToTranscription = prefs?.transcribe
        }
    }

    def getVoicemail = {
        def voicemail = Voicemail.get(params.id)
        if(!voicemail) {
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }

        if(!voicemail.owner.enabled()) {
            response.sendError(HSR.SC_FORBIDDEN)
            return
        }

        renderVoicemailAsJson(voicemail)
    }

    def listFindMeConfiguration = {
        if(!params.subscriber) {
            response.sendError(HSR.SC_BAD_REQUEST, 'Missing required parameter [subscriber]')
            return
        }

        def user = User.get(getIdFromHref(params.subscriber))
        if(!user) {
            response.sendError(HSR.SC_BAD_REQUEST, "Property [subscriber] with value [${params.subscriber}] references a non-existent entity")
            return
        }

        if(!user.enabled()) {
            response.sendError(HSR.SC_FORBIDDEN)
            return
        }

        def findMePreferences = FindMePreferences.findByUser(user)
        def groups = []
        FindMeNumber.findAllByUserGroupedByPriority(user, true).each { group ->
            def newGroup = []
            group.each {
                newGroup << [
                    href: "/findMeNumbers/${it.id}",
                    number: it.number,
                    isEnabled: it.isEnabled
                ]
            }
            groups << newGroup
        }
        def findMeConfiguration = [
            exists: findMePreferences ? true : false,
            expired: !findMePreferences?.isActive(),
            results: groups
        ]
        render findMeConfiguration as JSON
    }

    def listPhoneNumbers = {
        log.debug "listPhoneNumbers with params [${params}]"
        if((params.number && !params.organization) && !params.subscriber && !params.organization) {
            response.sendError(HSR.SC_BAD_REQUEST, 'Missing required parameters [number] and [organization] or [subscriber] alone or [organization] alone')
            return
        }

        def user
        if(params.subscriber) {
            user = User.get(getIdFromHref(params.subscriber))
            if(!user) {
                response.sendError(HSR.SC_BAD_REQUEST, "Parameter [subscriber] with value [${params.subscriber}] references a non-existent entity")
                return
            } else {
                log.debug "listPhoneNumbers found user [${user?.id}]"
            }
        }

        def organization = Organization.get(getIdFromHref(params.organization))
        if(params.organization && !organization) {
            response.sendError(HSR.SC_BAD_REQUEST, "Organization not found with href [${params.organization}]")
            return
        } else {
            log.debug "listPhoneNumbers using organization [${organization?.id}]"
        }
    

        if(organization && !organization.enabled) {
            response.sendError(HSR.SC_FORBIDDEN)
            return
        }

        if(user && !user.enabled()) {
            response.sendError(HSR.SC_FORBIDDEN)
            return
        }

        def list = PhoneNumber.createCriteria().list {
            if(params.number) {
                eq('number', params.number)
            }
            if(organization) {
                owner {
                    eq('organization', organization)
                    eq('enabled', true)
                }
            } else {
                owner {
                    eq('enabled', true)
                }
            }
            if(user) {
                eq('owner', user)
            }
        }
        def total = list.size()
        log.debug "listPhoneNumber list size [${total}]"
        list.each { ph ->
            log.debug "Found number [${ph.number}]"
        }
        
        def results = list.collect {
            return [
                'subscriber': "/subscribers/${it.owner.id}",
                'href': "/accessNumbers/${it.id}",
                'number': it.number,
                'type': it.type(),
                'ip': (it.instanceOf(Extension) ? it?.sipPhone?.ip : '')
            ]
        }

        def json = [
            href: '/accessNumbers?_fields=subscriber,number' + (params.number ? "&number=${params.number}" : '') + (user ? "&subscriber=/subscribers/${user.id}" : '') + (params.organization ? "&organization=${params.organization}" : ''),
            count: results.size(),
            total: total,
            results: results
        ]

        //log.debug "list PhoneNumber json [${(json as JSON).toString()}]"
        render json as JSON
    }

//    @Secured(['ROLE_VOICEMAIL_USER', 'ROLE_SPOT_API'])
    def listUsers = {
        def organization = Organization.get(getIdFromHref(params.organization))

        if(!organization) {
            def user = springSecurityService.currentUser
            if(!params.username || user.username != params.username) {
                response.sendError(HSR.SC_UNAUTHORIZED)
                return
            }
            organization = user.organization
        }

        if(!organization.enabled) {
            response.sendError(HSR.SC_FORBIDDEN)
            return
        }

        def users = User.createCriteria().list([offset: 0, max: 100]) {
            if(params.containsKey('username')) {
                eq('username', params.username)
                eq('enabled', true)
            }
            eq('organization', organization)
        }

        def total = User.createCriteria().get {
            projections {
                count('id')
            }
            if(params.containsKey('username')) {
                eq('username', params.username)
                eq('enabled', true)
            }
            eq('organization', organization)
        }

        def results = users.collect { u ->
            [
                href: "/subscribers/${u.id}",
                id: u.id,
                voicemailPlaybackOrder: VoicemailPreferences.findByUser(u)?.playbackOrder.name()
            ]
        }

        render(contentType: 'application/json') {
            delegate.total = total
            delegate.results = results
        }
    }

//    @Secured(['ROLE_SPOT_API', 'ROLE_VOICEMAIL_USER'])
    def listVoicemails = {
        // TODO it would be ideal if we could separate this out into two different APIs,
        //   one for SPOT systems and another for the Android app

        if(!params.subscriber) {
            log.warn 'Missing required parameter [subscriber]'
            response.sendError(HSR.SC_BAD_REQUEST, 'Missing required parameter [subscriber]')
            return
        }

        def user = User.get(getIdFromHref(params.subscriber))
        if(!user) {
            log.warn "Property [subscriber] with value [${params.subscriber}] references a non-existent entity"
            response.sendError(HSR.SC_BAD_REQUEST, "Property [subscriber] with value [${params.subscriber}] references a non-existent entity")
            return
        }

        try {
            // if this is an Android user, make sure that they are only accessing their own voicemails
            def current = springSecurityService.currentUser
            if(current && !current.hasRole('ROLE_SPOT_API') && current.id != user.id) {
                log.warn "Android user is unauthorized; current.id [${current.id}], user.id [${user.id}], hasRole [${user.hasRole('ROLE_SPOT_API')}]"
                response.sendError(HSR.SC_UNAUTHORIZED)
                return
            }
        } catch(MissingPropertyException e) {
            // ignore, this is an API user
            log.debug "MissingPropertyException in listVoicemails API, probably an API user"
        }

        if(!user.enabled()) {
            response.sendError(HSR.SC_FORBIDDEN)
            return
        }

        params.offset = params.int('_first') ?: 0
        params.max = params.int('_max') ?: 100
        params.sort = params['_sortBy'] ?: 'dateCreated'
        params.order = params['_sortOrder'] ? (params['_sortOrder'] == 'ASCENDING' ? 'asc' : 'desc') : 'asc'
        def voicemails = Voicemail.createCriteria().list(params) {
            eq('owner', user)
            if(params.containsKey('isNew')) {
                eq('isNew', params.boolean('isNew'))
            }
        }
        def total = Voicemail.createCriteria().get {
            projections {
                count('id')
            }
            eq('owner', user)
            if(params.containsKey('isNew')) {
                eq('isNew', params.boolean('isNew'))
            }
        }
        def count = voicemails.size()

        def formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")

        // TODO hasNotified?
        def results = voicemails.collect() { v ->
            [
                href: "/voicemails/${v.id}",
                id: v.id,
                isNew: v.isNew,
                leftBy: v.ani,
                leftByName: v.from(),
                description: v.audio.description,
                dateCreated: formatter.print(v.dateCreated),
                duration: v.audio.duration.millis,
                transcription: v.audio.transcription
            ]
        }

        def p = [:]
        if(params.containsKey('subscriber')) {
            p['subscriber'] = params.subscriber
        }
        if(params.containsKey('isNew')) {
            p['isNew'] = params.isNew
        }
        def pstring = p.inject('') { str, e -> str += "&${e.key}=${URLEncoder.encode(e.value, 'UTF-8')}" }

        log.debug "Returning [${count}] of [${total}] voicemails for user [${user}]"

        def json = [
            href: "/voicemails?_first=${params.offset}&_max=${params.max}&_sortBy=${params.sort}&_sortOrder=${params.order == 'asc' ? 'ASCENDING' : 'DESCENDING'}${pstring}",
            count: count,
            total: total,
            results: results
        ]
        if(count < total) {
            if((params.offset + count) < total) {
                json.put('next', "/voicemails?_first=${params.offset + count}&_max=${params.max}&_sortBy=${params.sort}&_sortOrder=${params.order == 'asc' ? 'ASCENDING' : 'DESCENDING'}${pstring}")
            }
        }
        log.debug "Rendering JSON: ${json}"
        render json as JSON
    }

    // given an access number and an organization, looks up the user owning the number
    def lookupAccessNumber = {
        log.debug "lookupAccessNumber request with params [${params}]"
        String number = params.number
        if(!number) {
            response.sendError(HSR.SC_BAD_REQUEST, 'Missing required parameter [number]')
            return
        }

        def organization = Organization.get(getIdFromHref(params.organization))
        if(!organization) {
            response.sendError(HSR.SC_BAD_REQUEST, "Organization not found with href [${params.organization}]")
            return
        }

        if(!organization.enabled) {
            response.sendError(HSR.SC_FORBIDDEN)
            return
        }

        if(number.size() == organization.extLength + organization.id.toString().size())
        {
            if(number.startsWith(organization.id.toString()))
            {
                String numberBefore = number;
                number = number.substring(organization.id.toString().size());
                log.debug("Set number to " + number + " from " + numberBefore);
            }
        }

        def phoneNumbers = PhoneNumber.withCriteria() {
            eq('number', number)
            owner {
                eq('organization', organization)
            }
        }

        def phoneNumber = phoneNumbers.size() > 0 ? phoneNumbers[0] : null
        if(phoneNumber && !phoneNumber.owner.enabled())
        {
            log.debug "Phone number [${number}] is not enabled"
            response.sendError(HSR.SC_FORBIDDEN)
            return
        }
        else if (!phoneNumber)
        {
            log.info "Phone number [${number}] is not configured"
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }
        else
        {
            if(log.isDebugEnabled())
            {
                log.debug("Returning phoneNumber[" + phoneNumber.number + "] for user[" +
                        phoneNumber.owner.username + "]");
            }
        }

        render(contentType: 'application/json') {
            count = (phoneNumber ? 1 : 0)
            if(phoneNumber) {
                results = [{
                    href = '/accessNumbers/' + phoneNumber.id
                    subscriber = {
                        href = '/subscribers/' + phoneNumber.owner.id
                    }
                }]
            } else {
                results = []
            }
        }
    }

    def menuAction = {
        def id = params.menuId
        if(!id)
        {
            // this is a request for the entry menu for a specific organization
            if(!params.organization)
            {
                log.warn("Menu Action request missing required parameter [organization]");
                response.sendError(HSR.SC_BAD_REQUEST, 'Missing required parameter [organization]')
                return
            }

            def organization = Organization.get(getIdFromHref(params.organization))
            def entry = menuLocatorService.findEntryMenu(organization)
            if(!entry.menu)
            {
                log.warn("Menu not found for organization[" + organization.id + "]");
                response.sendError(HSR.SC_NOT_FOUND)
                return
            }

            def command = entry.menu.toIvrCommand(organization.attendantPromptDirectory(), '', '')
            if(entry.override)
            {
                command.args.audioFile = command.args.audioFile.substring(0, command.args.audioFile.lastIndexOf('/') + 1) + entry.override.optionsPrompt[0]
                log.debug "Overrode default prompt with [${command.args.audioFile}]"
            }

            log.debug "MenuAction returning doAction [${command}]"
            render(command as JSON)
            return
        }

        def menu = Menu.get(id)
        if(!menu)
        {
            log.warn("Menu Id not found[" + id + "]");
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }

        // only need keysPressed param if a menu id was provided
        def doAction;
        def keysPressed = params.keysPressed
        if(!keysPressed || keysPressed.trim() == '' || keysPressed.trim().equals('TIMEOUT'))
        {
            doAction = menu.timeoutAction
        }
        else
        {
            def mappings = menu.keypressActions.inject([:]) { map, keypressAction ->
                map[keypressAction.keysPressed] = keypressAction
                return map
            }
            doAction = keypressToAction(mappings, keysPressed)
            if(!doAction)
            {
                doAction = menu.defaultAction
            }
        }

        String promptBefore = doAction.promptBefore;
        if(doAction.instanceOf(GoToMenuAction))
        {
            def group = MenuGroup.findByName(doAction.destinationMenuGroupName)
            doAction = Menu.findByMenuGroupAndName(group, doAction.destinationMenuName)
        }
        else if(doAction.instanceOf(ReplayMenuAction))
        {
            doAction = menu
        }

        def command =
            doAction.toIvrCommand(menu.menuGroup.organization.attendantPromptDirectory(), promptBefore,
                    (String)grailsApplication.config.com.interact.listen.artifactsDirectory,
                    menu.menuGroup.organization.getId());
        log.debug "MenuAction returning doAction attendant [${command}]";
        render(command as JSON)
    }

    private def keypressToAction(def mappings, def keysPressed)
    {
        // if we have a specific mapping for the number, simply return its action
        if(mappings.containsKey(keysPressed))
        {
            return mappings[keysPressed]
        }

        // strip all of the specific mappings (we didnt find one at this point, so we are looking for a wildcarded one)
        // also take out any mappings that are not equal to the number of keys pressed, they wont match since there
        // is not a "one or more" or "zero or more" wildcard
        def wildcards = new TreeMap<String, Action>()
        mappings.each { k, v ->
            if(k.endsWith('?') && k.length() == keysPressed.length()) {
                wildcards.put(k, v)
            }
        }

        def match
        wildcards.each { k, v ->
            // if the digits of the key without the wildcard(s) equals the same number of digits (length minus number of
            // wildcards)
            // then we have a most specific match. All wilcards will match with a 0.
            def length = k.length()
            int matchedWildcards = StringUtils.countMatches(k, '?')
            int position = length - matchedWildcards

            if(k.substring(0, position) == keysPressed.substring(0, position)) {
                match = v
                return
            }
        }
        return match
    }

    // registers a SPOT system with the controller
    def register = {
        if(!params.system) {
            response.sendError(HSR.SC_BAD_REQUEST, 'Missing required parameter [system]')
            return
        }

        def system = SpotSystem.findByName(params.system)
        if(!system) {
            new SpotSystem(name: params.system).save()
        }
        response.flushBuffer()
    }

    // Supports phone SIP registration and invite requests
    def sipRegister = {
//        log.debug "sipRegister request with params"
        log.debug "sipRegister request with params [${params}]"
        def deregister = false
        def register = false
        def invite = false

        if (!params.args) {
            log.warn "sipRegister request missing param [args]"
            response.sendError(HSR.SC_BAD_REQUEST, 'Missing required parameter [args]')
            return
        }
        def json = JSON.parse(params?.args)
        if(!json.To) {
            log.warn "sipRegister request missing param [args.To]"
            response.sendError(HSR.SC_BAD_REQUEST, 'Missing required parameter [args.To]')
            return
        }

        if(!json.From) {
            log.warn "sipRegister request missing param [args.From]"
            response.sendError(HSR.SC_BAD_REQUEST, 'Missing required parameter [args.From]')
            return
        }

        if(!json.Contact) {
            log.warn "sipRegister request missing param [args.Contact]"
            response.sendError(HSR.SC_BAD_REQUEST, 'Missing required parameter [args.Contact]')
            return
        }

        if(!json.'Request-Line') {
            log.warn "sipRegister request missing param [args.Request-Line]"
            response.sendError(HSR.SC_BAD_REQUEST, 'Missing required parameter [args.Request-Line]')
            return
        }
        else
        {
            def requestLine = URLDecoder.decode(json?.'Request-Line', 'UTF-8')
            requestLine = requestLine.split(' ')[0]
            if(requestLine == 'REGISTER'){
                register = true
            } else if(requestLine == 'INVITE'){
                invite = true
            } else {
                log.error "sipRegister request param [args.Request-Line] passed unexpected value [${requestLine}]"
                response.sendError(HSR.SC_BAD_REQUEST, 'Unexpected value for [args.Request-Line]')
                return
            }
        }

        def extension = new Extension()
        extension.sipPhone = new SipPhone()

        def To = URLDecoder.decode(json?.To, 'UTF-8')

        def From = URLDecoder.decode(json?.From, 'UTF-8')

        if (register)
        {
            extension.sipPhone.realName = To.split("<")[0].replace("\"", "")
        }
        else if (invite)
        {
            extension.sipPhone.realName = From.split("<")[0].replace("\"", "")
        }

        if ((extension.sipPhone.realName.length() > 0)
                && (extension.sipPhone.realName[extension.sipPhone.realName.length() - 1] == ' '))
        {
            extension.sipPhone.realName =
                extension.sipPhone.realName.substring(0, extension.sipPhone.realName.length() - 1)
        }

        def extNumber = To.split("sip:")[1].split("@")[0]

        extension.sipPhone.expires = -1
        if (json?.Expires)
        {
            extension.sipPhone.expires = json.Expires.toInteger()
        }

        if (json?.'User-Agent')
        {
            extension.sipPhone.userAgent = URLDecoder.decode(json?.'User-Agent', 'UTF-8')
        }

        def Contact = URLDecoder.decode(json?.Contact, 'UTF-8')
        if (Contact.contains(";") || Contact.contains("@"))
        {
            def contactList = Contact.tokenize(';')

            for ( int i = 0; i < contactList.size(); i++ )
            {
                def tmpval = contactList[i];
                if (tmpval.contains('expires='))
                {
                    extension.sipPhone.expires = tmpval.split("expires=")[1].toInteger();
                }

                if (tmpval.contains('sip:') && (tmpval.contains('@')))
                {
                    if (invite)
                    {
                        // for INVITE requests, we want to take the info from contact and use it as the originating extension number
                        extension.number = tmpval.split("@")[0]?.split("sip:")[1];
                    }

                    String sipIp = tmpval.split("@")[1]?.replaceAll(/\>/, '');
                    log.debug("Using SIP IP[" + sipIp + "]");
                    extension.sipPhone.ip = sipIp;
                }
            }
        }
        else if ((Contact == '*') && (extension.sipPhone.expires == 0 )) {
            // RFC 3261 section 10.2.2
            extension.sipPhone.ip = ''
        } else {
            log.error "sipRegister request param [args.Contact] improperly formatted"
            response.sendError(HSR.SC_BAD_REQUEST, 'Improperly formatted parameter [args.Contact]')
            return
        }

        if(json?.Authorization) {
            def tmpAuthorization = URLDecoder.decode(json?.Authorization, 'UTF-8')
            if (tmpAuthorization.contains("username=")) {
                def tmpAuthorization1 = tmpAuthorization.split("username=")[1]
                if (tmpAuthorization1.contains(",")) {
                    def tmpAuth = tmpAuthorization1.split(",")[0]
                    if (tmpAuth.contains("\"")){
                        extension.sipPhone.username = tmpAuth.replaceAll("\"", "")
                    } else {
                        extension.sipPhone.username = tmpAuth
                    }
                }
            } else {
                log.error "sipRegister request param [args.Authorization] unexpected format [${tmpAuthorization}]"
            }
        } else {
            extension.sipPhone.username = null
        }

        if (extension.sipPhone.expires == "0") {
            deregister = true
            register = false
        }

        if(json?.id) {
            if (deregister || register) {
                extension.number = URLDecoder.decode(json?.id, 'UTF-8')
            }
        } else {
            log.error "sipRegister request param [args.id] missing"
            response.sendError(HSR.SC_BAD_REQUEST, 'sipRegister missing params [args.id]')
            return
        }

        if(json?.CSeq){
            def tmpCSeq = URLDecoder.decode(json?.CSeq, 'UTF-8')
            if(tmpCSeq.contains(" ")){
                extension.sipPhone.cseq = tmpCSeq.split(" ")[0].toInteger()
            } else {
                extension.sipPhone.cseq = -1
            }
        }

        def expireSeconds = grailsApplication.config.com.interact.listen.sip.expires.toInteger()
        if ((extension.sipPhone.expires < expireSeconds) && (extension.sipPhone.expires > 0 )) {
            expireSeconds = extension.sipPhone.expires
        } else {
            extension.sipPhone.expires = expireSeconds
        }

        def regResponse
        if (deregister) {
            regResponse = extensionService.sipDeregistration(extension)
        } else if (register) {
            regResponse = extensionService.sipRequest(extension)
        } else if (invite) {
            regResponse = extensionService.sipRequest(extension)
        } else {
            log.error "sipRegister request encountered unexpected error"
            response.sendError(HSR.SC_BAD_REQUEST, 'sipRegister failed to process request')
            return
        }

        def xmlResponse

        if ((regResponse.returnCode == HSR.SC_NOT_FOUND) && (!extension?.sipPhone?.username)) {
            xmlResponse = "Id [${extension?.number}] not found"
            response.status = HSR.SC_NOT_FOUND
        } else if ((regResponse.returnCode == HSR.SC_NOT_FOUND) && (extension?.sipPhone?.username)) {
            xmlResponse = "Id [${extension?.number}] auth username [${extension.sipPhone.username}] not found"
            response.status = HSR.SC_NOT_FOUND
        } else if ((regResponse.returnCode == HSR.SC_OK) && (!extension?.sipPhone?.username)) {
            xmlResponse = "<arcade><client id='${extension.number}' password='${regResponse.extension?.sipPhone?.password}' username='null'/><expires seconds='${expireSeconds}'/>"
            response.status = HSR.SC_OK
        } else if ((regResponse.returnCode == HSR.SC_OK) && (extension?.sipPhone?.username)) {
            xmlResponse = "<arcade><client id='${extension.number}' password='${regResponse.extension?.sipPhone?.password}' username='${extension.sipPhone.username}'/><expires seconds='${expireSeconds}'/>"
            response.status = HSR.SC_OK
        }
        else if (regResponse.returnCode == HSR.SC_UNAUTHORIZED)
        {
            response.status = HSR.SC_UNAUTHORIZED
        }
        else {
            log.error "sipRegister unexpected response code [${regResponse.returnCode}]"
            xmlResponse = "sipRegister internal server error [${regResponse.returnCode}]"
            response.status = HSR.SC_INTERNAL_SERVER_ERROR
        }

        if(xmlResponse != null)
        {
            log.debug "sipRegister request xmlResponse [${xmlResponse}]"
            def xmlResponseEncoded = URLEncoder.encode(xmlResponse, 'UTF-8')
            response << xmlResponseEncoded
        }
        //log.debug "sipRegister request xmlResponse encoded [${xmlResponseEncoded}]"

        response.flushBuffer()
    }
    // sets the SPOT system phone number (for display in notifications)
    def setPhoneNumber = {
        log.debug "setPhoneNumber request[${request} with params [${params}]"
        // TODO phone number wont get persisted, and wont be available if application is restarted
        if(!params.phoneNumber) {
            response.sendError(HSR.SC_BAD_REQUEST, 'Missing required parameter [phoneNumber]')
            return
        }

        def validProtocols = ['PSTN', 'VOIP'] as Set
        def delimiter = ';'
        def number = params.phoneNumber

        if(!number.contains(delimiter) ||
               number.split(delimiter).length != 2 ||
               !validProtocols.contains(number.split(delimiter)[0])) {
            response.sendError(HSR.SC_BAD_REQUEST, "Property [phoneNumber] must be in the format 'PROTOCOL;NUMBER', where PROTOCOL = [${validProtocols.join('|')}]")
            return
        }

        grailsApplication.config.com.interact.listen.phoneNumber = number
        response.flushBuffer()
    }

    def updateConference = {
        log.debug "updateConference request[${request}] with params [${params}]"
        def conference = Conference.get(params.id)
        if(!conference) {
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }

        if(!conference.owner.enabled()) {
            response.sendError(HSR.SC_FORBIDDEN)
            return
        }

        def json = JSON.parse(request)
        log.debug "updateConference request with request json [${json}]"

        if(json.arcadeId != conference.arcadeId) {
            conference.arcadeId = json.arcadeId
        }

        if(json.recordingSessionId != conference.recordingSessionId) {
            conference.recordingSessionId = json.recordingSessionId
        }

        if(!(conference.validate() && conference.save())) {
            log.debug "updateConference - failed to save [${conference.errors}]"
            response.sendError(HSR.SC_BAD_REQUEST)
            return
        }

        boolean success = true
        if(json.isStarted.toBoolean() && !conference.isStarted) {
            log.debug "updateConference - start conference"
            success = conferenceService.startConference(conference)
        } else if(!json.isStarted.toBoolean() && conference.isStarted) {
            log.debug "updateConference - stop conference"
            success = conferenceService.stopConference(conference)
        } else {
            // the second block above handles recording stopping when the conference stops
            // this case handles it if the conference 'isStarted' status didnt change
            if(json.isRecording.toBoolean() && !conference.isRecording) {
                log.debug "updateConference - start recording conference"
                success = conferenceService.startRecordingConference(conference)
            } else if(!json.isRecording.toBoolean() && conference.isRecording) {
                log.debug "updateConference - stop recording conference"
                success = conferenceService.stopRecordingConference(conference)
            }
        }

        if(success) {
            response.status = HSR.SC_OK
            response.flushBuffer()
        } else {
            response.sendError(HSR.SC_BAD_REQUEST)
        }
    }

    def updateFindMeNumber = {
        log.debug "updateFindMeNumber request[${request}] with params [${params}]"
        def findMeNumber = FindMeNumber.get(params.id)
        if(!findMeNumber) {
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }

        if(!findMeNumber.user.enabled()) {
            response.sendError(HSR.SC_FORBIDDEN)
            return
        }

        def json = JSON.parse(request)

        if(json.isEnabled.toBoolean() != findMeNumber.isEnabled) {
            findMeNumber.isEnabled = json.isEnabled.toBoolean()
        }

        if(findMeNumber.validate() && findMeNumber.save()) {
            historyService.changedFindMeNumbers(findMeNumber.user)
            response.status = HSR.SC_OK
            response.flushBuffer()
        } else {
            response.sendError(HSR.SC_BAD_REQUEST, beanErrors(findMeNumber))
        }
    }

    def updateFindMeExpiration = {
        def user = User.get(params.id)
        if(!user) {
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }

        if(!user.enabled()) {
            response.sendError(HSR.SC_FORBIDDEN)
            return
        }

        def findMePreferences = FindMePreferences.findByUser(user)
        def json = JSON.parse(request)
        if(json.isActivating.toBoolean()) {
            findMePreferences.expires = new DateTime().plusDays(1)
        } else {
            findMePreferences.expires = new DateTime().minusMinutes(1)
        }

        if(findMePreferences.validate() && findMePreferences.save()) {
            historyService.changedFindMeExpiration(findMePreferences)
            response.status = HSR.SC_OK
            response.flushBuffer()
        } else {
            response.sendError(HSR.SC_BAD_REQUEST, beanErrors(findMePreferences))
        }
    }

    def updateParticipant = {
        log.debug "updateParticipant request[${request}] with params [${params}]"
        def participant = Participant.get(params.id)
        if(!participant) {
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }

        def json = JSON.parse(request)

        if(json.isAdminMuted.toBoolean() != participant.isAdminMuted) {
            if(participant.isAdminMuted) {
                conferenceService.unmuteCaller(participant)
            } else {
                conferenceService.muteCaller(participant)
            }
        }

        participant.isMuted = json.isMuted.toBoolean()

        if(participant.validate() && participant.save()) {
            response.status = HSR.SC_OK
            response.flushBuffer()
        } else {
            response.sendError(HSR.SC_BAD_REQUEST, beanErrors(participant))
        }
    }

    def updatePhoneNumber = {
        log.debug "updatePhoneNumber request[${request}] with params [${params}]"
	def phoneNumber = PhoneNumber.get(params.id)

        if(!phoneNumber) {
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }

        if(!phoneNumber.owner.enabled()) {
            response.sendError(HSR.SC_FORBIDDEN)
            return
        }

        PhoneNumber.withTransaction { status ->
            def json = JSON.parse(request)
        
            if(json.subscriber?.href) {
                def user = User.get(getIdFromHref(json.subscriber.href))
                if(!user) {
                    response.sendError(HSR.SC_BAD_REQUEST, "Property [subscriber] with value [${json.subscriber}] references a non-existent entity")
                    return
                }
                phoneNumber.owner = user
            }

            if((phoneNumber.instanceOf(Extension) || phoneNumber.instanceOf(DirectInwardDialNumber) || phoneNumber.instanceOf(DirectMessageNumber)) && json.greetingLocation) {
		// Always use new greeting since grails doesn't seem to detect updates as a change to the greeting and won't save
		// TP 66683 - DWW - 2/4/15
		// hold on to this - we can't delete it until the phoneNumber has been saved
		//def oldGreeting = phoneNumber.greeting
		def greeting = new Audio(duration: new Duration(0))
                greeting.file = new File(new URI(json.greetingLocation))
                log.debug "phone number greeting [${greeting.file}]"
                    
                if(!(greeting.validate() && greeting.save(flush:true))) {
                    status.setRollbackOnly()
                    response.sendError(HSR.SC_BAD_REQUEST, beanErrors(greeting))
                    return
                }
		phoneNumber.greeting = greeting
                log.debug "We've saved greeting for user [${phoneNumber.owner}]"
            }

            if(json.number) {
                phoneNumber.number = json.number
            }

            if(phoneNumber.instanceOf(Extension) && !json.isNull('forwardedTo')) {
                phoneNumber.forwardedTo = json.forwardedTo.length() > 0 ? json.forwardedTo : null
            }

            if(phoneNumber.instanceOf(Extension)) {
                phoneNumber.extLength = phoneNumber.owner.organization.extLength
            }

            if(phoneNumber.validate() && phoneNumber.save(failonerror:true, flush:true)) {
                log.debug "We've saved the entire phone number record"
		// I can't seem to get deleting to work - it runs into some taglib issue
		// which I'm not sure how it is related - will work as a bug later - DWW
		// TP 66702
		//oldGreeting.delete()
                response.status = HSR.SC_OK
                response.flushBuffer()
            } else {
                log.error "We've failed to save phone number changes"
                status.setRollbackOnly()
                response.sendError(HSR.SC_BAD_REQUEST, beanErrors(phoneNumber))
            }

            log.debug "We've completed the update phone number method"
        }
    }

    def updateUser = {
        log.debug "updateUser request with params [${params}]"
        def user = User.get(params.id)
        if(!user) {
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }

        def json = JSON.parse(request)
        def preferences = VoicemailPreferences.findByUser(user)
        if(json.voicemailPin) {
            preferences.passcode = json.voicemailPin
        }

        if(preferences.validate() && user.validate() && preferences.save() && user.save()) {
            response.status = HSR.SC_OK
            response.flushBuffer()
        } else {
            response.sendError(HSR.SC_BAD_REQUEST, beanErrors(user) + beanErrors(preferences))
        }
    }

//    @Secured(['ROLE_SPOT_API', 'ROLE_VOICEMAIL_USER'])
    def updateVoicemail = {
        def voicemail = Voicemail.get(params.id)
        if(!voicemail) {
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }

        try {
            // if this is an Android user, make sure that they are only accessing their own voicemails
            def current = springSecurityService.currentUser
            if(current && !current.hasRole('ROLE_SPOT_API') && current.id != user.id) {
                response.sendError(HSR.SC_UNAUTHORIZED)
                return
            }
        } catch(MissingPropertyException e) {
            // ignore, this is an API user
            log.debug "MissingPropertyException in updateVoicemail API, probably an API user"
        }

        if(!voicemail.owner.enabled()) {
            response.sendError(HSR.SC_FORBIDDEN)
            return
        }

        def originalTranscription = voicemail.audio.transcription

        Voicemail.withTransaction { status ->
            def json = JSON.parse(request)

			log.debug "request[${json}]"

            def originalIsNew = voicemail.isNew
            if(json.containsKey('isNew')) {
				log.debug "Updating isNew to [${json.isNew.toBoolean()}]"
                voicemail.isNew = json.isNew.toBoolean()
            }

            if(json.transcription) {
                voicemail.audio.transcription = json.transcription
            }

            if(!json.isNull('forwardedBy')) {
                def user = User.get(getIdFromHref(json.forwardedBy.href))
                if(!user) {
                    status.setRollbackOnly()
                    response.sendError(HSR.SC_BAD_REQUEST, "Property [forwardedBy] with value [${json.forwardedBy}] references a non-existent entity")
                    return
                }
                voicemail.forwardedBy = user
            }

            if(!(voicemail.audio.validate() && voicemail.audio.save())) {
                status.setRollbackOnly()
                response.sendError(HSR.SC_BAD_REQUEST, beanErrors(voicemail.audio))
                return
            }

            if(!(voicemail.validate() && voicemail.save())) {
                status.setRollbackOnly()
                response.sendError(HSR.SC_BAD_REQUEST, beanErrors(voicemail))
                return
            }

			// CloudToDeviceService.sendVoicemailSync will throw an exception because there is a missing service(CloudToDeviceMessaging)
			try {
				cloudToDeviceService.sendVoicemailSync(voicemail.owner)
			}
			catch(Exception e) {
				log.error("Exception sending voicemail sync: " + e.getMessage(), e);
			}
            messageLightService.toggle(voicemail.owner)

            if(originalIsNew != voicemail.isNew) {
                if(voicemail.isNew) {
                    historyService.markedVoicemailNew(voicemail)
                } else {
                    historyService.markedVoicemailOld(voicemail)
                }
            }

            // only send a notification for a new message whose transcription was updated from 'Transcription Pending'
            // to something different (which means that the IVR received its transcription back and updated the voicemail
            // with it)
            if(voicemail.isNew && originalTranscription == 'Transcription Pending' && voicemail.audio.transcription != 'Transcription Pending') {
                voicemailNotificationService.sendNewVoicemailEmail(voicemail)
                voicemailNotificationService.sendNewVoicemailSms(voicemail)
            }
        }

	
        renderVoicemailAsJson(voicemail)
        // response.flushBuffer()
    }

    def getTranscriptionConfiguration = {
        def organization = Organization.get(getIdFromHref(params.organization))
        if(!organization) {
            response.sendError(HSR.SC_BAD_REQUEST, "Organization not found with href [${params.organization}]")
            return
        }

        def transcription = TranscriptionConfiguration.findByOrganization(organization)
        if(transcription) {
            render(contentType: 'application/json') {
                phoneNumber = transcription.phoneNumber    
                isEnabled = transcription?.isEnabled
            }
        } else {
            render(contentType: 'application/json') {
                isEnabled = false     
            }
        }
    }

    private def getIdFromHref(def href) {
        return hrefParserService.idFromHref(href)
    }

    private def beanErrors(def bean) {
        def result = new StringBuilder()
        g.eachError(bean: bean) {
            result << g.message(error: it)
            result << "\n"
        }
        log.debug "Built beanErrors: ${result}"
        return result.toString()
    }

    private def renderVoicemailAsJson(Voicemail voicemail) {
        def formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
        def forwardFromExt = null;

        if(voicemail.forwardedBy) {
            // prepare some information for the render
            def extension = Extension.findByOwner(voicemail.forwardedBy, [max: 1])
            if (extension){
                // We have only one extension, use it
                forwardFromExt = extension.number;
                log.debug("We have forwarded by extension [${forwardFromExt}]")
            }
        }
        render(contentType: 'application/json') {
            href = '/voicemails/' + voicemail.id
            id = voicemail.id
            isNew = voicemail.isNew
            leftBy = voicemail.ani
            if(voicemail.forwardedBy) {
                forwardedBy = {
                    href = '/subscribers/' + voicemail.forwardedBy.id
                    ext = forwardFromExt
                }
            } else {
                forwardedBy = null
                ext = null
            }
            subscriber = {
                href = '/subscribers/' + voicemail.owner.id
            }
            uri = voicemail.audio.file.toURI().toString()
            description = voicemail.audio.description
            dateCreated = formatter.print(voicemail.audio.dateCreated)
            duration = voicemail.audio.duration.millis
            transcription = voicemail.audio.transcription
        }
    }

    // for call data graphing
    def startCall = {
        def json = JSON.parse(request)
        def call = new CallData()
        call.ani = json.ani
        call.dnis = json.dnis
        call.sessionId = json.sessionId
        call.save(flush: true)

        response.status = HSR.SC_OK
        response.flushBuffer()
    }

    def endCall = {
        def json = JSON.parse(request)
        def call = CallData.findBySessionId(json.sessionId)
        if(call) {
            call.refresh()
            call.ended = new LocalDateTime()
            call.save(flush: true)
        }
        response.status = HSR.SC_OK
        response.flushBuffer()
    }
}
