package com.interact.listen

class OrganizationSelectTagLib {
    static namespace = 'listen'

    def organizationSelect = { attrs ->
        if(!attrs.name) throwTagError "Tag [organizationSelect] is missing required attribute [name]"

        def organizations = Organization.list([sort: 'name', order: 'asc'])
        attrs.value = attrs.value ?: ''
        attrs.from = organizations
        attrs.optionKey = 'id'

        out << g.select(attrs)
    }
}
