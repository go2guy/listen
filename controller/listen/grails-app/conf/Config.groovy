import com.interact.listen.license.LicenseService
import com.interact.listen.license.ListenFeature

import grails.util.Environment
import org.apache.log4j.Logger

import org.joda.time.*
import org.joda.time.contrib.hibernate.*

// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if(System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }

println "Our appname is [${appName}]"
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
                      multipartForm: 'multipart/form-data',
                      xls: 'application/vnd.ms-excel'
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
        println "production Appname is [${appName}]"
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
    def appName = grails.util.Metadata.current.'app.name'
    println "log4j Appname is [${appName}]"
    appenders {
        def defaultPattern = '%d{ISO8601} [%10.10t] [%18.18c] %5p: %m%n'
        console name: 'stdout', layout: pattern(conversionPattern: defaultPattern)

        environments {
            production {
                def dir = System.getProperty('catalina.base')
                if(!dir) {
                    dir = '/interact/listen/logs'
                } else {
                    dir += '/logs'
                }

                println "Log directory is [${dir}]"
                println "Appname is [${appName}]"
                
                def file = new File(dir)
                if(file.exists()) {
                    if(!file.isDirectory()) {
                        def fallback = System.getProperty('java.io.tmpdir')
                        println "Log directory [${dir}] exists but is not a directory, using [${fallback}] instead"
                        dir = fallback
                    }
                } else {
                    println "Log directory [${dir}] does not exist, creating"
                    file.mkdirs()
                }

                rollingFile name: 'file', maxFileSize: '100MB', maxBackupIndex: '7', file: "${dir}/${appName}.log", layout: pattern(conversionPattern: defaultPattern)
                rollingFile name: 'StackTrace', maxFileSize: '10MB', maxBackupIndex: '7', file: "${dir}/${appName}-stacktrace.log"
            }
        }
    }

    environments {
        development {
            root { warn 'stdout' }
        }
        test {
            root { warn 'stdout' }
        }
        production {
            root { warn 'file' }
        }
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
           'com.interact'/*,
           'org.apache.directory.server',
           'org.apache.mina',
           'groovy.grails.ldap.server'*/
}

environments {
    production {
        def logger = Logger.getRootLogger()
        logger.removeAppender('stdout')
        // see http://stackoverflow.com/questions/2410955/why-is-grails-in-tomcat-logging-to-both-catalina-out-and-my-custom-file-appende
    }
}

// Spring Security Core plugin:
grails.plugins.springsecurity.userLookup.userDomainClassName = 'com.interact.listen.User'
grails.plugins.springsecurity.userLookup.authorityJoinClassName = 'com.interact.listen.UserRole'
grails.plugins.springsecurity.userLookup.enabledPropertyName = 'enabledForLogin'
grails.plugins.springsecurity.authority.className = 'com.interact.listen.Role'
grails.plugins.springsecurity.useSecurityEventListener = true

def providers = []
providers << 'apiKeyAuthenticationProvider'
providers << 'daoAuthenticationProvider'

def licenseService = new LicenseService()
licenseService.afterPropertiesSet()
if(licenseService.isLicensed(ListenFeature.ACTIVE_DIRECTORY)) {
    providers << 'activeDirectoryAuthenticationProvider'
}
providers << 'anonymousAuthenticationProvider'
grails.plugins.springsecurity.providerNames = providers

grails.plugins.springsecurity.filterChain.chainMap = [
    '/api/**': 'authenticationProcessingFilter,headerDetailsFilter,exceptionTranslationFilter,filterInvocationInterceptor',
    '/faxApi/**': 'authenticationProcessingFilter,headerDetailsFilter,exceptionTranslationFilter,filterInvocationInterceptor',
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

grails.gorm.autoFlush=true
// Joda-Time plugin, other mappings:
//import org.joda.time.*
//import org.joda.time.contrib.hibernate.*
grails.gorm.default.mapping = {
    //'user-type' (type: PersistentDateTime, class: DateTime)
    //'user-type' (type: PersistentLocalDate, class: LocalDate)
    
   
    "user-type" type: org.joda.time.contrib.hibernate.PersistentDateTime, class: org.joda.time.DateTime
    "user-type" type: org.joda.time.contrib.hibernate.PersistentDuration, class: org.joda.time.Duration
    "user-type" type: org.joda.time.contrib.hibernate.PersistentInstant, class: org.joda.time.Instant
    "user-type" type: org.joda.time.contrib.hibernate.PersistentInterval, class: org.joda.time.Interval
    "user-type" type: org.joda.time.contrib.hibernate.PersistentLocalDate, class: org.joda.time.LocalDate
    "user-type" type: org.joda.time.contrib.hibernate.PersistentLocalTimeAsString, class: org.joda.time.LocalTime
    "user-type" type: org.joda.time.contrib.hibernate.PersistentLocalDateTime, class: org.joda.time.LocalDateTime
    "user-type" type: org.joda.time.contrib.hibernate.PersistentPeriod, class: org.joda.time.Period

    'user-type' type: com.interact.listen.PersistentFileUri, class: File
}

// Mail plugin
grails {
    mail {
        host = 'localhost'
        username = ''
        password = ''
    }
}
grails.mail.default.from = 'listen@localhost'

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
com.interact.listen.conferencing.defaultPinLength = 6
com.interact.listen.activeDirectory.server = 'iiserver01'
com.interact.listen.activeDirectory.domain = 'interact.nonreg'
com.interact.listen.ldap.basedn = System.getProperty('com.interact.listen.ldap.basedn', 'dc=iivip,dc=com')
com.interact.listen.ldap.port = System.getProperty('com.interact.listen.ldap.port', '389')

// page:     https://test.authorize.net
// username: hruskar@iivip.com
// password: Int3ract!Inc
com.interact.listen.authorizenet.loginId = '9u9rhMY2hS2'
com.interact.listen.authorizenet.transactionKey = '6s6Z7U5z2WnyA2Xz'

/*ldapServers {
    listen {
        base = com.interact.listen.ldap.basedn
        port = com.interact.listen.ldap.port as int
    }
}*/
