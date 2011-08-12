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

    def ifEqualsCurrentUser = { attrs, body ->
        if(!attrs.user) throwTagError 'Tag [ifEqualsCurrentUser] is missing required attribute [user]'
        if(equalsCurrentUser(attrs.user)) {
            out << body()
        }
    }

    def ifNotEqualsCurrentUser = { attrs, body ->
        if(!attrs.user) throwTagError 'Tag [ifNotEqualsCurrentUser] is missing required attribute [user]'
        if(!equalsCurrentUser(attrs.user)) {
            out << body()
        }
    }

    private boolean equalsCurrentUser(def user) {
        def currentUser = springSecurityService.getCurrentUser()
        return user == currentUser
    }
}
