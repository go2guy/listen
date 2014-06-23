package com.interact.listen

class UserTagLib {
    static namespace = 'listen'

    def springSecurityService

    def userSelectForOperator = { attrs ->
        def organization = attrs.organization ?: springSecurityService.getCurrentUser().organization
        if(!organization) throwTagError 'Tag [userSelect] is missing required attribute [organization]'

        def duplicates = User.executeQuery('select realName from User where enabled=true group by realName having count(realName) > 1')

        def from = [] as List
        User.findAllByOrganizationAndEnabled(organization, true, [sort: 'realName', order: 'asc']).each {
            def name = it.realName + (duplicates.contains(it.realName) ? ' (' + it.username + ')' : '')
            from << new Expando(id: it.id, name: name)
        }
        attrs.from = from
        attrs.optionKey = 'id'
        attrs.optionValue = 'name'

        out << g.select(attrs)
    }
}
