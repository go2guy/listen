package com.interact.listen.history

import com.interact.listen.User
import com.interact.listen.Organization

import org.joda.time.DateTime

class ActionHistory {
    DateTime dateCreated
    User byUser
    User onUser
    Action action
    String description
    Channel channel
    Organization organization

    // byUser is nullable so that non-user-contextual jobs (e.g. cron) can write histories
    // organization is nullable so custodians can have histories
    static constraints = {
        byUser nullable: true
        onUser nullable: true
        organization nullable: true
    }
}
