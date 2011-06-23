// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if(System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }

grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [ html: ['text/html','application/xhtml+xml'],
                      xml: ['text/xml', 'application/xml'],
                      text: 'text/plain',
                      js: 'text/javascript',
                      rss: 'application/rss+xml',
                      atom: 'application/atom+xml',
                      css: 'text/css',
                      csv: 'text/csv',
                      all: '*/*',
                      json: ['application/json','text/json'],
                      form: 'application/x-www-form-urlencoded',
                      multipartForm: 'multipart/form-data'
                    ]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// whether to install the java.util.logging bridge for sl4j. Disable for AppEngine!
grails.logging.jul.usebridge = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// set per-environment serverURL stem for creating absolute links
environments {
    production {
        grails.serverURL = "http://www.changeme.com"
    }
    development {
        grails.serverURL = "http://localhost:8080/${appName}"
    }
    test {
        grails.serverURL = "http://localhost:8080/${appName}"
    }

}

// log4j configuration
log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    //
    //appenders {
    //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    //}

    root {
        warn()
    }

    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
           'org.codehaus.groovy.grails.web.pages', //  GSP
           'org.codehaus.groovy.grails.web.sitemesh', //  layouts
           'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
           'org.codehaus.groovy.grails.web.mapping', // URL mapping
           'org.codehaus.groovy.grails.commons', // core / classloading
           'org.codehaus.groovy.grails.plugins', // plugins
           'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
           'org.springframework',
           'org.hibernate',
           'net.sf.ehcache.hibernate'

    warn   'org.mortbay.log',
           'grails.app.tagLib.com.energizedwork.grails.plugins.jodatime'

    debug  'grails.app',
           'com.interact'
}

// Spring Security Core plugin:
grails.plugins.springsecurity.userLookup.userDomainClassName = 'com.interact.listen.User'
grails.plugins.springsecurity.userLookup.authorityJoinClassName = 'com.interact.listen.UserRole'
grails.plugins.springsecurity.authority.className = 'com.interact.listen.Role'
grails.plugins.springsecurity.useSecurityEventListener = true
grails.plugins.springsecurity.providerNames = [
    'apiKeyAuthenticationProvider',
    'daoAuthenticationProvider',
    'activeDirectoryAuthenticationProvider',
    'anonymousAuthenticationProvider'
]
grails.plugins.springsecurity.filterChain.chainMap = [
    '/api/**': 'authenticationProcessingFilter,headerDetailsFilter,exceptionTranslationFilter,filterInvocationInterceptor',
    '/spotApi/**': 'authenticationProcessingFilter,headerDetailsFilter,exceptionTranslationFilter,filterInvocationInterceptor',
    '/meta/**': 'authenticationProcessingFilter,headerDetailsFilter,exceptionTranslationFilter,filterInvocationInterceptor',
    '/**': 'JOINED_FILTERS'
]
grails.plugins.springsecurity.onInteractiveAuthenticationSuccessEvent = { e, context ->
    com.interact.listen.User.withTransaction {
        def user = context.springSecurityService.getCurrentUser()
        user.lastLogin = new org.joda.time.DateTime()
        user.save(flush: true)

        context.historyService.loggedIn(user)
        context.statWriterService.send(com.interact.listen.stats.Stat.GUI_LOGIN)
    }
}

// Joda-Time plugin:
grails.gorm.default.mapping = {
	"user-type" type: org.joda.time.contrib.hibernate.PersistentDateTime, class: org.joda.time.DateTime
	"user-type" type: org.joda.time.contrib.hibernate.PersistentDuration, class: org.joda.time.Duration
	"user-type" type: org.joda.time.contrib.hibernate.PersistentInstant, class: org.joda.time.Instant
	"user-type" type: org.joda.time.contrib.hibernate.PersistentInterval, class: org.joda.time.Interval
	"user-type" type: org.joda.time.contrib.hibernate.PersistentLocalDate, class: org.joda.time.LocalDate
	"user-type" type: org.joda.time.contrib.hibernate.PersistentLocalTimeAsString, class: org.joda.time.LocalTime
	"user-type" type: org.joda.time.contrib.hibernate.PersistentLocalDateTime, class: org.joda.time.LocalDateTime
	"user-type" type: org.joda.time.contrib.hibernate.PersistentPeriod, class: org.joda.time.Period
}

// Mail plugin
grails {
    mail {
        // FIXME hard-coded configuration
        host = 'mail.iivip.com'
        username = 'listen@iivip.com'
        password = 'listen'
    }
}
grails.mail.default.from = 'listen@iivip.com'

// Database Migrations plugin
environments {
    production {
        grails.plugin.databasemigration.updateOnStart = true
        grails.plugin.databasemigration.updateOnStartFileNames = ['changelog.groovy']
    }
}

// Codenarc plugin
codenarc.propertiesFile = 'grails-app/conf/codenarc/codenarc.properties'
codenarc.extraIncludeDirs = ['grails-app/jobs']
codenarc.maxPriority1Violations = 0
codenarc.maxPriority2Violations = 0
codenarc.maxPriority3Violations = 0

// Listen configuration
com.interact.listen.phoneNumber = '(402) 476-8786' // FIXME hard-coded number
com.interact.listen.conferencing.pinLength = 6 // FIXME hard-coded number
com.interact.listen.activeDirectory.server = 'iiserver01'
com.interact.listen.activeDirectory.domain = 'interact.nonreg'
