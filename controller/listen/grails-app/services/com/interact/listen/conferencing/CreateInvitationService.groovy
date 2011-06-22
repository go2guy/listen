package com.interact.listen.conferencing

class CreateInvitationService {
    static scope = 'singleton'
    static transactional = true

    def historyService
    def scheduledConferenceNotificationService
    def springSecurityService

    def createInvitation(def params) {
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

        if(invitation.validate() && invitation.save()) {
            scheduledConferenceNotificationService.sendEmails(invitation)
            historyService.createdConferenceInvitation(invitation)
            // TODO stat?
        }

        return invitation
    }
}
