package com.interact.listen.conferencing

class DeleteRecordingService {
    static scope = 'singleton'
    static transactional = true

    def deleteRecording(Recording recording) {
        recording.delete()
        // TODO write history
        // TODO stat?
        // TODO is this necessary?
    }
}
