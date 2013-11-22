package com.interact.listen.voicemail

import com.interact.listen.User
import com.interact.listen.stats.Stat
import com.interact.listen.voicemail.afterhours.AfterHoursConfiguration
import org.joda.time.DateTime
import org.joda.time.Minutes
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

class NewVoicemailRecurringNotificationJob {
    static triggers = {
        cron name: 'newVoicemailNotificationTrigger', cronExpression: '0 0/1 * * * ?'
    }

    def statWriterService
    def voicemailNotificationService

    def execute() {
        def now = new DateTime()
        def preferences = VoicemailPreferences.findAllWhere(isSmsNotificationEnabled: true, recurringNotificationsEnabled: true)
        def afterHoursConfigs = AfterHoursConfiguration.findAll()
        preferences.each { pref ->
            // does the user have any new voicemails?
            def newVoicemails = Voicemail.findAllByOwnerAndIsNew(pref.user, true)
            log.debug "Found ${newVoicemails.size()} new voicemails for ${pref.user}"
            newVoicemails.each { voicemail ->
                
                afterHoursConfigs.each { config ->
                    if(checkIfPegStat(pref.user, config)) {
                        // Generate after hours stat right here!!
                        log.debug "After Hours voice mail is present since [${voicemail.dateCreated}], peg stat"
                        statWriterService.send(Stat.AFTER_HOURS_MSG_PRESENT)
                    }
                }
                def differenceInMinutes = Minutes.minutesBetween(voicemail.dateCreated, now).minutes
                log.debug "Minutes between [${voicemail.dateCreated}] and [${now}]: ${differenceInMinutes}"
                
                if(differenceInMinutes > 0 && differenceInMinutes % 10 == 0) {
                    sendSms(voicemail)
                    afterHoursConfigs.each { config ->
                        if(checkIfPegStat(pref.user, config)) {
                            if(config.alternateNumber && config.alternateNumber != '') {
                                log.debug "Sending alternate-number page to ${config.alternateNumber}"
                                voicemailNotificationService.sendNewVoicemailSms(voicemail, config.alternateNumber, Stat.NEW_VOICEMAIL_RECURRING_SMS_ALTERNATE)
                            }
                        }
                    }
                }
            }
        }
        
        checkAfterHoursVoicemails()
    }

    private boolean checkAfterHoursVoicemails() {
        def now = new DateTime()
        def afterHoursConfigs = AfterHoursConfiguration.findAll()
        def afterHoursUser = CH.config.com.interact.listen.afterHours.username
        //log.debug "Checking for after hours user based upon configured username[${afterHoursUser}]"
        afterHoursUser = User.findByUsername(afterHoursUser)
        if(!afterHoursUser) {
            log.warn "After hours subscriber is not found"
            return
        }
        
        def vmPreferences = VoicemailPreferences.findByUser(afterHoursUser)
        
        if(!vmPreferences){
            log.warn "Can't find preferences for [${afterHoursUser}]"
        }
        
        if(vmPreferences.isSmsNotificationEnabled && vmPreferences.recurringNotificationsEnabled) {
            // The logic within the above execute statement will catch after hours messages if these preferences are configured
            //log.info "After Hours processing should be taken care of by normal process"
            return
        }
        
        def newVoicemails = Voicemail.findAllByOwnerAndIsNew(afterHoursUser, true)
        log.debug "Found ${newVoicemails.size()} new voicemails for ${afterHoursUser}"
        newVoicemails.each { voicemail ->
            log.debug "After Hours voice mail is present since [${voicemail.dateCreated}], peg stat"
            statWriterService.send(Stat.AFTER_HOURS_MSG_PRESENT)
            
            def differenceInMinutes = Minutes.minutesBetween(voicemail.dateCreated, now).minutes
            log.debug "Minutes between [${voicemail.dateCreated}] and [${now}]: ${differenceInMinutes}"
            
            if(differenceInMinutes > 0 && differenceInMinutes % 10 == 0) {
                sendSms(voicemail)
                afterHoursConfigs.each { config ->
                    log.debug "We have AF config [${config?.alternateNumber}]"
                    if(config.alternateNumber && config.alternateNumber != '') {
                        log.debug "Sending alternate-number page to ${config.alternateNumber}"
                        voicemailNotificationService.sendNewVoicemailSms(voicemail, config.alternateNumber, Stat.NEW_VOICEMAIL_RECURRING_SMS_ALTERNATE)
                    }
                }
            }
        }
        
        //log.debug "Finished checking after hours"
    }
    
    private boolean checkIfPegStat(User user, AfterHoursConfiguration config) {
        log.debug "Check to see if we should peg stat for user [${user.username}]"
        
        def pegStat = false
        def afterHoursUser = config.mobilePhone?.owner
        if(!afterHoursUser) {
            afterHoursUser = CH.config.com.interact.listen.afterHours.username
            log.debug "Looking for after hours user based upon configured username[${afterHoursUser}]"
            afterHoursUser = User.findByUsername(afterHoursUser)
            if(!afterHoursUser) {
                log.warn "After hours subscriber is not found"
            }
        }
        
        if(user.username == afterHoursUser.username) {
            log.debug "Matched after hours user by user name"
            pegStat = true
        }
        
        return pegStat
    }
    
    private void sendSms(Voicemail voicemail) {
        log.debug "Sending recurring notification for user [${voicemail.owner}] for voicemail received [${voicemail.dateCreated}]"
        voicemailNotificationService.sendNewVoicemailSms(voicemail, null, Stat.NEW_VOICEMAIL_RECURRING_SMS)
    }
}
