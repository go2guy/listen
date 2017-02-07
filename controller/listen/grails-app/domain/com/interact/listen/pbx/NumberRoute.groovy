package com.interact.listen.pbx

import com.interact.listen.Organization

class NumberRoute {
    String destination
    String label
    Organization organization
    String pattern
    Type type

    static final enum Type {
        EXTERNAL,
        INTERNAL
    }

    static belongsTo = Organization

    static constraints = {
        destination blank: false, maxSize: 100
        label nullable: true, blank: false, maxSize: 50, unique: ['organization', 'type'], validator: { val, obj ->
            if(val && obj.pattern?.contains('*')) return 'on.wildcarded.number'
            return true
        }
        pattern blank: false, maxSize: 50, matches: '^[0-9]+\\*?|\\*$', unique: ['organization', 'type']
    }

/*    static mapping = {
        organization lazy: false
    }*/

    static fetchMode = [organization: 'eager']
}
