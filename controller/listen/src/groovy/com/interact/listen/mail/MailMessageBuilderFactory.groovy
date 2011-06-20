package com.interact.listen.mail

import grails.plugin.mail.MailMessageBuilder
import org.springframework.mail.javamail.JavaMailSender

class MailMessageBuilderFactory {
    def mailSender
    def mailMessageContentRenderer

    MailMessageBuilder createBuilder(ConfigObject config) {
        println "Making awesome builder"
        new HeaderOverridingMailMessageBuilder(mailSender, config, mailMessageContentRenderer)
    }

    boolean isMimeCapable() {
        mailSender instanceof JavaMailSender
    }
}
