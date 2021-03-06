package com.interact.listen.conferencing

import grails.plugin.springsecurity.annotation.Secured

import org.joda.time.LocalDate
import org.joda.time.LocalTime
import com.interact.listen.pbx.NumberRoute;
import org.apache.log4j.Logger

@Secured(['ROLE_CONFERENCE_USER'])
class ConferencingController {
    static allowedMethods = [
        index: 'GET',
        ajaxPagination: 'GET',
        cancel: 'POST',
        deleteRecording: 'POST',
        downloadRecording: 'GET',
        dropCaller: 'POST',
        editInvitation: 'GET',
        manage: 'GET',
        muteCaller: 'POST',
        outdial: 'POST',
        polledConference: 'GET',
        recordings: 'GET',
        invite: 'POST',
        invitations: 'GET',
        startRecording: 'POST',
        stopRecording: 'POST',
        unmuteCaller: 'POST',
        updateInvitation: 'POST'
    ]

    def audioDownloadService
    def grailsApplication
    def historyService
    def invitationService
    def recordingService
    def scheduledConferenceNotificationService
    def spotCommunicationService
    def userCreationService
    def conferenceService
    def springSecurityService

    def index = {
        redirect(action: 'manage')
    }

    def ajaxPagination = {
        render listen.paginateTotal(total: params.total, messagePrefix: 'paginate.total.callers') + g.paginate(controller: params.c, action: params.a, total: params.total, max: params.max, offset: params.offset, params: [sort: params.sort, order: params.order], maxsteps: 5)
    }

    def cancel = {
        def invitation = ScheduledConference.get(params.id)
        if(!invitation) {
            flash.errorMessage = 'Invitation not found'
            redirect(action: 'invitations')
            return
        }

        invitationService.cancel(invitation)
        flash.successMessage = 'Cancellation has been sent to invited callers'
        redirect(action: 'invitations')
    }

    def deleteRecording = {
        def preserve = [:]
        if(params.sort) preserve.sort = params.sort
        if(params.order) preserve.order = params.order
        if(params.offset) preserve.offset = params.offset
        if(params.max) preserve.max = params.max

        def recording = Recording.get(params.id)
        if(!recording) {
            flash.errorMessage = 'Recording not found'
            redirect(action: 'recordings', params: preserve)
        }

        def user = springSecurityService.currentUser
        if(recording.conference.owner != user) {
            redirect(controller: 'login', action: 'denied')
            return
        }

        recordingService.delete(recording)

        flash.successMessage = 'Recording deleted'
        redirect(action: 'recordings', params: preserve)
    }

    def downloadRecording = {
        def preserve = [:]
        if(params.sort) preserve.sort = params.sort
        if(params.order) preserve.order = params.order
        if(params.offset) preserve.offset = params.offset
        if(params.max) preserve.max = params.max

        def recording = Recording.get(params.id)
        if(!recording) {
            flash.errorMessage = 'Recording not found'
            redirect(action: 'recordings', params: preserve)
        }

        def user = springSecurityService.currentUser
        if(recording.conference.owner != user) {
            redirect(controller: 'login', action: 'denied')
            return
        }

        // TODO stat?
        audioDownloadService.download(recording.audio, response)
        historyService.downloadedConferenceRecording(recording)
    }

    // ajax
    def polledConference = {
        def conference = Conference.get(params.id)
        if(!conference) {
            response.sendError(404)
            return
        }

        params.sort = params.sort ?: 'ani'
        params.order = params.order ?: 'asc'
        params.max = 10
        params.offset = params.offset ?: 0

        def participants = Participant.findAllByConference(conference, params)
        def total = Participant.countByConference(conference)

        def json = [:]

        def c = [:]
        c.isStarted = conference.isStarted
        c.isRecording = conference.isRecording
        c.started = c.isStarted ? listen.prettytime(date: conference.startTime) : ''

        def p = [:]
        p.total = total
        p.max = params.max
        p.offset = params.offset
        p.sort = params.sort
        p.order = params.order
        p.ids = participants.collect { it.id }
        p.list = participants.collect {
            [id: it.id,
             displayName: it.displayName(),
             joined: listen.prettytime(date: it.dateCreated),
             isAdmin: it.isAdmin,
             isAdminMuted: it.isAdminMuted,
             isMuted: it.isMuted,
             isPassive: it.isPassive]
        }

        json.participants = p
        json.conference = c

        render(contentType: 'application/json') {
            json
        }
    }

