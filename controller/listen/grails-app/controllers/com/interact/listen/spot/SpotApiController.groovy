package com.interact.listen.spot

import com.interact.insa.client.StatId
import com.interact.listen.*
import com.interact.listen.android.DeviceRegistration
import com.interact.listen.attendant.*
import com.interact.listen.attendant.action.*
import com.interact.listen.conferencing.*
import com.interact.listen.license.ListenFeature
import com.interact.listen.pbx.*
import com.interact.listen.pbx.findme.*
import com.interact.listen.stats.*
import com.interact.listen.voicemail.*
import com.interact.listen.voicemail.afterhours.AfterHoursConfiguration
import grails.converters.JSON
import grails.plugins.springsecurity.Secured
import javax.servlet.http.HttpServletResponse as HSR
import org.apache.commons.lang.StringUtils
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat

@Secured(['ROLE_SPOT_API'])
class SpotApiController {
    static allowedMethods = [
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
        setPhoneNumber: 'POST',
        updateConference: 'PUT',
        updateFindMeExpiration: 'PUT',
        updateFindMeNumber: 'PUT',
        updatePhoneNumber: 'PUT',
        updateUser: 'PUT',
        updateVoicemail: 'PUT'
    ]

    def audioDownloadService
    def cloudToDeviceService
    def conferenceService
    def deleteParticipantService
    def deleteVoicemailService
    def googleAuthService
    def historyService
    def hrefParserService
    def menuLocatorService
    def messageLightService
    def springSecurityService
    def statWriterService
    def voicemailNotificationService
    def licenseService

    def addCallHistory = {
        statWriterService.send(StatId.LISTEN_CONTROLLER_API_CALLDETAILRECORD_POST)

        def formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
        def json = JSON.parse(request)
        response.status = HSR.SC_CREATED

        def callHistory = new CallHistory()
        callHistory.dateTime = formatter.parseDateTime(json.date)
        callHistory.ani = json.ani
        callHistory.dnis = json.dnis
        callHistory.duration = new Duration(json.duration)
        callHistory.fromUser = User.lookupByPhoneNumber(json.ani)
        callHistory.toUser = User.lookupByPhoneNumber(json.dnis)
        callHistory.organization = Organization.get(getIdFromHref(json.organization.href))
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
        statWriterService.send(StatId.LISTEN_CONTROLLER_API_CONFERENCERECORDING_POST)

        def json = JSON.parse(request)
        response.status = HSR.SC_CREATED

        Recording.withTransaction { status ->
            def audio = new Audio()
            audio.duration = new Duration(json.duration as Long)
            audio.fileSize = json.fileSize
            audio.uri = json.uri
            if(!(audio.validate() && audio.save())) {
                status.setRollbackOnly()
                response.sendError(HSR.SC_BAD_REQUEST, beanErrors(audio))
            }

            def recording = new Recording()
            recording.conference = Conference.get(getIdFromHref(json.conference.href))
            recording.audio = audio

            if(!(recording.validate() && recording.save())) {
                status.setRollbackOnly()
                response.sendError(HSR.SC_BAD_REQUEST, beanErrors(recording))
                return
            }
        }

        response.flushBuffer()
    }

