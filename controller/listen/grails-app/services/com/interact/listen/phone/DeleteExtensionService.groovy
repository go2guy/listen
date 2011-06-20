package com.interact.listen.phone

import com.interact.listen.PhoneNumber

class DeleteExtensionService {
    static scope = 'singleton'
    static transactional = true

    def cloudToDeviceService
    def messageLightService
    def spotCommunicationService

    // TODO re-fit this into Extension/Mobile domains, once we refactor
    def deleteExtension(PhoneNumber phoneNumber) {
        phoneNumber.delete()
        
        // TODO history?
        // TODO stat?

        cloudToDeviceService.sendContactSync()
        if(phoneNumber.supportsMessageLight) {
            messageLightService.toggle(phoneNumber.number, false)
        }
        if(phoneNumber.greeting) {
            spotCommunicationService.deleteArtifact(phoneNumber.greeting.uri)
        }
    }
}
