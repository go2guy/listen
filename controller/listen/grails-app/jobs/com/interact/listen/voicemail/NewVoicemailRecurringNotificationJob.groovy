package com.interact.listen.voicemail

import com.interact.listen.stats.Stat
import com.interact.listen.voicemail.afterhours.AfterHoursConfiguration
import org.joda.time.DateTime
import org.joda.time.Minutes

class NewVoicemailRecurringNotificationJob {
    static triggers = {
        cron name: 'newVoicemailNotificationTrigger', cronExpression: '0 0/1 * * * ?'
    }

    def voicemailNotificationService

    def execute() {
        def now = new DateTime()
        def preferences = VoicemailPreferences.findAllWhere(isSmsNotificationEnabled: true, recurringNotificationsEnabled: true)
        def afterHoursConfigs = AfterHoursConfiguration.findAll()
        preferences.each { pref ->
            // does the user have any new voicemails?
            def newVoicemails = Voicemail.findAllByOwnerAndIsNew(pref.user, true)
            log.debug "Found ${newVoicemails.size()} new voicemails for ${pref.user}"
            newVoicemails.each { voicemail ->
                def differenceInMinutes = Minutes.minutesBetween(voicemail.dateCreated, now).minutes
                log.debug "Minutes between [${voicemail.dateCreated}] and [${now}]: ${differenceInMinutes}"
                if(differenceInMinutes > 0 && differenceInMinutes % 10 == 0) {
                    sendSms(voicemail)
                    afterHoursConfigs.each { config ->
                        if(pref.user == config.mobilePhone.owner) {
                            if(config.alternateNumber && config.alternateNumber != '') {
                                log.debug "Sending alternate-number page to ${config.alternateNumber}"
                                voicemailNotificationService.sendNewVoicemailSms(voicemail, config.alternateNumber, Stat.NEW_VOICEMAIL_RECURRING_SMS_ALTERNATE)
                            }
                        }
                    }
                }
            }
        }
    }

    private void sendSms(Voicemail voicemail) {
        log.debug "Sending recurring notification for user [${voicemail.owner}] for voicemail received [${voicemail.dateCreated}]"
        voicemailNotificationService.sendNewVoicemailSms(voicemail, null, Stat.NEW_VOICEMAIL_RECURRING_SMS)
    }
}
