package com.interact.listen.pbx

import com.interact.listen.Audio
import com.interact.listen.PhoneNumber

class Extension extends PhoneNumber {
    String forwardedTo
    Audio greeting
    String ip
    int extLength

    static transients = ['extLength']

    static constraints = {
        // TODO IP address validation

        // all fields must be nullable since we extend PhoneNumber
        forwardedTo nullable: true, blank: false, maxSize: 50
        greeting nullable: true
        // NOTE: ip cannot have a 'unique' constraint, it causes grails/hibernate to segfault. :(
        ip nullable: true, blank: false, unique: true, maxSize: 50
    }
}
