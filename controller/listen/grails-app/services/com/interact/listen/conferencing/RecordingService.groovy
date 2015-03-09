package com.interact.listen.conferencing

class RecordingService {
    def historyService

    void delete(Recording recording) {
        recording.delete()
        recording.audio.delete()
        historyService.deletedConferenceRecording(recording)
    }
}
