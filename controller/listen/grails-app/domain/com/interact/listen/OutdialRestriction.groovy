package com.interact.listen

import com.interact.listen.User

class OutdialRestriction {
    Organization organization
    String pattern
    User target // null means everyone

    static hasMany = [exceptions: OutdialRestrictionException]

    static constraints = {
        pattern blank: false, maxSize: 50 // TODO pattern validation
        target nullable: true
    }
}
