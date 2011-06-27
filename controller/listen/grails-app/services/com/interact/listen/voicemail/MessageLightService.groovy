package com.interact.listen.voicemail

import com.interact.listen.User
import com.interact.listen.pbx.Extension
import com.interact.listen.spot.SpotCommunicationException

class MessageLightService {
    static transactional = true

    def spotCommunicationService

    void toggle(User user) {
        Extension.findAllByOwner(user).each { extension ->
            toggle(extension)
        }
    }

    void toggle(Extension extension) {
        boolean hasNew = Voicemail.countByOwnerAndIsNew(extension.owner, true) > 0
        toggle(extension.number, hasNew)
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
