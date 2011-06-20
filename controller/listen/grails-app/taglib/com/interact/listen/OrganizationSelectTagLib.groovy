package com.interact.listen

class OrganizationSelectTagLib {
    static namespace = 'listen'

    def organizationSelect = { attrs ->
        def name = attrs.name
        if(!name) throwTagError "Tag [organizationSelect] is missing required attribute [name]"

        def value = attrs.value ?: ''

        def organizations = Organization.list([sort: 'name', order: 'asc'])
        out << g.select(name: name, from: organizations, optionKey: 'id', value: value)
    }
}