    def dropCaller = {
        log.debug("dropCaller with params [${params}]")

        def participant = Participant.get(params.id)
        if(!participant) {
            log.error("dropCaller passed invalid participant [${params.id}]")
            flash.errorMessage = 'Participant not found'
            redirect(action: 'manage')
            return
        }

        def user = springSecurityService.currentUser
        if(participant.conference.owner != user) {
            log.warn("can't dropCaller because dropper is not the conference owner [${user}]")
            redirect(controller: 'login', action: 'denied')
            return
        }

        if(participant.isAdmin) {
            log.warn("Authenticated GUI user [${user?.username}] is dropping admin participant [${participant.displayName()}]")
        } else {
            log.debug("Authenticated GUI user [${user?.username}] is dropping participant [${participant.displayName()}]")
        }

        if(spotCommunicationService.dropParticipant(participant)){
            flash.successMessage = 'Caller dropped'
            log.debug("We've drop the participant [${participant.id}]")
        } else {
            // something went wrong and we couldn't drop the participant, go ahead and directly delete the entry from the database
            log.error("We've failed to drop the participant [${participant.id}], directly delete from database.")
            participant.delete()
            def adminLegs = participant.conference.findAdmins()
            log.debug("The number of admins [${adminLegs.size()}]")
            def shutdownConf = true
            if (adminLegs.size() > 0) {
                adminLegs.each { admin ->
                    log.debug("We still have an admin conference leg active [${admin}]")
                    if (admin.id == participant.id) {
                        log.debug("We already deleted admin [${admin}]")
                    } else {
                        log.debug("Admin id [${admin.id}] still active, leave conference alone")
                        shutdownConf = false
                    }
                }
            } else {
                log.error("We don't have an admin conference leg available, shutdown conference")
            }

            if (shutdownConf) {
                log.debug("We will shutdown conference [${participant.conference.id}]")
                conferenceService.stopConference(participant.conference)
            } else {
                log.debug("We'll leave conference [${participant.conference.id}] active for now")
            }
        }

        redirect(action: 'manage')
    }

    def editInvitation = {
        def invitation = ScheduledConference.get(params.id)
        if(!invitation) {
            flash.errorMessage = 'Invitation not found'
            redirect(action: 'invitations')
            return
        }

        render(view: 'invitations', model: [edit: true, scheduledConference: invitation, scheduleLists: scheduleLists()])
    }

    def manage = {
        def user = springSecurityService.currentUser
        // TODO only works for current user, needs to be usable by admins
        def conference = Conference.findByOwner(user)
        if(!conference) {
          log.warn "A conference was not found for user [${user.realName}]. We'll just create one for now since we're nice like that :)"
          // TODO render a conference not found view
          // throw new AssertionError("User ${user} does not have a conference")
          // Create a standard conference - this should probably only be a temp fix for Mimio
          conference = userCreationService.createDefaultConference(user)
        }
        
        
        
        params.sort = params.sort ?: 'ani'
        params.order = params.order ?: 'asc'
        params.max = 10
        params.offset = params.offset ?: 0

        
        def participantList = Participant.findAllByConference(conference, params)
        def participantTotal = Participant.countByConference(conference)
        def numberRoutes = NumberRoute.findAllByOrganizationAndDestination(user.organization, "Conferencing")
        render(view: 'manage', model: [conference: conference, participantList: participantList, participantTotal: participantTotal, numberRoutes: numberRoutes])
    }

    def muteCaller = {
        def participant = Participant.get(params.id)
        if(!participant) {
            flash.errorMessage = 'Participant not found'
            redirect(action: 'manage')
            return
        }

        def user = springSecurityService.currentUser
        if(participant.conference.owner != user) {
            redirect(controller: 'login', action: 'denied')
            return
        }

        if(participant.isAdmin) {
            flash.errorMessage = 'Cannot mute admin caller'
            redirect(action: 'manage')
            return
        } else if(participant.isPassive) {
            flash.errorMessage = 'Cannot mute passive caller'
            redirect(action: 'manage')
            return
        }

        spotCommunicationService.muteParticipant(participant)
        flash.successMessage = 'Caller muted'
        redirect(action: 'manage')
    }

    def outdial = {
        // TODO stat?

        def conference = Conference.get(params.id)
        if(!conference) {
            flash.errorMessage = 'Conference not found'
            redirect(action: 'manage')
            return
        }

        def user = springSecurityService.currentUser
        if(conference.owner != user) {
            redirect(controller: 'login', action: 'denied')
            return
        }

        if(!params.onDemandNumber) {
            flash.errorMessage = 'Missing number to outdial'
            redirect(action: 'manage')
            return
        }

        def automated = (params.onDemandMode != 'interactive')
        def number = params.onDemandNumber.replaceAll(' ', '')
        def requestingNumber = grailsApplication.config.com.interact.listen.phoneNumber

        if(!user.canDial(number)) {
            flash.errorMessage = 'You are not allowed to dial that number'
            redirect(action: 'manage')
            return
        }

        log.debug "Outdialing to number [${number}] for conference id [${conference.id}], automated = [${automated}]"

        if(automated) {
            spotCommunicationService.outdial(number, conference, requestingNumber)
        } else {
            spotCommunicationService.interactiveOutdial(number, conference, requestingNumber)
        }

        flash.successMessage = 'Bridging admin conference user for On Demand dial'
        redirect(action: 'manage')
    }

