package com.interact.listen

class PhoneNumberSelectTagLib {
    static namespace = 'listen'

    def springSecurityService

    def mobilePhoneSelect = { attrs ->
        if(!attrs.name) throwTagError "Tag [mobilePhoneSelect] is missing required attribute [name]"

        def organization = attrs.organization
        if(!organization) {
            def user = springSecurityService.getCurrentUser()
            if(!user) throwTagError 'Tag [mobilePhoneSelect] is missing required attribute [organization] (no current user)'
            organization = user.organization
        }

        def mobilePhones = MobilePhone.withCriteria { 
            owner {
                eq('organization', organization)
            }
            order('number', 'asc')
        }

        attrs.from = mobilePhones
        out << g.select(attrs)
    }
}
