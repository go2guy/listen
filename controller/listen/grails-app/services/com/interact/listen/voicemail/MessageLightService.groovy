package com.interact.listen.voicemail

import com.interact.listen.PhoneNumber
import com.interact.listen.User
import com.interact.listen.spot.SpotCommunicationException

class MessageLightService {
    static scope = 'singleton'
    static transactional = true

    def spotCommunicationService

    void toggle(User user) {
        PhoneNumber.findAllByOwnerAndSupportsMessageLight(user, true).each { phoneNumber ->
            toggle(phoneNumber)
        }
    }

    void toggle(PhoneNumber phoneNumber) {
        boolean hasNew = Voicemail.countByOwnerAndIsNew(phoneNumber.owner, true) > 0
        toggle(phoneNumber.number, hasNew)
    }

    void toggle(def number, boolean on) {
        // handle Exceptions to avoid failing an entire transaction simply because the light didnt update

        try {
            log.debug "Toggling message light [${on ? 'on' : 'off'}] for phone [${number}]"
            spotCommunicationService.toggleMessageLight(number, on)
        } catch(IOException e) {
            log.error(e)
        } catch(SpotCommunicationException e) {
            log.error(e)
        }
    }
}
