package com.interact.listen

class PhoneNumber {
    String forwardedTo
    Audio greeting
    boolean isPublic = true
    String number
    User owner
    boolean supportsMessageLight = false
    PhoneNumberType type = PhoneNumberType.OTHER

    static belongsTo = User

    static constraints = {
        forwardedTo nullable: true, blank: false, maxSize: 50
        greeting nullable: true
        number blank: false, maxSize: 50, validator: { val, obj ->
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

    def beforeInsert() {
        supportsMessageLight = (type == PhoneNumberType.EXTENSION)
    }

    def beforeUpdate() {
        supportsMessageLight = (type == PhoneNumberType.EXTENSION)
        // encountered a strange error where the type property wouldnt get saved
        // returning true from this hook fixes it. possible recurrence of:
        //   http://jira.grails.org/browse/GRAILS-3903 (thats where the workaround came from)
        return true
    }
}
