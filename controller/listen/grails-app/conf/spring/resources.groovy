import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

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

    // most of our user lookups are based on organizations, so overwrite the default Spring Security UserDetailsService
    userDetailsService(com.interact.listen.OrganizationUserDetailsService) {
        grailsApplication = ref('grailsApplication')
    }

    // but in some cases, we just need a lookup by username, so keep the plugin-provided GormUserDetailsService handy
    gormUserDetailsService(org.codehaus.groovy.grails.plugins.springsecurity.GormUserDetailsService) {
        grailsApplication = ref('grailsApplication')
    }

    cloudToDeviceMessaging(com.interact.listen.android.C2DMessaging) {
        cloudToDeviceSender = ref('cloudToDeviceSender')
        cloudToDeviceService = ref('cloudToDeviceService')
        googleAuthService = ref('googleAuthService')
        statWriterService = ref('statWriterService')
    }

    cloudToDeviceSender(com.interact.listen.android.C2DSender) {
        googleAuthService = ref('googleAuthService')
    }

    headerDetailsFilter(com.interact.listen.authentication.HeaderDetailsFilter) {
        authenticationManager = ref('authenticationManager')
    }

    apiKeyAuthenticationProvider(com.interact.listen.authentication.ApiKeyAuthenticationProvider)

    activeDirectoryAuthenticationProvider(com.interact.listen.authentication.ActiveDirectoryAuthenticationProvider) {
        grailsApplication = ref('grailsApplication')
        userDetailsService = ref('gormUserDetailsService')
    }

    //MAILOVERIDE 
    customMailMessageBuilderFactory(com.interact.listen.mail.MailMessageBuilderFactory) {
        it.autowire = true
    }

    listenLdapServer(com.interact.grails.ldap.server.TransientGrailsLdapServer) {
        base = CH.config.com.interact.listen.ldap.basedn
        port = CH.config.com.interact.listen.ldap.port as int
    }
}
