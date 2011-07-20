package com.interact.listen.history

import com.interact.listen.Organization
import com.interact.listen.User
import org.joda.time.DateTime
import org.joda.time.Duration

class CallHistory {
    String ani
    DateTime dateTime
    String dnis
    Duration duration
    User fromUser
    Organization organization
    User toUser
    String result

    static constraints = {
        fromUser nullable: true
        toUser nullable: true
    }
}
