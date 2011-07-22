package com.interact.listen.voicemail

import com.interact.listen.stats.Stat
import com.interact.listen.voicemail.afterhours.AfterHoursConfiguration
import org.joda.time.DateTime
import org.joda.time.Minutes

class FailedTranscriptionHandlerJob {
    static triggers = {
        cron name: 'failedTranscriptionHandlerTrigger', cronExpression: '0 0/1 * * * ?'
    }

    def statWriterService
    def voicemailNotificationService

    def execute() {
        def now = new DateTime()
        def voicemails = Voicemail.createCriteria().list {
            eq('isNew', true)
            audio {
                eq('transcription', 'Transcription Pending')
            }
        }

        log.debug "TONY found ${voicemails.size} voicemails pending transcription"
        voicemails.each { voicemail ->
            def differenceInMinutes = Minutes.minutesBetween(voicemail.dateCreated, now).minutes
            log.debug "Minutes between [${voicemail.dateCreated}] and [${now}]: ${differenceInMinutes}"
            if(differenceInMinutes > 5) {
                statWriterService.send(Stat.TRANSCRIPTION_FAILED)
                voicemail.audio.transcription = 'Transcription Failed'
                if(!(voicemail.validate() && voicemail.save())) {
                        log.error 'Unable to update voicemail ${voicemail.id} for failed transcription.'
                }
                voicemailNotificationService.sendNewVoicemailEmail(voicemail)
                voicemailNotificationService.sendNewVoicemailSms(voicemail)
            }
        }
    }
}
