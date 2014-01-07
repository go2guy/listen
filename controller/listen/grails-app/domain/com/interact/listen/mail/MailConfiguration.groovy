package com.interact.listen.mail

import grails.util.Holders

class MailConfiguration
{
    String host = Holders.config.grails.mail.host
    String username = Holders.config.grails.mail.username
    String password = Holders.config.grails.mail.password
    String defaultFrom = Holders.config.grails.mail.default.from
    String protocol
    Integer port

    static transients = ['protocol', 'port']
    
    static constraints =
    {
        host blank: false
        username nullable: true, blank: true
        password nullable: true, blank: true
        defaultFrom nullable: true, blank: false
    }
}
