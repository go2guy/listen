package com.interact.listen

import com.interact.listen.pbx.Extension

class PhoneNumber {
    String number
    User owner

    static belongsTo = User

    static constraints = {
        owner nullable: false
        number blank: false, maxSize: 100, validator: { val, obj ->
            if(!obj?.owner?.organization) return false
            def existing = PhoneNumber.createCriteria().get {
                owner {
                    eq('organization', obj.owner.organization)
                }
                eq('number', val)
                if(obj.id) {
                    ne('id', obj.id)
                }
            }
            return !existing
        }
    }

    // legacy, for returning to un-migrated (android) API clients
    def type() {
        if(this.instanceOf(DirectMessageNumber)) {
            return 'VOICEMAIL'
        } else if(this.instanceOf(Extension)) {
            return 'EXTENSION'
        } else if(this.instanceOf(DirectInwardDialNumber)) {
            return 'DID'
        } else if(this.instanceOf(MobilePhone)) {
            return 'MOBILE'
        } else {
            return 'OTHER'
        }
    }
}
