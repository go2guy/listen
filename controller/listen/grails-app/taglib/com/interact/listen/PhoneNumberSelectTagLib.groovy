package com.interact.listen

class PhoneNumberSelectTagLib {
    static namespace = 'listen'

    def springSecurityService

    def phoneNumberSelect = { attrs ->
        if(!attrs.name) throwTagError "Tag [phoneNumberSelect] is missing required attribute [name]"

        def type = attrs.type ? PhoneNumberType.valueOf(attrs.type) : null

        def organization = attrs.organization
        if(!organization) {
            def user = springSecurityService.getCurrentUser()
            if(!user) throwTagError 'Tag [phoneNumberSelect] is missing required attribute [organization] (no current user)'
            organization = user.organization
        }

        def phoneNumbers = PhoneNumber.withCriteria {
            owner {
                eq('organization', organization)
            }
            if(type) {
                eq('type', type)
            }
            order('number', 'asc')
        }

        attrs.from = phoneNumbers

        out << g.select(attrs)
    }
}
