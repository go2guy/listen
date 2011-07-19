package com.interact.listen

class CurrentUserTagLib {
    static namespace = 'listen'

    def inboxMessageService
    def springSecurityService

    def realName = { attrs ->
        out << springSecurityService.getCurrentUser().realName
    }

    def newMessageCount = { attrs ->
        out << inboxMessageService.newMessageCount()
    }

    def ifCannotDial = { attrs, body ->
        def user = springSecurityService.getCurrentUser()
        def number = attrs.number as String

        if(number && !user.canDial(number)) {
            out << body()
        }
    }
}
