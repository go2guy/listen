package com.interact.listen

import groovy.sql.Sql
import org.springframework.security.ldap.userdetails.Person

import javax.persistence.EntityManager
import javax.persistence.Query

class SingleOrganizationConfiguration
{
    def dataSource
    Organization organization

    static mapping = {
        organization lazy : false
    }

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

//        return SingleOrganizationConfiguration.list()[0].organization
    def row = SingleOrganizationConfiguration.find("From SingleOrganizationConfiguration order by id");

    return row.organization;
    }
}
