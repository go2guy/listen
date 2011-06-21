package com.interact.listen

class TranscriptionConfiguration {
    boolean isEnabled = false
    Organization organization
    String phoneNumber = ''

    static constraints = {
        organization unique: true
        phoneNumber blank: true, validator: { val, obj ->
            // can only be blank if transcriptions are not enabled
            if(obj.isEnabled && (!val || val.trim() == '')) {
                return 'missing'
            }
            return true
        }
    }
}
