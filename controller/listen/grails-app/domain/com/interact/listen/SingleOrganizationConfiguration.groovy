package com.interact.listen

class SingleOrganizationConfiguration {
    Organization organization

    static boolean exists() {
        SingleOrganizationConfiguration.count() > 0
    }

    static void thisSet(Organization organization) {
        SingleOrganizationConfiguration.withTransaction {
            unset()

            def instance = new SingleOrganizationConfiguration(organization: organization)
            instance.save(flush: true)
        }
    }

    static void unset() {
        SingleOrganizationConfiguration.executeUpdate('delete from SingleOrganizationConfiguration')
    }

    static Organization retrieve() {
        if(!exists()) {
            return null
        }

        return SingleOrganizationConfiguration.list()[0].organization
    }
}
