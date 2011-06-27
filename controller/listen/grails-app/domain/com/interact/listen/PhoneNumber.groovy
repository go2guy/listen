package com.interact.listen

import com.interact.listen.pbx.Extension
import com.interact.listen.voicemail.DirectVoicemailNumber

// Note: do not create new instances of PhoneNumber
// This class should be marked abstract, but cannot because of http://jira.grails.org/browse/GRAILS-6780

/*abstract*/ class PhoneNumber {
    String number
    User owner

    static belongsTo = User

    static constraints = {
        number blank: false, maxSize: 100, validator: { val, obj ->
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

    // legacy, for returning to un-migrated APIs
    def type() {
        if(this.instanceOf(DirectVoicemailNumber)) {
            return 'VOICEMAIL'
        } else if(this.instanceOf(Extension)) {
            return 'EXTENSION'
        }

        return 'MOBILE'
    }
}
