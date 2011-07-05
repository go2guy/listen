package com.interact.listen.mail

class MailConfigurationService {
    MailConfiguration getConfiguration() {
        def list = MailConfiguration.list()
        return list.size() == 0 ? null : list[0]
    }
}
