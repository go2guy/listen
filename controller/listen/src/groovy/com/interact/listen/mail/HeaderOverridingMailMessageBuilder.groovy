package com.interact.listen.mail

import grails.plugin.mail.MailMessageBuilder
import grails.plugin.mail.MailMessageContentRenderer
import org.apache.log4j.Logger
import org.springframework.mail.MailMessage
import org.springframework.mail.MailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.mail.javamail.MimeMailMessage

// We've defined this overriding class because we desired to support the sending of email by organization, which means that we also desired to be able to send
// email notifications through various organization's email servers.  By default, the mail grails plugin only allows host, protocol, properties to be set via the config.groovy,
// so by overriding this class we are providing a more configurable solution.

class HeaderOverridingMailMessageBuilder extends MailMessageBuilder {

    def mailConfigurationService

    private static final Logger log = Logger.getLogger(HeaderOverridingMailMessageBuilder.class)

    private def headers = [:]

    HeaderOverridingMailMessageBuilder(MailSender mailSender, ConfigObject config, MailMessageContentRenderer mailMessageContentRenderer = null) {
        super(mailSender, config, mailMessageContentRenderer)
    }

    void headers(Map hdrs) {
        super.headers(hdrs)
        hdrs.each { k, v ->
            def kstr = k?.toString()
            def vstr = v?.toString()
            if(kstr?.length() > 0 && vstr?.length() > 0) {
                headers[k] = v
            }
        }
    }

    MailMessage finishMessage() {
        def message = super.finishMessage()
        headers.each { k, v ->
            message.mimeMessageHelper.mimeMessage.setHeader(k, v)
        }
        return message
    }

    MailMessage sendMessage() {
        log.debug "Overriding sendMessage"
        
        def configuration = mailConfigurationService.getConfiguration()
        if(!configuration) {
            log.debug "No available mail configuration, using default values"
            return super.sendMessage()
        }
        
        log.debug "get protocol             [${mailSender.getProtocol()}]"
        log.debug "get host                 [${mailSender.getHost()}]"
        log.debug "get port                 [${mailSender.getPort()}]"
        log.debug "get password             [<protected>]"
        log.debug "get username             [${mailSender.getUsername()}]"
        log.debug "get java mail properties [${mailSender.getJavaMailProperties()}]"
        
        mailSender.setDefaultEncoding('utf-8')

        log.debug "Using configured email host [${configuration.host}]"
        mailSender.setHost(configuration.host)

        if(configuration.username) {
            log.debug "Using configured username [${configuration.username}]"
            mailSender.setUsername(configuration.username)
        }

        if(configuration.password) {
            log.debug "Using configured password [<protected>]"
            mailSender.setPassword(configuration.password)
        }

        if(configuration?.protocol) {
            log.debug "Using configured email protocol [${configuration.protocol}]"
            mailSender.setProtocol(configuration.protocol)
        }
        else {
            log.debug "No overriding protocol has been configured using default [${mailSender.getProtocol()}]"
        }
        
        if(configuration?.port) {
            log.debug "Using configured email port [${configuration.port}]"
            mailSender.setPort(configuration.port.toInteger())
        }
        else if (mailSender.getProtocol() == "smtp") {
            log.debug "Setting default smtp email port [587]"
            mailSender.setPort(587)
        }
                
        def message = finishMessage()

        if(configuration.defaultFrom) {
            log.debug "Using configured defaultFrom [${configuration.defaultFrom}]"
            message.from = configuration.defaultFrom
        }

        if (log.traceEnabled) {
            log.trace("Sending mail ${getDescription(message)}} ...")
        }

        log.debug "Using protocol     [${mailSender.getProtocol()}]"
        log.debug "Using host         [${mailSender.getHost()}]"
        log.debug "Using port         [${mailSender.getPort()}]"
        log.debug "Using password     [<protected>]"
        log.debug "Using username     [${mailSender.getUsername()}]"
        log.debug "Using username     [${mailSender.getJavaMailProperties()}]"
        
        mailSender.send(message instanceof MimeMailMessage ? message.mimeMessage : message)

        if (log.traceEnabled) {
            log.trace("Sent mail ${getDescription(message)}} ...")
        }

        message
    }
}
