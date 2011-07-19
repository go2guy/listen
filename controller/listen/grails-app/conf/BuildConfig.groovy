grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"
grails.project.war.file = "target/listen-controller.war"
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        grailsPlugins()
        grailsHome()
        grailsCentral()

        mavenRepo "http://megatron:8081/nexus/content/groups/public"

        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        //mavenLocal()
        //mavenCentral()
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        compile 'org.mortbay.jetty:jetty:6.1.24',
                'org.mortbay.jetty:jetty-util:6.1.24'

        test 'mysql:mysql-connector-java:5.1.13'

        runtime 'commons-codec:commons-codec:1.4',
                'commons-io:commons-io:1.4',
                'commons-lang:commons-lang:2.5',
                'org.apache.httpcomponents:httpcore:4.0.1',
                'org.apache.httpcomponents:httpcore-nio:4.0.1',
                'org.apache.httpcomponents:httpclient:4.0.1',
                'org.apache.httpcomponents:httpmime:4.0.1',
                'com.googlecode.json-simple:json-simple:1.1',
                'org.apache.tika:tika-core:0.9',
                'org.apache.tika:tika-parsers:0.9'
    }
}
