package com.interact.listen

class OrganizationService {
    def ldapService

    Organization create(def params, def features = [] as Set) {
        def organization = new Organization(params)
        features.each {
            organization.addToEnabledFeatures(it)
        }
        if(organization.validate() && organization.save()) {
            // TODO history
            ldapService.addOrganization(organization)
        }
        return organization
    }
}
