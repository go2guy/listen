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

    def newAcdMessageCount(def skill = null) {
      def user
      def count = 0
      if ( skill == null || skill == 'All' ) { // find all users via skills associated with current user and retrieve the count
        user = springSecurityService.getCurrentUser()
        UserSkill.findAllByUser(user).each() { userSkill ->
          count += InboxMessage.countByOwnerAndIsNew(AcdService.getVoicemailUserBySkillname(userSkill.skill.skillname), true)
        }
      }
      else { // find only the count associated with the given skill
        user = AcdService.getVoicemailUserBySkillname(skill)
        log.debug "InboxMessageService.newAcdMessageCount(): Getting new voicemail message count for user[${user}]"
        count = InboxMessage.countByOwnerAndIsNew(user,true)
      }

      count = "(" + count + ")"

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
