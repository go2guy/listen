package com.interact.listen.voicemail
import com.interact.listen.Audio
import com.interact.listen.User

import org.joda.time.DateTime

class Voicemail {
    String ani
    Audio audio
    DateTime dateCreated // auto-timestamped by GORM
    User forwardedBy
    boolean isNew = true
    User leftBy // user who left it, if available; cached (i.e. not transient) so we can sort by it

    static belongsTo = [owner: User]

    static constraints = {
        ani blank: false
        forwardedBy nullable: true
        leftBy nullable: true
    }

    def from() {
        leftBy?.realName ? leftBy.realName + ' (' + ani + ')' : ani
    }

    def beforeInsert() {
        leftBy = User.lookupByPhoneNumber(ani)
    }
}
