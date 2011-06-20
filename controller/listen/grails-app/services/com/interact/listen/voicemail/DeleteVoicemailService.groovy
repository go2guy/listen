package com.interact.listen.voicemail

class DeleteVoicemailService {
    static scope = 'singleton'
    static transactional = true

    def cloudToDeviceService
    def historyService
    def messageLightService
    def spotCommunicationService

    def deleteVoicemail(Voicemail voicemail) {
        voicemail.delete()

        historyService.deletedVoicemail(voicemail)
        cloudToDeviceService.sendVoicemailSync(voicemail.owner)
        messageLightService.toggle(voicemail.owner)
        spotCommunicationService.deleteArtifact(voicemail.audio.uri)
    }
}
