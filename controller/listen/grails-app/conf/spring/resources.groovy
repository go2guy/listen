import grails.util.Holders

// Place your Spring DSL code here
beans = {
    dataSource(org.apache.commons.dbcp.BasicDataSource) {

        driverClassName = Holders.config.dataSource.driverClassName
        url = Holders.config.dataSource.url
        username = Holders.config.dataSource.username
        password = Holders.config.dataSource.password

        minEvictableIdleTimeMillis = 1000 * 60 * 60 * 4
        timeBetweenEvictionRunsMillis = 1000 * 60 * 60 * 4
        numTestsPerEvictionRun = 3

        testOnBorrow = true
        testWhileIdle = true
        testOnReturn = true
        validationQuery = Holders.config.dataSource.validationQuery
    }

    headerDetailsFilter(com.interact.listen.authentication.HeaderDetailsFilter) {
        authenticationManager = ref('authenticationManager')
    }

    apiKeyAuthenticationProvider(com.interact.listen.authentication.ApiKeyAuthenticationProvider){
        grailsApplication = ref('grailsApplication')
    }

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

    //MAILOVERIDE
    customMailMessageBuilderFactory(com.interact.listen.mail.MailMessageBuilderFactory) {
        it.autowire = true
    }

    if(grailsApplication.config.com.interact.listen.log4j.config)
    {
        log4jConfigurer(org.springframework.beans.factory.config.MethodInvokingFactoryBean)
        {
            targetClass = 'org.springframework.util.Log4jConfigurer'
            targetMethod = 'initLogging'
            arguments = ['file:' + grailsApplication.config.com.interact.listen.log4j.config,60000]
        }
    }
    
    customMailMessageBuilderFactory(com.interact.listen.mail.MailMessageBuilderFactory) {
        it.autowire = true
    }

}
