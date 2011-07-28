package com.interact.listen.conferencing

class InvitationService {
    def historyService
    def scheduledConferenceNotificationService
    def springSecurityService

    ScheduledConference create(def params) {
        def user = springSecurityService.getCurrentUser()
        // TODO doesnt work with multiple conferences per user
        def conference = Conference.findByOwner(user)
        if(!conference) {
            throw new AssertionError('User does not have a conference to invite people to')
        }

        def invitation = new ScheduledConference()
        invitation.properties = params
        invitation.forConference = conference
        invitation.scheduledBy = user
        invitation.uid = UUID.randomUUID().toString()

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
        def originalActiveCallers = invitation.activeCallers(false)
        def originalPassiveCallers = invitation.passiveCallers(false)
        def deletedActiveInvitees = []
        def deletedPassiveInvitees = []

        invitation.properties = params
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
            historyService.createdConferenceInvitation(invitation)
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
