package com.interact.listen.conferencing

class DeleteParticipantService {
    static scope = 'singleton'
    static transactional = true

    def historyService

    def deleteParticipant(Participant participant) {
        participant.delete()
        historyService.droppedConferenceCaller(participant)
        // TODO stat?
    }
}
