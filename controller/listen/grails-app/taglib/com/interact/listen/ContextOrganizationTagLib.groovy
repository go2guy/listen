package com.interact.listen

class ContextOrganizationTagLib {
    static namespace = 'listen'

    def contextOrganizationProperty = { attrs ->
        if(!attrs.field) throwTagError 'Tag [contextOrganizationProperty] is missing required attribute [field]'

        def organization = session.organization
        if(!organization) {
            log.debug "Session organization is null, using defaultValue [${attrs.defaultValue}]"
            out << attrs.defaultValue ?: ''
            return
        }

        out << fieldValue(bean: organization, field: attrs.field)
    }
}
