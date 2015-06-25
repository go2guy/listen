grails.servlet.version = "2.3.3" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
grails.project.source.level = 1.6
//grails.project.war.file = "target/${appName}-${appVersion}.war"
grails.project.war.file = "target/listen-controller.war"

// uncomment (and adjust settings) to fork the JVM to isolate classpaths
//grails.project.fork = [
//   run: [maxMemory:1024, minMemory:64, debug:false, maxPerm:256]
//]

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // specify dependency exclusions here; for example, uncomment this to disable ehcache:
        // excludes 'ehcache'
    }
    log "debug" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve
    legacyResolve false // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility

    repositories {
        inherits true // Whether to inherit repository definitions from plugins

        grailsPlugins()
        grailsHome()
        grailsCentral()

        mavenLocal()
        mavenCentral()

        mavenRepo "http://repo.grails.org/grails/libs-releases/"
        mavenRepo "http://m2repo.spockframework.org/ext/"
        mavenRepo "http://m2repo.spockframework.org/snapshots/"
        mavenRepo "http://repo.spring.io/milestone/"
        mavenRepo "http://download.java.net/maven/2/"
        mavenRepo "http://megatron:8081/nexus/content/groups/public"
        mavenRepo "http://repo1.maven.org/maven2"
        mavenRepo "http://repo.grails.org/grails/core"
        mavenRepo "http://repo.grails.org/grails/plugins"
    }

    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes e.g.

        // runtime 'mysql:mysql-connector-java:5.1.20'
        compile("joda-time:joda-time-hibernate:1.3") {
            excludes "joda-time", "hibernate"
        }

//        compile('org.apache.mina:mina-core:2.0.4')
        compile('org.apache.mina:mina-core:1.1.6')
        compile('license-client:license-client:1.0')
        compile('realize-client:realize-client:1.0')
        compile('apacheds:apacheds-core:1.5.4')
        compile('apacheds:apacheds-core-entry:1.5.4')
        compile('apacheds:apacheds-protocol-shared:1.5.4')
        compile('shared-ldap:shared-ldap:0.9.12')
        compile('shared-ldap:shared-ldap-constants:0.9.12')
        compile('commons-dbcp:commons-dbcp:1.4')
        compile('commons-pool:commons-pool:1.5.4')
        compile('commons-configuration:commons-configuration:1.9')

        runtime 'commons-codec:commons-codec:1.4',
                'commons-io:commons-io:1.4',
                'commons-lang:commons-lang:2.5',
                'org.apache.httpcomponents:httpcore:4.0.1',
                'org.apache.httpcomponents:httpcore-nio:4.0.1',
                'org.apache.httpcomponents:httpclient:4.0.1',
                'org.apache.httpcomponents:httpmime:4.0.1',
                'com.googlecode.json-simple:json-simple:1.1',
                'org.apache.tika:tika-core:0.9',
                'net.sourceforge.jexcelapi:jxl:2.6.12'

        runtime('mariadb-client:mariadb-java-client:1.1.5')

        compile('anet:anet-java-sdk:1.4.5')
        compile('prettytime:prettytime:2.1.3.Final')
    }

    plugins {
        build ":tomcat:7.0.47"

        compile ':cache:1.0.1'
        compile ":hibernate:3.6.10.4"
        compile ":joda-time:1.4"
        compile ":background-thread:1.6"
        compile ':mail:1.0', {
            excludes 'spring-test'
        }
        compile ":quartz:1.0.1"
        compile ':spring-security-core:1.2.7.3'
        compile ":spring-security-ldap:2.0-RC2"
        compile ":background-thread:1.6"
        compile ":tooltip:0.8"
        runtime ":jquery:1.8.3"
        runtime ":resources:1.1.6"
    }

    def deps = [
            "shared-asn1-0.9.12.jar",
            "shared-asn1-codec-0.9.12.jar",
            "shared-bouncycastle-reduced-0.9.12.jar",
            "shared-ldap-0.9.12.jar",
            "shared-ldap-constants-0.9.12.jar"]

    grails.war.dependencies = {
        fileset(dir: "lib") {
            for (pattern in deps) {
                println("Including " + pattern)
                include(name: pattern)
            }
        }
    }
}
