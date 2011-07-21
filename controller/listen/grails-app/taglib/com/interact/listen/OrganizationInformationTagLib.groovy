package com.interact.listen

class OrganizationInformationTagLib {
    static namespace = 'listen'

    def springSecurityService

    def ifTranscriptionEnabled = { attrs, body ->
        def organization = attrs.organization ?: springSecurityService.getCurrentUser().organization
        if(!organization) throwTagError 'Tag [ifTranscriptionEnabled] is missing required attribute [organization]'

        if(organization.hasTranscriptionEnabled()) {
            out << body()
        }
    }
}
