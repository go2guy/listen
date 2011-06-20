package com.interact.listen.pbx.findme

import com.interact.listen.User
import org.joda.time.DateTime

class FindMePreferences {
    DateTime expires
    boolean sendReminder = false
    String reminderNumber
    User user

    static constraints = {
        expires nullable: true
        reminderNumber nullable: true, blank: false, email: true, validator: { val, obj ->
            return obj.sendReminder && val == null ? 'not.provided' : true
        }
    }

    def reminderNumberComponents() {
        if(reminderNumber?.contains('@')) {
            def s = reminderNumber.split('@')
            return [
                number: s[0],
                provider: s[1]
            ]
        }
        return [
            number: reminderNumber,
            provider: ''
        ]
    }

    def isActive() {
        def now = new DateTime()
        return expires && expires?.isAfter(now)
    }

    def shouldSendReminder(def windowStart, def windowEnd) {
        if(!isActive()) {
            log.debug "Find Me configuration is not active"
            return false
        }

        if(FindMeNumber.countByIsEnabledAndUser(true, user) == 0) {
            log.debug "Find Me configuration has no enabled numbers"
            return false
        }

        def expires = expires.toLocalDateTime()
        if(expires.isBefore(windowStart) || expires.isAfter(windowEnd.minusMillis(1))) {
            log.debug "Expiration date is outside of window"
            return false
        }

        return true
    }
}
