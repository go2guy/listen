package com.interact.listen.voicemail

import com.interact.listen.Audio
import com.interact.listen.PhoneNumber

class DirectVoicemailNumber extends PhoneNumber {
    Audio greeting

    static constraints = {
        // all fields must be nullable since we extend PhoneNumber
        greeting nullable: true
    }
}
