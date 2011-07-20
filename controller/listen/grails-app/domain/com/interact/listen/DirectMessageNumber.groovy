package com.interact.listen

class DirectMessageNumber extends PhoneNumber {
    Audio greeting

    static constraints = {
        // all fields must be nullable since we extend PhoneNumber
        greeting nullable: true
    }
}
