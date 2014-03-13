import com.interact.listen.license.LicenseService
import com.interact.listen.license.ListenFeature
import grails.util.Environment
import org.joda.time.*

grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [
    all:           '*/*',
    atom:          'application/atom+xml',
    css:           'text/css',
    csv:           'text/csv',
    form:          'application/x-www-form-urlencoded',
    html:          ['text/html','application/xhtml+xml'],
    js:            'text/javascript',
    json:          ['application/json', 'text/json'],
    multipartForm: 'multipart/form-data',
    rss:           'application/rss+xml',
    text:          'text/plain',
    xml:           ['text/xml', 'application/xml']
]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// What URL patterns should be processed by the resources plugin
grails.resources.adhoc.patterns = ['/images/*', '/css/*', '/js/*', '/plugins/*']

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
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
// whether to disable processing of multi part requests
grails.web.disable.multipart=false

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// configure auto-caching of queries by default (if false you can cache individual queries with 'cache: true')
grails.hibernate.cache.queries = false

environments {
    development {
        grails.logging.jul.usebridge = true
    }
    production {
        grails.config.locations = [ "classpath:listen-controller.properties"]
        grails.logging.jul.usebridge = false
    }
}

// log4j configuration
switch(Environment.current) {
    case Environment.DEVELOPMENT:
    case Environment.TEST:
        log4j = {
            def appName = grails.util.Metadata.current.'app.name'
            appenders {
                def defaultPattern = '%d{ISO8601} [%10.10t] [%18.18c] %5p: %m%n'
                console name: 'stdout', layout: pattern(conversionPattern: defaultPattern)
            }

            environments {
                development {
                    root { info 'stdout' }
                }
                test {
                    root { warn 'stdout' }
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
                    'org.hibernate',
                    'net.sf.ehcache.hibernate',
                    'org.springframework'

            warn   'org.mortbay.log',
                    'grails.app.tagLib.com.energizedwork.grails.plugins.jodatime'

            debug  'grails.app',
                    'com.interact'
        }
        break;
    case Environment.PRODUCTION:
        com.interact.listen.log4j.config = '/interact/tomcat/lib/log4j.listen.properties'
        break;
}

grails.plugin.springsecurity.userLookup.userDomainClassName = 'com.interact.listen.User'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'com.interact.listen.UserRole'
grails.plugin.springsecurity.userLookup.enabledPropertyName = 'enabledForLogin'
grails.plugin.springsecurity.authority.className = 'com.interact.listen.Role'
grails.plugin.springsecurity.useSecurityEventListener = true

def providers = []
providers << 'apiKeyAuthenticationProvider'
providers << 'daoAuthenticationProvider'

def licenseService = new LicenseService()
licenseService.afterPropertiesSet()
if(licenseService.isLicensed(ListenFeature.ACTIVE_DIRECTORY))
{
    providers << 'activeDirectoryAuthenticationProvider'
}
providers << 'anonymousAuthenticationProvider'
grails.plugin.springsecurity.providerNames = providers

grails.plugin.springsecurity.filterChain.chainMap = [
        '/api/**': 'authenticationProcessingFilter,headerDetailsFilter,exceptionTranslationFilter,filterInvocationInterceptor',
        '/faxApi/**': 'authenticationProcessingFilter,headerDetailsFilter,exceptionTranslationFilter,filterInvocationInterceptor',
        '/spotApi/**': 'authenticationProcessingFilter,headerDetailsFilter,exceptionTranslationFilter,filterInvocationInterceptor',
        '/meta/**': 'authenticationProcessingFilter,headerDetailsFilter,exceptionTranslationFilter,filterInvocationInterceptor',
        '/**': 'JOINED_FILTERS'
]
grails.plugin.springsecurity.onInteractiveAuthenticationSuccessEvent = { e, context ->
    com.interact.listen.User.withTransaction {
        def user = context.springSecurityService.getCurrentUser()
        user.lastLogin = new org.joda.time.DateTime()
        user.save(flush: true)

        context.historyService.loggedIn(user)
        context.statWriterService.send(com.interact.listen.stats.Stat.GUI_LOGIN)
    }
}

grails.gorm.autoFlush=true

grails.gorm.default.mapping = {
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
        host = "localhost"
        username = ""
        password = ""
        props = ["mail.smtp.starttls.enable":"true",
                "mail.smtp.port":"587"]
    }
}

grails.mail.default.from = 'listen@newnet.com'

// Listen configuration
com.interact.listen.ivr = ''
com.interact.listen.spotUrl = 'http://localhost/spot'
com.interact.listen.phoneNumber = '(402) 476-8786' // FIXME hard-coded number
com.interact.listen.conferencing.defaultPinLength = 6

// After Hours Configuration
com.interact.listen.afterHours.username = 'After Hours'

com.interact.listen.activeDirectory.server = 'na-ne-dc01'
com.interact.listen.activeDirectory.domain = 'newnet.local'
com.interact.listen.ldap.basedn = System.getProperty('com.interact.listen.ldap.basedn', 'dc=newnet,dc=local')
com.interact.listen.ldap.port = System.getProperty('com.interact.listen.ldap.port', '389')

// page:     https://test.authorize.net
// username: hruskar@iivip.com
// password: Int3ract!Inc
com.interact.listen.authorizenet.loginId = '9u9rhMY2hS2'
com.interact.listen.authorizenet.transactionKey = '6s6Z7U5z2WnyA2Xz'

com.interact.listen.acd.callProcessor.repeatInterval = 5000
com.interact.listen.acd.cleanup.repeatInterval = 20000
com.interact.listen.acd.agent.waitTime = 15
com.interact.listen.acd.waiting.max = 300
com.interact.listen.acd.connect_request.max = 20
com.interact.listen.acd.enqueue.max = 300
com.interact.listen.acd.disableOnFailedCall = false
com.interact.listen.api.adminSignature = 'a80f3b59eec04c24abff14b8b2c8e93d'


// listen artifacts directory - not everything is using this at the moment
com.interact.listen.artifactsDirectory = '/interact/listen/artifacts'

// Enable pretty print for JSON and XML.
environments {
  development {
    grails.converters.default.pretty.print = true
  }
}

