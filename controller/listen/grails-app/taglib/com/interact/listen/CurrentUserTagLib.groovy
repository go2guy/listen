package com.interact.listen

class CurrentUserTagLib {
    static namespace = 'listen'

    def newVoicemailCountService
    def springSecurityService

    def realName = { attrs ->
        out << springSecurityService.getCurrentUser().realName
    }

    def newVoicemailCount = { attrs ->
        out << newVoicemailCountService.count()
    }
}
