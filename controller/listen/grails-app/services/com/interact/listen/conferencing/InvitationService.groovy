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

        invitation.delete()

        scheduledConferenceNotificationService.sendCancellation(invitation)
        historyService.cancelledConferenceInvitation(invitation)
        // TODO stat?
    }
    
    ScheduledConference change(ScheduledConference invitation, def params) {
        return invitation
    }
}