    def recordings = {
        params.max = Math.min(params.max ? params.int('max') : 25, 100)
        params.offset = params.offset ? params.int('offset') : 0
        params.sort = params.sort ?: 'audio.dateCreated'
        params.order = params.order ?: 'desc'

        def user = springSecurityService.currentUser
        // TODO doesnt work with multiple conferences per user
        def conference = Conference.findByOwner(user)
        params.max = Math.min(params.max ? params.int('max') : 25, 100)
        def recordingList = Recording.findAllByConference(conference, params)
        def recordingTotal = Recording.countByConference(conference)
        render(view: 'recordings', model: [recordingList: recordingList, recordingTotal: recordingTotal])
    }

    def invite = {
        log.debug "Save conferencing invite with params [${params}]"

        def invitation = invitationService.create(params)
        if(invitation.hasErrors()) {
            log.debug "Invitation has errors, did not save"
            render(view: 'invitations', model: [scheduledConference: invitation, scheduleLists: scheduleLists()])
        } else {
            flash.successMessage = 'Conference has been scheduled and email invitations have been sent'
            log.debug "Invitation was saved"
            redirect(action: 'invitations')
        }
    }

    def invitations = {
        render(view: 'invitations', model: [scheduleLists: scheduleLists()])
    }

    private def scheduleLists() {
        def user = springSecurityService.currentUser
        def future = ScheduledConference.withCriteria {
            eq('scheduledBy', user)
            or {
                and {
                    eq('date', new LocalDate())
                    ge('starts', new LocalTime())
                }
                gt('date', new LocalDate())
            }
            order('date', 'asc')
            order('starts', 'asc')
        }
        def past = ScheduledConference.withCriteria {
            eq('scheduledBy', user)
            or {
                and {
                    eq('date', new LocalDate())
                    lt('starts', new LocalTime())
                }
                lt('date', new LocalDate())
            }
            order('date', 'asc')
            order('starts', 'asc')
        }
        return [future: future, past: past]
    }

    def startRecording = {
        def conference = Conference.get(params.id)
        if(!conference) {
            flash.errorMessage = 'Conference not found'
            redirect(action: 'manage')
            return
        }

        def user = springSecurityService.currentUser
        if(conference.owner != user) {
            redirect(controller: 'login', action: 'denied')
            return
        }

        spotCommunicationService.startRecording(conference)
        flash.successMessage = 'Conference recording started'
        redirect(action: 'manage')
    }

    def stopRecording = {
        def conference = Conference.get(params.id)
        if(!conference) {
            flash.errorMessage = 'Conference not found'
            redirect(action: 'manage')
            return
        }

        def user = springSecurityService.currentUser
        if(conference.owner != user) {
            redirect(controller: 'login', action: 'denied')
            return
        }

        spotCommunicationService.stopRecording(conference);
        flash.successMessage = 'Conference recording stopped'
        redirect(action: 'manage')
    }

    def unmuteCaller = {
        log.debug("Unmute caller")
        def participant = Participant.get(params.id)
        if(!participant) {
            flash.errorMessage = 'Participant not found'
            redirect(action: 'manage')
            return
        }

        def user = springSecurityService.currentUser
        if(participant.conference.owner != user) {
            redirect(controller: 'login', action: 'denied')
            return
        }

        if(participant.isAdmin) {
            flash.errorMessage = 'Cannot unmute admin caller'
            redirect(action: 'manage')
            return
        } else if(participant.isPassive) {
            flash.errorMessage = 'Cannot unmute passive caller'
            redirect(action: 'manage')
            return
        }

        spotCommunicationService.unmuteParticipant(participant)
        flash.successMessage = 'Caller unmuted'
        redirect(action: 'manage')
    }

    def updateInvitation = {
        def invitation = ScheduledConference.get(params.id)

        if(!invitation) {
            flash.errorMessage = 'Invitation not found'
            redirect(action: 'invitations')
            return
        }

        invitation = invitationService.change(invitation, params)
        if(invitation.hasErrors()) {
            render(view: 'invitations', model: [edit: true, scheduledConference: invitation, scheduleLists: scheduleLists()])
        } else {
            flash.successMessage = 'Invitation updated; email updates have been sent'
            redirect(action: 'invitations')
        }
    }
}
