package com.interact.listen

class DirectInwardDialNumber extends PhoneNumber {
    Audio greeting

    static constraints = {
        // all fields must be nullable since we extend PhoneNumber
        greeting nullable: true
    }
}
