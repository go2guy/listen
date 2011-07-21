package com.interact.listen

import com.interact.listen.fax.Fax
import com.interact.listen.voicemail.Voicemail

class InboxMessageService {
    def cloudToDeviceService
    def historyService
    def messageLightService
    def spotCommunicationService
    def springSecurityService

    def delete(InboxMessage message) {
        message.delete()

        if(message.instanceOf(Voicemail)) {
            historyService.deletedVoicemail(message)
            cloudToDeviceService.sendVoicemailSync(message.owner)
            messageLightService.toggle(message.owner)
        } else if(message.instanceOf(Fax)) {
            historyService.deletedFax(message)
        }
    }

    def newMessageCount(def user = null) {
        def forUser = user ?: springSecurityService.getCurrentUser()
        return InboxMessage.countByOwnerAndIsNew(forUser, true)
    }

    void toggleStatus(InboxMessage message) {
        message.isNew = !message.isNew
        message.save()

        if(message.instanceOf(Voicemail)) {
            cloudToDeviceService.sendVoicemailSync(message.owner)
            messageLightService.toggle(message.owner)
        }
    }

    void setStatus(InboxMessage message, boolean isNew) {
        message.isNew = isNew
        message.save()

        if(message.instanceOf(Voicemail)) {
            cloudToDeviceService.sendVoicemailSync(message.owner)
            messageLightService.toggle(message.owner)
        }
    }
}
