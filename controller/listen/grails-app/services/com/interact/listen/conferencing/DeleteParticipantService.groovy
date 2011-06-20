package com.interact.listen.conferencing

class DeleteParticipantService {
    static scope = 'singleton'
    static transactional = true

    def historyService
    // def spotCommunicationService

    def deleteParticipant(Participant participant) {
        participant.delete()
        historyService.droppedConferenceCaller(participant)
        // TODO stat?

        // TODO is this necessary? or does the IVR automatically delete the recording?
        //spotCommunicationService.deleteArtifact(participant.recordedName.uri)
    }
}
