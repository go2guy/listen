package com.interact.listen

class MobilePhone extends PhoneNumber {
    boolean isPublic = true
    Audio greeting
    String smsDomain

    static constraints = {
        // all fields must be nullable since we extend PhoneNumber
        smsDomain nullable: true, blank: false, maxSize: 50
    }

    def asSmsEmail() {
        "${number}@${smsDomain}"
    }
}
