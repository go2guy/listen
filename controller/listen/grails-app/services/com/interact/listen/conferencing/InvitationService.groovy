package com.interact.listen.conferencing

import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.apache.log4j.Logger

class InvitationService {
    def historyService
    def scheduledConferenceNotificationService
    def springSecurityService

    ScheduledConference create(def params) {
        log.debug("Create a new scheduled conference with params [${params}]")
        def user = springSecurityService.getCurrentUser()
        // TODO doesn't work with multiple conferences per user
        def conference = Conference.findByOwner(user)
        if(!conference) {
            throw new AssertionError('User does not have a conference to invite people to')
        }

        def invitation = new ScheduledConference()

        params.date = new LocalDate(params?.date_year.toInteger(), params?.date_month.toInteger(), params?.date_day.toInteger())
        params.starts = new LocalTime(params?.starts_hour.toInteger(), params?.starts_minute.toInteger())
        params.ends = new LocalTime(params?.ends_hour.toInteger(), params?.ends_minute.toInteger())
        invitation.properties = params
        log.debug("Invitation properties after binding [${invitation.properties}]")
        invitation.forConference = conference
        invitation.scheduledBy = user
        invitation.uid = UUID.randomUUID().toString()

        log.debug("lets validate and save the invitation")
        if(invitation.validate() && invitation.save()) {
            scheduledConferenceNotificationService.sendEmails(invitation)
            historyService.createdConferenceInvitation(invitation)
            // TODO stat?
        }

        return invitation
    }

    void cancel(ScheduledConference invitation) {
        def user = springSecurityService.getCurrentUser()
        if(invitation.forConference.owner != user && invitation.scheduledBy != user) {
            // TODO user better exception type
            throw new AssertionError('Action not allowed')
        }
        
        invitation.sequence++
        invitation.delete()

        scheduledConferenceNotificationService.sendCancellation(invitation)
        historyService.cancelledConferenceInvitation(invitation)
        // TODO stat?
    }
    
    ScheduledConference change(ScheduledConference invitation, def params) {
        log.debug("Update existing scheduled conference with params [${params}]")
        def originalActiveCallers = invitation.activeCallers(false)
        def originalPassiveCallers = invitation.passiveCallers(false)
        def deletedActiveInvitees = []
        def deletedPassiveInvitees = []

        params.date = new LocalDate(params?.date_year.toInteger(), params?.date_month.toInteger(), params?.date_day.toInteger())
        params.starts = new LocalTime(params?.starts_hour.toInteger(), params?.starts_minute.toInteger())
        params.ends = new LocalTime(params?.ends_hour.toInteger(), params?.ends_minute.toInteger())
        invitation.properties = params
        log.debug("Invitation properties after binding [${invitation.properties}]")
        invitation.sequence++
        def currentActiveCallers = invitation.activeCallers(false)
        def currentPassiveCallers = invitation.passiveCallers(false)

        //Figure out which, if any, of the original invitees have been removed
        originalActiveCallers.each {
            if(!currentActiveCallers.contains(it)) {
                deletedActiveInvitees << it
            }
        }

        originalPassiveCallers.each {
            if(!currentPassiveCallers.contains(it)) {
                deletedPassiveInvitees << it
            }
        }

        if(invitation.validate() && invitation.save()) {
            scheduledConferenceNotificationService.sendEmails(invitation)
            historyService.changedConferenceInvitation(invitation)
            // TODO stat?
        }

        //Need a temp scheduled conference to use to cancel the un-invited attendees
        ScheduledConference tempInvitation = new ScheduledConference()
        tempInvitation.scheduledBy = invitation.scheduledBy
        tempInvitation.forConference = invitation.forConference
        tempInvitation.uid = invitation.uid
        tempInvitation.properties = params

        tempInvitation.activeCallerAddresses = deletedActiveInvitees.join(",")
        tempInvitation.passiveCallerAddresses = deletedPassiveInvitees.join(",")
        scheduledConferenceNotificationService.sendCancellation(tempInvitation, false)

        tempInvitation.discard()

        return invitation
    }
}
