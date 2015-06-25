package com.interact.listen.pbx.findme

import org.joda.time.LocalDateTime
import com.interact.listen.PrimaryNode

class FindMeExpirationReminderJob {
    static triggers = {
        cron name: 'findMeExpirationReminderTrigger', cronExpression: '0 0/30 * * * ?'
    }

    def findMeExpirationReminderService

    def execute() {
        def hostName = InetAddress.getLocalHost().getHostName();
        def primaryNodeName = PrimaryNode.findAll().nodeName[0];
        def now = new LocalDateTime().withSecondOfMinute(0).withMillisOfSecond(0)
        def start = now.plusMinutes(30)
        def end = now.plusMinutes(60)

        FindMePreferences.findAllBySendReminderAndReminderNumberIsNotNull(true).each {
            if(hostName == primaryNodeName) {
                log.debug "Checking to see if user [${it.user}] should be reminded about Find Me expiring"
                if (it.shouldSendReminder(start, end)) {
                    findMeExpirationReminderService.sendReminder(it)
                }
            }
        }
    }
}
