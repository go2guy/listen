package com.interact.listen.mail

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

class MailConfiguration {
    String host = CH.config.grails.mail.host
    String username = CH.config.grails.mail.username
    String password = CH.config.grails.mail.password
    String defaultFrom = CH.config.grails.mail.default.from

    static constraints = {
        host blank: false
        username nullable: true, blank: true
        password nullable: true, blank: true
        defaultFrom nullable: true, blank: false
    }
}
