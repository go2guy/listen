package com.interact.listen.conferencing

class CancelInvitationService {
    static scope = 'singleton'
    static transactional = true

    def historyService
    def scheduledConferenceNotificationService
    def springSecurityService

    void cancel(ScheduledConference invitation) {
        def user = springSecurityService.getCurrentUser()
        if(invitation.forConference.owner != user && invitation.scheduledBy != user) {
            // TODO user better exception type
            throw new AssertionError('Action not allowed')
        }

        invitation.delete()

        // FIXME uncomment after implemented
        //scheduledConferenceNotificationService.sendCancellation(invitation)
        historyService.cancelledConferenceInvitation(invitation)
        // TODO stat?
    }
}
