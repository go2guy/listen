package com.interact.listen

import com.interact.listen.User
import org.joda.time.DateTime

class InboxMessage {
    DateTime dateCreated // auto-timestamped by GORM
    User forwardedBy
    boolean isNew = true
    User leftBy // user who left it, if available; cached (i.e. not transient) so we can sort by it

    static belongsTo = [owner: User]

    static constraints = {
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
