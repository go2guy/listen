package com.interact.listen

class CallHistoryUserNameTagLib {
    static namespace = 'listen'

    def springSecurityService

    def numberWithRealName = { attrs ->
        def currentUser = springSecurityService.getCurrentUser()
        def personalize = attrs.personalize as Boolean
        def number = attrs.number?.encodeAsHTML() ?: 'Unknown'

        if(attrs.user == currentUser && personalize) {
            out << "me (${number})"
        } else if(attrs.user != null ) {
            out << "${g.fieldValue(bean: attrs.user, field: 'realName')} (${number})"
        } else {
            out << "${number}"
        }
    }
}
