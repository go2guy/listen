import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

// Place your Spring DSL code here
beans = {
    dataSource(org.apache.commons.dbcp.BasicDataSource) {

        driverClassName = CH.config.dataSource.driverClassName
        url = CH.config.dataSource.url
        username = CH.config.dataSource.username
        password = CH.config.dataSource.password

        minEvictableIdleTimeMillis = 1000 * 60 * 60 * 4
        timeBetweenEvictionRunsMillis = 1000 * 60 * 60 * 4
        numTestsPerEvictionRun = 3

        testOnBorrow = true
        testWhileIdle = true
        testOnReturn = true
        validationQuery = CH.config.dataSource.validationQuery
    }

    headerDetailsFilter(com.interact.listen.authentication.HeaderDetailsFilter) {
        authenticationManager = ref('authenticationManager')
    }

    apiKeyAuthenticationProvider(com.interact.listen.authentication.ApiKeyAuthenticationProvider)

    // most of our user lookups are based on organizations, so overwrite the default Spring Security UserDetailsService
    userDetailsService(com.interact.listen.OrganizationUserDetailsService) {
        grailsApplication = ref('grailsApplication')
    }

    // but in some cases, we just need a lookup by username, so keep the plugin-provided GormUserDetailsService handy
    gormUserDetailsService(grails.plugin.springsecurity.userdetails.GormUserDetailsService) {
        grailsApplication = ref('grailsApplication')
    }

    activeDirectoryAuthenticationProvider(com.interact.listen.authentication.ActiveDirectoryAuthenticationProvider) {
        grailsApplication = ref('grailsApplication')
        userDetailsService = ref('gormUserDetailsService')
    }

    /*listenLdapServer(com.interact.grails.ldap.server.TransientGrailsLdapServer) {
        base = CH.config.com.interact.listen.ldap.basedn
        port = CH.config.com.interact.listen.ldap.port as int
    }*/

}
