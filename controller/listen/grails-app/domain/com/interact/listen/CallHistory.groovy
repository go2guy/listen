package com.interact.listen

import com.interact.listen.User
import com.interact.listen.Organization

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
