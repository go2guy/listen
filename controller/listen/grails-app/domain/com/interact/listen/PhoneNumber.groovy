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
            // if the object is of type extension, verify that it's length is correct
            if((obj.type() == "EXTENSION") && (val.size() != obj.extLength)){
                return ['invalidSize', obj.extLength]
            }
            def existing = PhoneNumber.createCriteria().get {
                owner {
                    eq('organization', obj.owner.organization)
                }
                eq('number', val)
                if(obj.id) {
                    ne('id', obj.id)
                }
            }
            if (!existing) {
                return true
            } else {
                return ['duplicate']
            }
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
