package com.interact.listen.voicemail

import com.interact.listen.User

class VoicemailPreferences {
    boolean transcribe = false
    String passcode // numeric string (to support leading zeroes)
    PlaybackOrder playbackOrder = PlaybackOrder.OLDEST_TO_NEWEST
    User user

    // TODO ideally these settings would be generalized in some sort of
    // 'notification' model, which would allow notifications for all types
    // of events. if we add notifications later to other apps, we should
    // refactor toward that end.
    boolean isEmailNotificationEnabled = false
    boolean isSmsNotificationEnabled = false
    boolean recurringNotificationsEnabled = false
    String emailNotificationAddress
    String smsNotificationAddress

    static hasMany = [
        emailTimeRestrictions: TimeRestriction,
        smsTimeRestrictions: TimeRestriction
    ]

    static constraints = {
        emailNotificationAddress nullable: true, blank: false
        passcode blank: false, maxSize: 20, matches: '^[0-9]+$'
        smsNotificationAddress nullable: true, blank: false, email: true, validator: { val, obj ->
            return obj.isSmsNotificationEnabled && val == null ? 'not.provided' : true
        }
    }

    def smsNotificationNumber() {
        if(smsNotificationAddress?.split('@')?.length == 2) {
            def smsNumber = smsNotificationAddress.split('@')[0]

            if(smsNumber.length() == 11 && smsNumber.startsWith("1")) {
                return smsNumber.substring(1)
            }
            else {
                return smsNumber
            }
        }
    }

    def smsNotificationProvider() {
        if(smsNotificationAddress?.split('@')?.length == 2) {
            return smsNotificationAddress.split('@')[1]
        }
    }
}