    def addParticipant = {
        statWriterService.send(StatId.LISTEN_CONTROLLER_API_PARTICIPANT_POST)

        def json = JSON.parse(request)
        response.status = HSR.SC_CREATED
        
        def conference = Conference.get(getIdFromHref(json.conference.href))
        if(!conference) {
            response.sendError(HSR.SC_BAD_REQUEST, "Property [conference] with value [${params.conference.href}] references a non-existent entity")
            return
        }

        def participant = new Participant()
        Participant.withTransaction { status ->
            def audio = new Audio()
            audio.duration = new Duration(0)
            audio.fileSize = "0"
            audio.transcription = ""
            audio.uri = json.audioResource

            if(!(audio.validate() && audio.save())) {
                status.setRollbackOnly()
                response.sendError(HSR.SC_BAD_REQUEST, beanErrors(audio))
                return
            }

            participant.ani = json.number
            participant.conference = conference
            participant.isAdmin = json.isAdmin
            participant.isAdminMuted = json.isAdminMuted
            participant.isMuted = json.isMuted
            participant.isPassive = json.isPassive
            participant.recordedName = audio
            participant.sessionId = json.sessionID

            if(!(participant.validate() && participant.save())) {
                status.setRollbackOnly()
                response.sendError(HSR.SC_BAD_REQUEST, beanErrors(participant))
                return
            }

            render(contentType: 'application/json') {
                href = "/participants/${participant.id}"
                audioResource = participant.recordedName.uri
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
        statWriterService.send(StatId.LISTEN_CONTROLLER_API_VOICEMAIL_POST)

        def json = JSON.parse(request)
        response.status = HSR.SC_CREATED

        Voicemail.withTransaction { status ->
            def audio = new Audio()
            audio.duration = new Duration(json.duration as Long)
            audio.fileSize = json.fileSize
            audio.transcription = json.transcription
            audio.uri = json.uri

            if(!(audio.validate() && audio.save())) {
                status.setRollbackOnly()
                response.sendError(HSR.SC_BAD_REQUEST, beanErrors(audio))
                return
            }
        
            def voicemail = new Voicemail()
            voicemail.ani = json.leftBy
            voicemail.audio = audio
            voicemail.owner = User.get(getIdFromHref(json.subscriber.href))

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
                voicemailNotificationService.sendNewVoicemailEmail(voicemail)
                voicemailNotificationService.sendNewVoicemailSms(voicemail)

                def config = AfterHoursConfiguration.findByOrganization(voicemail.owner.organization)
                if(config && voicemail.owner == config.phoneNumber?.owner && config.alternateNumber?.length() > 0) {
                    log.debug "Sending alternate-number page to ${config.alternateNumber}"
                    voicemailNotificationService.sendNewVoicemailSms(voicemail, config.alternateNumber)
                }
            }

            renderVoicemailAsJson(voicemail)
        }
    }

    @Secured(['ROLE_VOICEMAIL_USER'])
    def androidEmailContact = {
        statWriterService.send(StatId.LISTEN_CONTROLLER_META_API_GET_EMAIL_CONTACTS)

        def user = springSecurityService.getCurrentUser()
        def contact = User.get(params.id)

        println "ID: ${params.id}, CONTACT: ${contact}"

        if(!contact || contact.organization != user.organization) {
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

    @Secured(['ROLE_VOICEMAIL_USER'])
    def androidEmailContacts = {
        statWriterService.send(StatId.LISTEN_CONTROLLER_META_API_GET_EMAIL_CONTACTS)

        def user = springSecurityService.getCurrentUser()

        params.offset = params['_first'] ?: 0
        params.max = params['_max'] ?: 100

        def list = User.findAllByOrganization(user.organization, params)
        def total = User.countByOrganization(user.organization)

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
            delegate.count = Kcountresults.size()
            delegate.total = total
            delegate.results = results
            if(count < total) {
                if(params.offset + count < total) {
                    next = "/emailContacts?_first=${params.offset + count}&_max=${params.max}"
                }
            }
        }
    }

    @Secured(['ROLE_VOICEMAIL_USER'])
    def androidGetDeviceRegistration = {
        def user = springSecurityService.getCurrentUser()
        def deviceType = DeviceRegistration.DeviceType.valueOf(params.deviceType)

        def device = DeviceRegistration.findWhere(user: user, deviceType: deviceType, deviceId: params.deviceId)
        if(!device) {
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }

        String token = device?.registrationToken ?: ''
        def json = [
            enabled: String.valueOf(googleAuthService.isEnabled()),
            account: googleAuthService.getUsername(),
            registrationToken: token,
            registeredTypes: device.registeredTypes.collect { it.name() }
        ]

        render json as JSON
    }

    @Secured(['ROLE_VOICEMAIL_USER'])
    def androidNumberContact = {
        statWriterService.send(StatId.LISTEN_CONTROLLER_META_API_GET_NUMBER_CONTACTS)

        def user = springSecurityService.getCurrentUser()
        def number = PhoneNumber.get(params.id)

        if(!number || number.owner.organization != user.organization) {
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }
        
        def json = [
            id: number.id,
            subscriberId: number.owner.id,
            name: number.owner.realName,
            number: number.number,
            type: number.type
        ]

        render json as JSON
    }

    @Secured(['ROLE_VOICEMAIL_USER'])
    def androidNumberContacts = {
        statWriterService.send(StatId.LISTEN_CONTROLLER_META_API_GET_NUMBER_CONTACTS)

        def user = springSecurityService.getCurrentUser()

        params.offset = params['_first'] ?: 0
        params.max = params['_max'] ?: 100

        def list = PhoneNumber.createCriteria().list(params) {
            eq('isPublic', true)
                owner {
                eq('organization', user.organization)
            }
        }

        def total = PhoneNumber.createCriteria().get {
            projections {
                count('id')
            }
            eq('isPublic', true)
            owner {
                eq('organization', user.organization)
            }
        }

        def results = []
        list.each { number ->
            results << [
                id: number.id,
                subscriberId: number.owner.id,
                name: number.owner.realName,
                number: number.number,
                type: number.type
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

    @Secured(['ROLE_VOICEMAIL_USER'])
    def androidUpdateDeviceRegistration = {
        def user = springSecurityService.getCurrentUser()
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

    @Secured(['ROLE_VOICEMAIL_USER'])
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

        def user = springSecurityService.getCurrentUser()
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

        render(contentType: 'application/json') {             
            canAccess = licenseService.canAccess(feature, organization)
        }
    }

    def canDial = {
        // TODO stat? (didnt write a stat in the old version)

        def user = User.get(getIdFromHref(params.subscriber))
        def destination = params.destination
        if(!destination) {
            response.sendError(HSR.SC_BAD_REQUEST, 'Missing required parameter [destination]')
            return
        }

        render(contentType: 'application/json') {
            delegate.canDial = user.canDial(destination)
        }
    }

    def deleteFindMeNumber = {
        // TODO stat?

        def findMeNumber = FindMeNumber.get(params.id)
        if(!findMeNumber) {
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }
        
		historyService.deletedFindMeNumber(findMeNumber)
        findMeNumber.delete()
        
        response.flushBuffer()
    }

    def deleteParticipant = {
        statWriterService.send(StatId.LISTEN_CONTROLLER_API_PARTICIPANT_DELETE)

        def participant = Participant.get(params.id)
        if(!participant) {
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }

        deleteParticipantService.deleteParticipant(participant)
        response.flushBuffer()
    }

    @Secured(['ROLE_SPOT_API', 'ROLE_VOICEMAIL_USER'])
    def deleteVoicemail = {
        statWriterService.send(StatId.LISTEN_CONTROLLER_API_VOICEMAIL_DELETE)

        def voicemail = Voicemail.get(params.id)
        if(!voicemail) {
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }

        try {
            // if this is an Android user, make sure that they are only accessing their own voicemails
            def current = springSecurityService.getCurrentUser()
            if(current && !current.hasRole('ROLE_SPOT_API') && current.id != user.id) {
                response.sendError(HSR.SC_UNAUTHORIZED)
                return
            }
        } catch(MissingPropertyException e) {
            // ignore, this is an API user
            log.debug "MissingPropertyException in deleteVoicemail API, probably an API user"
        }

        deleteVoicemailService.deleteVoicemail(voicemail)
        response.flushBuffer()
    }

    // for a given number, determines what number should *actually* be dialed
    // (considering find me configurations, forwarding, outdialing restrictions, etc.)
    def dial = {
        // TODO this does not consider find me configurations yet, since 
        // find me has not yet been migrated. after migrating, add find me lookups

        // TODO stat?

        if(!params.destination) {
            response.sendError(HSR.SC_BAD_REQUEST, 'Missing required parameter [destination]')
            return
        }

        def destination = params.destination
        def userPhoneNumber = PhoneNumber.findByNumber(params.destination)
        def user = userPhoneNumber?.owner

        if(!user) {
            response.sendError(HSR.SC_BAD_REQUEST, "Subscriber not found with phone number [${destination}]")
            return
        }

        def groups = FindMeNumber.findAllByUserGroupedByPriority(user, false)

        def isExpired = true
        def findMePreference = FindMePreferences.findByUser(user)
        if(findMePreference) {
            LocalDateTime expires = findMePreference.expires.toLocalDateTime()
            if(expires.isAfter(new LocalDateTime())) {
                isExpired = false
            }
        }

        if(groups.size() == 0 || isExpired) {
            groups.clear()

            def findMeNumber = [:]
            findMeNumber.number = destination
            findMeNumber.duration = 25
            findMeNumber.enabled = true

            def destinationPhoneNumber = PhoneNumber.findByNumber(destination)
            if(destinationPhoneNumber && destinationPhoneNumber.forwardedTo && destinationPhoneNumber.owner.canDial(destinationPhoneNumber.forwardedTo)) {
                findMeNumber.number = destinationPhoneNumber.forwardedTo
            }

            def findMeNumbers = []
            findMeNumbers.add(findMeNumber)
            groups.add(findMeNumbers)
        } else {
            def tempGroups = groups.clone()

            groups.each {entry ->
                def updatedNumbers = []
                entry.each {
                    def updatedNumber = [:]
                    updatedNumber.number = it.number
                    updatedNumber.duration = it.dialDuration
                    updatedNumber.enabled = it.isEnabled

                    def phoneNumber = PhoneNumber.findByNumber(updatedNumber.number)
                    if(phoneNumber && phoneNumber.forwardedTo && phoneNumber.owner.canDial(phoneNumber.forwardedTo)) {
                        updatedNumber.number = phoneNumber.forwardedTo
                    }

                    if(user.canDial(updatedNumber.number)) {
                        updatedNumbers.add(updatedNumber)
                    }
                }

                if(updatedNumbers.size() > 0) {
                    //tempGroups currently has FindMeNumbers. Remove and add in the map version with less info
                    tempGroups.remove(entry)
                    tempGroups.add(updatedNumbers)
                } else {
                    tempGroups.remove(entry)
                }
            }

            groups = tempGroups
        }

        if(groups.size() == 0) {
            def findMeNumber = [:]
            findMeNumber.number = destination
            findMeNumber.duration = 25
            findMeNumber.enabled = true

            def findMeNumbers = []
            findMeNumbers.add(findMeNumber)
            groups.add(findMeNumbers)
        }

        render(contentType: 'application/json') {
            groups
        }
    }

    // looks up the appropriate DNIS mapping/route for the provided number
    def dnisLookup = {
        statWriterService.send(StatId.LISTEN_CONTROLLER_META_API_GET_DNIS)

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
            return
        }

        def organization = Organization.get(getIdFromHref(params.organization))
        if(!organization) {
            response.sendError(HSR.SC_BAD_REQUEST, "Organization not found with href [${params.organization}]")
            return
        }

        def afterHoursConfig = AfterHoursConfiguration.findByOrganization(organization)
        if(!afterHoursConfig) {
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }

        def subscriber = afterHoursConfig.phoneNumber?.owner
        if(!subscriber) {
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }

        render(contentType: 'application/json') {
            href = '/subscribers/' + subscriber.id
        }
    }

    def getConference = {
        def conference = Conference.get(params.id)
        if(!conference) {
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }

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

        def featureStatusMap = [:]
        licenseService.licensableFeatures().each {
            featureStatusMap.put(it.toString(), licenseService.canAccess(it, organization))
        }

        render(contentType: 'application/json') {
            featureStatusMap
        }
    }

    def getParticipants = {

        if(!params.conference) {
            response.sendError(HSR.SC_BAD_REQUEST, 'Missing required parameter [conference]')
            return
        }

        def conference = Conference.get(getIdFromHref(params.conference))
        if(!conference) {
            response.sendError(HSR.SC_BAD_REQUEST, "Property [conference] with value [${params.conference}] references a non-existent entity")
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
            participant.put("audioResource", p.recordedName.uri)
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
        statWriterService.send(StatId.LISTEN_CONTROLLER_API_ACCESSNUMBER_GET)

        def phoneNumber = PhoneNumber.get(params.id)
        if(!phoneNumber) {
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }

        render(contentType: 'application/json') {
            href = '/accessNumbers/' + phoneNumber.id
            forwardedTo = phoneNumber.forwardedTo ?: ''
            greetingLocation = phoneNumber.greeting?.uri ?: ''
            id = phoneNumber.id
            number = phoneNumber.number
            numberType = phoneNumber.type.name()
            publicNumber = phoneNumber.isPublic
            subscriber = '/subscribers/' + phoneNumber.owner.id
        }
    }

    def getPin = {
        statWriterService.send(StatId.LISTEN_CONTROLLER_API_PIN_GET)

        if(!params.number) {
            response.sendError(HSR.SC_BAD_REQUEST, 'Missing required parameter [number]')
            return
        }

        def pin = Pin.findByNumber(params.number)
        if(!pin) {
            response.sendError(HSR.SC_NOT_FOUND, "Pin not found with number [${params.number}]")
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
        statWriterService.send(StatId.LISTEN_CONTROLLER_API_SUBSCRIBER_GET)

        def user = User.get(params.id)
        if(!user) {
            response.sendError(HSR.SC_NOT_FOUND)
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
        statWriterService.send(StatId.LISTEN_CONTROLLER_API_VOICEMAIL_GET)

        def voicemail = Voicemail.get(params.id)
        if(!voicemail) {
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }

        renderVoicemailAsJson(voicemail)
    }

    def listFindMeConfiguration = {
        // TODO stat?

        if(!params.subscriber) {
            response.sendError(HSR.SC_BAD_REQUEST, 'Missing required parameter [subscriber]')
            return
        }

        def user = User.get(getIdFromHref(params.subscriber))
        if(!user) {
            response.sendError(HSR.SC_BAD_REQUEST, "Property [subscriber] with value [${params.subscriber}] references a non-existent entity")
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
        statWriterService.send(StatId.LISTEN_CONTROLLER_API_ACCESSNUMBER_GET)

        if(!params.number) {
            response.sendError(HSR.SC_BAD_REQUEST, 'Missing required parameter [number]')
            return
        }

        def phoneNumber = PhoneNumber.findByNumber(params.number)

        def total = PhoneNumber.countByNumber(params.number)

        def results = phoneNumber?.inject([]) { list, p ->
            def subscriber = [:]
            subscriber.put("href", "/subscribers/${p.owner.id}")
            list.add(subscriber)
            return list
        }

        render(contentType: 'application/json') {
            href = "/accessNumbers?_fields=subscriber&number=${params.number}"
            count = phoneNumber == null ? 0 : 1
            delegate.total = total
            delegate.results = (results == null ? [] : results)
        }
    }

    @Secured(['ROLE_VOICEMAIL_USER', 'ROLE_SPOT_API'])
    def listUsers = {
        statWriterService.send(StatId.LISTEN_CONTROLLER_API_SUBSCRIBER_GET)

        def organization = Organization.get(getIdFromHref(params.organization))

        if(!organization) {
            def user = springSecurityService.getCurrentUser()
            if(!params.username || user.username != params.username) {
                response.sendError(HSR.SC_UNAUTHORIZED)
                return
            }
            organization = user.organization
        }

        def users = User.createCriteria().list([offset: 0, max: 100]) {
            if(params.containsKey('username')) {
                eq('username', params.username)
            }
            eq('organization', organization)
        }

        def total = User.createCriteria().get {
            projections {
                count('id')
            }
            if(params.containsKey('username')) {
                eq('username', params.username)
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

    @Secured(['ROLE_SPOT_API', 'ROLE_VOICEMAIL_USER'])
    def listVoicemails = {
        statWriterService.send(StatId.LISTEN_CONTROLLER_API_VOICEMAIL_GET)

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
            def current = springSecurityService.getCurrentUser()
            if(current && !current.hasRole('ROLE_SPOT_API') && current.id != user.id) {
                log.warn "Android user is unauthorized; current.id [${current.id}], user.id [${user.id}], hasRole [${user.hasRole('ROLE_SPOT_API')}]"
                response.sendError(HSR.SC_UNAUTHORIZED)
                return
            }
        } catch(MissingPropertyException e) {
            // ignore, this is an API user
            log.debug "MissingPropertyException in listVoicemails API, probably an API user"
        }

        params.offset = params['_first'] ?: 0
        params.max = params['_max'] ?: 100
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
            if(params.offset + count < total) {
                json.next = "/voicemails?_first=${params.offset + count}&_max=${params.max}&_sortBy=${params.sort}&_sortOrder=${params.order == 'asc' ? 'ASCENDING' : 'DESCENDING'}${pstring}"
            }
        }
        log.debug "Rendering JSON: ${json}"
        render json as JSON
    }

    // given an access number and an organization, looks up the user owning the number
    def lookupAccessNumber = {
        // TODO stat?

        def number = params.number
        if(!number) {
            response.sendError(HSR.SC_BAD_REQUEST, 'Missing required parameter [number]')
            return
        }

        def organization = Organization.get(getIdFromHref(params.organization))
        if(!organization) {
            response.sendError(HSR.SC_BAD_REQUEST, "Organization not found with href [${params.organization}]")
            return
        }

        def phoneNumbers = PhoneNumber.withCriteria() {
            eq('number', number)
            owner {
                eq('organization', organization)
            }
        }

        render(contentType: 'application/json') {
            count = (phoneNumbers.size() > 0 ? 1 : 0)
            if(phoneNumbers.size() > 0) {
                results = [{
                    href = '/accessNumbers/' + phoneNumbers[0].id
                    subscriber = {
                        href = '/subscribers/' + phoneNumbers[0].owner.id
                    }
                }]
            } else {
                results = []
            }
        }
    }

    def menuAction = {
        statWriterService.send(StatId.LISTEN_CONTROLLER_META_API_GET_MENU_ACTION)

        def id = params.menuId
        if(!id) {
            // this is a request for the entry menu for a specific organization
            if(!params.organization) {
                response.sendError(HSR.SC_BAD_REQUEST, 'Missing required parameter [organization]')
                return
            }

            def organization = Organization.get(getIdFromHref(params.organization))
            def menu = menuLocatorService.findEntryMenu(organization)
            if(!menu) {
                response.sendError(HSR.SC_NOT_FOUND)
                return
            }

            def command = menu.toIvrCommand(menu.menuGroup.organization.attendantPromptDirectory(), '')
            render(command as JSON)
            return
        }

        def menu = Menu.get(id)
        if(!menu) {
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }

        // only need keysPressed param if a menu id was provided
        def doAction
        def keysPressed = params.keysPressed
        if(!keysPressed || keysPressed.trim() == '') {
            doAction = menu.timeoutAction
        } else {
            def mappings = menu.keypressActions.inject([:]) { map, keypressAction ->
                map[keypressAction.keysPressed] = keypressAction
                return map
            }
            doAction = keypressToAction(mappings, keysPressed)
            if(!doAction) {
                doAction = menu.defaultAction
            }
        }

        def promptBefore = doAction.promptBefore
        if(doAction.instanceOf(GoToMenuAction)) {
            def group = MenuGroup.findByName(doAction.destinationMenuGroupName)
            doAction = Menu.findByMenuGroupAndName(group, doAction.destinationMenuName)
        } else if(doAction.instanceOf(ReplayMenuAction)) {
            doAction = menu
        }

        def command = doAction.toIvrCommand(menu.menuGroup.organization.attendantPromptDirectory(), promptBefore)
        render(command as JSON)
    }

    private def keypressToAction(def mappings, def keysPressed) {
        // if we have a specific mapping for the number, simply return its action
        if(mappings.containsKey(keysPressed)) {
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
        // TODO stat?

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

    // sets the SPOT system phone number (for display in notifications)
    def setPhoneNumber = {
        // TODO stat?

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
        statWriterService.send(StatId.LISTEN_CONTROLLER_API_CONFERENCE_PUT)

        def conference = Conference.get(params.id)
        if(!conference) {
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }

        def json = JSON.parse(request)

        if(json.arcadeId != conference.arcadeId) {
            conference.arcadeId = json.arcadeId
        }

        if(json.recordingSessionId != conference.recordingSessionId) {
            conference.recordingSessionId = json.recordingSessionId
        }

        if(!(conference.validate() && conference.save())) {
            response.sendError(HSR.SC_BAD_REQUEST)
            return
        }

        boolean success = true
        if(json.isStarted && !conference.isStarted) {
            success = conferenceService.startConference(conference)
        } else if(!json.isStarted && conference.isStarted) {
            success = conferenceService.stopConference(conference)
        } else {
            // the second block above handles recording stopping when the conference stops
            // this case handles it if the conference 'isStarted' status didnt change
            if(json.isRecording && !conference.isRecording) {
                success = conferenceService.startRecordingConference(conference)
            } else if(!json.isRecording && conference.isRecording) {
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
        // TODO stat?

        def findMeNumber = FindMeNumber.get(params.id)
        if(!findMeNumber) {
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }

        def json = JSON.parse(request)

        if(json.isEnabled != findMeNumber.isEnabled) {
            findMeNumber.isEnabled = json.isEnabled
        }

        if(findMeNumber.validate() && findMeNumber.save()) {
        // TODO action history?
            response.status = HSR.SC_OK
            response.flushBuffer()
    		historyService.updatedFindMeNumber(findMeNumber)
        } else {
            response.sendError(HSR.SC_BAD_REQUEST, beanErrors(findMeNumber))
        }
    }

    def updateFindMeExpiration = {
        // TODO stat?
        // TODO Action History?

        def user = User.get(params.id)
        if(!user) {
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }

        def findMePreferences = FindMePreferences.findByUser(user)
        def json = JSON.parse(request)
        if(json.isActivating) {
            findMePreferences.expires = new DateTime().plusDays(1)
        } else {
            findMePreferences.expires = new DateTime().minusMinutes(1)
        }

        if(findMePreferences.validate() && findMePreferences.save()) {
            response.status = HSR.SC_OK
            response.flushBuffer()
        } else {
            response.sendError(HSR.SC_BAD_REQUEST, beanErrors(findMePreferences))
        }
    }

    def updateParticipant = {
        statWriterService.send(StatId.LISTEN_CONTROLLER_API_PARTICIPANT_PUT)

        def participant = Participant.get(params.id)
        if(!participant) {
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }

        def json = JSON.parse(request)

        if(json.isAdminMuted != participant.isAdminMuted) {
            participant.isAdminMuted = json.isAdminMuted
            if(participant.isAdminMuted) {
                historyService.mutedConferenceCaller(participant)
            } else {
                historyService.unmutedConferenceCaller(participant)
            }
        }

        if(json.isMuted != participant.isMuted) {
            participant.isMuted = json.isMuted
        }

        if(participant.validate() && participant.save()) {
            response.status = HSR.SC_OK
            response.flushBuffer()
        } else {
            response.sendError(HSR.SC_BAD_REQUEST, beanErrors(participant))
        }
    }

    def updatePhoneNumber = {
        statWriterService.send(StatId.LISTEN_CONTROLLER_API_ACCESSNUMBER_PUT)

        def phoneNumber = PhoneNumber.get(params.id)
        if(!phoneNumber) {
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }

        def json = JSON.parse(request)
        
        if(json.subscriber?.href) {
            def user = User.get(getIdFromHref(json.subscriber.href))
            if(!user) {
                response.sendError(HSR.SC_BAD_REQUEST, "Property [subscriber] with value [${json.subscriber}] references a non-existent entity")
                return
            }
            phoneNumber.owner = user
        }

        if(json.greetingLocation) {
            phoneNumber.greeting.uri = json.greetingLocation
        }

        if(json.number) {
            phoneNumber.number = json.number
        }

        if(!json.isNull('forwardedTo')) {
            phoneNumber.forwardedTo = json.forwardedTo.length() > 0 ? json.forwardedTo : null
        }

        if(phoneNumber.validate() && phoneNumber.save()) {
            response.status = HSR.SC_OK
            response.flushBuffer()
        } else {
            response.sendError(HSR.SC_BAD_REQUEST, beanErrors(phoneNumber))
        }
    }

    def updateUser = {
        statWriterService.send(StatId.LISTEN_CONTROLLER_API_SUBSCRIBER_PUT)

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

    @Secured(['ROLE_SPOT_API', 'ROLE_VOICEMAIL_USER'])
    def updateVoicemail = {
        statWriterService.send(StatId.LISTEN_CONTROLLER_API_VOICEMAIL_PUT)

        def voicemail = Voicemail.get(params.id)
        if(!voicemail) {
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }

        try {
            // if this is an Android user, make sure that they are only accessing their own voicemails
            def current = springSecurityService.getCurrentUser()
            if(current && !current.hasRole('ROLE_SPOT_API') && current.id != user.id) {
                response.sendError(HSR.SC_UNAUTHORIZED)
                return
            }
        } catch(MissingPropertyException e) {
            // ignore, this is an API user
            log.debug "MissingPropertyException in updateVoicemail API, probably an API user"
        }

        def originalTranscription = voicemail.audio.transcription

        Voicemail.withTransaction { status ->
            def json = JSON.parse(request)
            if(json.containsKey('isNew')) {
                voicemail.isNew = json.isNew
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

            cloudToDeviceService.sendVoicemailSync(voicemail.owner)
            messageLightService.toggle(voicemail.owner)

            // only send a notification for a new message whose transcription was updated from 'Transcription Pending'
            // to something different (which means that the IVR received its transcription back and updated the voicemail
            // with it)
            if(voicemail.isNew && originalTranscription == 'Transcription Pending' && voicemail.audio.transcription != 'Transcription Pending') {
                voicemailNotificationService.sendNewVoicemailEmail(voicemail)
                voicemailNotificationService.sendNewVoicemailSms(voicemail)
            }
        }

        response.flushBuffer()
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
        return result.toString()
    }

    private def renderVoicemailAsJson(Voicemail voicemail) {
        def formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")

        render(contentType: 'application/json') {
            href = '/voicemails/' + voicemail.id
            id = voicemail.id
            isNew = voicemail.isNew
            leftBy = voicemail.ani
            if(voicemail.forwardedBy) {
                forwardedBy = {
                    href = '/subscribers/' + voicemail.forwardedBy.id
                }
            } else {
                forwardedBy = null
            }
            subscriber = {
                href = '/subscribers/' + voicemail.owner.id
            }
            uri = voicemail.audio.uri
            description = voicemail.audio.description
            fileSize = voicemail.audio.fileSize
            dateCreated = formatter.print(voicemail.audio.dateCreated)
            duration = voicemail.audio.duration.millis
            transcription = voicemail.audio.transcription
        }
    }
}