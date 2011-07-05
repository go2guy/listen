package com.interact.listen.mail

import grails.plugin.mail.MailMessageBuilder
import org.springframework.mail.javamail.JavaMailSender

class MailMessageBuilderFactory {
    def mailConfigurationService
    def mailSender
    def mailMessageContentRenderer

    MailMessageBuilder createBuilder(ConfigObject config) {
        def builder = new HeaderOverridingMailMessageBuilder(mailSender, config, mailMessageContentRenderer)
        builder.mailConfigurationService = mailConfigurationService
        return builder
    }

    boolean isMimeCapable() {
        mailSender instanceof JavaMailSender
    }
}
