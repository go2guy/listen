package com.interact.listen.pbx.findme

import com.interact.listen.pbx.Extension
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
//import grails.plugins.springsecurity.Secured
import javax.servlet.http.HttpServletResponse as HSR

@Secured(['ROLE_FINDME_USER'])
class FindmeController {
    static allowedMethods = [
        index: 'GET',
        configure: 'GET',
        numberDetails: 'GET',
        save: 'POST'
    ]

    def historyService
    def springSecurityService

    def index = {
        redirect(action: 'configure')
    }

    def configure = {
        def user = springSecurityService.currentUser
        def preferences = FindMePreferences.findByUser(user)
        def groups = FindMeNumber.findAllByUserGroupedByPriority(user)
        render(view: 'configure', model: [groups: groups, preferences: preferences])
    }

    // ajax
    def numberDetails = {
        def number = params.number
        if(!number) {
            response.sendError(HSR.SC_NOT_FOUND)
            return
        }

        def user = springSecurityService.currentUser
        def extension = Extension.findByOwnerAndNumber(user, number)
        def forwardedTo = extension?.forwardedTo
        def canDial = user.canDial(forwardedTo ?: number)
        
        render(contentType: 'application/json') {
            delegate.isForwarded = forwardedTo ? true : false
            delegate.result = forwardedTo ?: number
            delegate.canDial = canDial
        }
    }

    def save = {
        def user = springSecurityService.currentUser

        // hacky value override since we declared it twice on the page, causing it to come in as an array
        
        params.expires = 'struct'
        log.debug "Saving Find Me configuration, params after = ${params}"
        def jsonGroups = JSON.parse(params.jsonGroups)
        def groups = []

        def priority = 1
        jsonGroups.each { jsonGroup ->
            def group = []
            jsonGroup.each { jsonDestination ->
                def number = new FindMeNumber(user: user, priority: priority)
                def map = [
                    'number': String.valueOf(jsonDestination.number),
                    'dialDuration': String.valueOf(jsonDestination.dialDuration),
                    'isEnabled': String.valueOf(jsonDestination.isEnabled)
                ]
                number.properties['number', 'dialDuration', 'isEnabled'] = map
                group << number
            }
            groups << group
            priority++
        }

        def preferences = FindMePreferences.findByUser(user)
        if(!preferences) {
            preferences = new FindMePreferences(user: user)
        }
        
        def oldExpires = preferences.expires
        def oldSendReminder = preferences.sendReminder
        def oldReminderNumber = preferences.reminderNumber

        preferences.properties['sendReminder'] = params
        
        preferences.expires = new DateTime(params?.expires_year.toInteger(), params?.expires_month.toInteger(), params?.expires_day.toInteger(), params?.expires_hour.toInteger(), params?.expires_minute.toInteger())
        
        if(params.smsNumber?.trim() == '') {
            preferences.reminderNumber = null
        } else {
            preferences.reminderNumber = params.smsNumber + '@' + params.smsProvider
        }
        
        def oldNumbers = FindMeNumber.findAllByUserForHistory(user)

        FindMeNumber.withTransaction { status ->
            boolean success = true

            FindMeNumber.removeAll(user)
            groups.each { group ->
                group.each { number ->
                    success = success && number.validate() && number.save()
                }
            }

            success = success && preferences.validate() && preferences.save()

            if(success) {

                def newNumbers = FindMeNumber.findAllByUserForHistory(user)
                if(oldNumbers != newNumbers) {
                    historyService.changedFindMeNumbers(user)
                }

                boolean wasJustEnabled = false
                if(oldSendReminder != preferences.sendReminder) {
                    if(preferences.sendReminder) {
                        historyService.enabledFindMeExpirationReminderSms(preferences)
                        wasJustEnabled = true
                    } else {
                        historyService.disabledFindMeExpirationReminderSms(preferences)
                    }
                }

                if(oldReminderNumber != preferences.reminderNumber && !wasJustEnabled) {
                    historyService.changedFindMeExpirationReminderSmsNumber(preferences, oldReminderNumber)
                }

                if(oldExpires != preferences.expires) {
                    historyService.changedFindMeExpiration(preferences)
                }

                flash.successMessage = 'Your Find Me / Follow Me configuration has been saved'
                redirect(action: 'configure')
            } else {
                log.error "Find Me / Follow Me configuration not saved"
                log.debug "Error preferences [${preferences.errors}]"
                status.setRollbackOnly()
                render(view: 'configure', model: [groups: groups, preferences: preferences])
            }
        }
    }
}
