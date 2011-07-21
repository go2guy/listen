package com.interact.listen.pbx.findme

import org.joda.time.LocalDateTime
import org.joda.time.Period

class FindMeExpirationReminderService {
    def backgroundService

    def sendReminder(FindMePreferences preferences) {
        log.debug "Sending Find Me expiration reminder to user [${preferences.user}] at [${preferences.reminderNumber}]"
        backgroundService.execute("Find Me expiration reminder to [${preferences.user}] at [${preferences.reminderNumber}]", {

            def now = new LocalDateTime().withSecondOfMinute(0).withMillisOfSecond(0)
            def expires = preferences.expires.toLocalDateTime()
            def minutes = new Period(now, expires).toStandardMinutes().minutes
            def message = "Your Find Me / Follow Me configuration expires in ${minutes} minutes"
            sendMail {
                from 'Listen'
                to preferences.reminderNumber
                subject ''
                body message
            }
        })
    }
}
