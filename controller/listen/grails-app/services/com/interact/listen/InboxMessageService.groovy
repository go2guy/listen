package com.interact.listen

import com.interact.listen.acd.UserSkill
import com.interact.listen.acd.AcdService
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

    def newAcdMessageCount(def user = null) {
      if ( user == null ) {
        user = springSecurityService.getCurrentUser()
      }

      def count = ""

      UserSkill.findAllByUser(user).each() { userSkill ->
//        log.debug "InboxMessageService.newAcdMessageCount(): Getting new voicemail messages for user[${AcdService.getVoicemailUserBySkillname(userSkill.skill.skillname).username}]"
        count += "(" + InboxMessage.countByOwnerAndIsNew(AcdService.getVoicemailUserBySkillname(userSkill.skill.skillname), true) + ")"
      }

      if ( count == "" ) {
        count = "(0)"
      }

      return count
    }

    void toggleStatus(InboxMessage message) {
        setStatus(message, !message.isNew)
    }

    void setStatus(InboxMessage message, boolean isNew) {
        def originalIsNew = message.isNew

        message.isNew = isNew
        message.save()

        if(originalIsNew != message.isNew) {
            if(message.instanceOf(Voicemail)) {
                if(message.isNew) {
                    historyService.markedVoicemailNew(message)
                } else {
                    historyService.markedVoicemailOld(message)
                }
            } else {
                if(message.isNew) {
                    historyService.markedFaxNew(message)
                } else {
                    historyService.markedFaxOld(message)
                }
            }
        }

        if(message.instanceOf(Voicemail)) {
            cloudToDeviceService.sendVoicemailSync(message.owner)
            messageLightService.toggle(message.owner)
        }
    }
}
