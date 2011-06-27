package com.interact.listen

class MobilePhone extends PhoneNumber {
    boolean isPublic = true
    String smsDomain

    static constraints = {
        // all fields must be nullable since we extend PhoneNumber
        smsDomain nullable: true, blank: false, maxSize: 50
    }

    def asSmsEmail() {
        "${number}@${smsDomain}"
    }
}
