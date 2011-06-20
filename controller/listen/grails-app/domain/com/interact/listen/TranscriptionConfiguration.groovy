package com.interact.listen

import com.interact.listen.Organization

class TranscriptionConfiguration {
    boolean isEnabled = false
    Organization organization
    String phoneNumber = ''

    static constraints = {
        organization unique: true
        phoneNumber blank: true, validator: { val, obj ->
            // can only be blank if transcriptions are not enabled
            if(obj.isEnabled && (!val || val.trim().equals(''))) {
                return 'missing'
            }
            return true
        }
    }
}
