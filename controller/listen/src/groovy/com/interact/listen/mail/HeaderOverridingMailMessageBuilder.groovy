package com.interact.listen.mail

import grails.plugin.mail.MailMessageBuilder
import grails.plugin.mail.MailMessageContentRenderer
import org.apache.log4j.Logger
import org.springframework.mail.MailMessage
import org.springframework.mail.MailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.mail.javamail.MimeMailMessage

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
        def configuration = mailConfigurationService.getConfiguration()
        if(!configuration) {
            log.debug "No available mail configuration, using default values"
            return super.sendMessage()
        }

        def sender = new JavaMailSenderImpl()
        sender.defaultEncoding = 'utf-8'

        log.debug "Using configured host [${configuration.host}]"
        sender.host = configuration.host

        if(configuration.username) {
            log.debug "Using configured username [${configuration.username}]"
            sender.username = configuration.username
        }

        if(configuration.password) {
            log.debug "Using configured password [<protected>]"
            sender.password = configuration.password
        }

        def message = finishMessage()

        if(configuration.defaultFrom) {
            log.debug "Using configured defaultFrom [${configuration.defaultFrom}]"
            message.from = configuration.defaultFrom
        }

        sender.send(message instanceof MimeMailMessage ? message.mimeMessage : message)

        message
    }
}
