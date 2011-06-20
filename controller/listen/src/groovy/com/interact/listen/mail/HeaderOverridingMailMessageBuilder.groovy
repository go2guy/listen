package com.interact.listen.mail

import grails.plugin.mail.MailMessageBuilder
import grails.plugin.mail.MailMessageContentRenderer
import org.springframework.mail.MailMessage
import org.springframework.mail.MailSender

class HeaderOverridingMailMessageBuilder extends MailMessageBuilder {
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
}
