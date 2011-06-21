package com.interact.listen.pbx.findme

import com.interact.listen.PhoneNumber
import grails.converters.JSON
import grails.plugins.springsecurity.Secured
import javax.servlet.http.HttpServletResponse as HSR

@Secured(['ROLE_FINDME_USER'])
class FindmeController {
    static allowedMethods = [
        index: 'GET',
        configure: 'GET',
        numberDetails: 'GET',
        save: 'POST'
    ]

    def springSecurityService

    def index = {
        redirect(action: 'configure')
    }

    def configure = {
        def user = springSecurityService.getCurrentUser()
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

        def user = springSecurityService.getCurrentUser()
        def phoneNumber = PhoneNumber.findByOwnerAndNumber(user, number)
        def forwardedTo = phoneNumber?.forwardedTo
        def canDial = user.canDial(forwardedTo ?: number)
        
        render(contentType: 'application/json') {
            delegate.isForwarded = forwardedTo ? true : false
            delegate.result = forwardedTo ?: number
            delegate.canDial = canDial
        }
    }

    def save = {
        def user = springSecurityService.getCurrentUser()

        // hacky value override since we declared it twice on the page, causing it to come in as an array
        params.expires = 'struct'
        log.debug "Saving Find Me configuration, params = ${params}"

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
        
        preferences.properties['expires', 'sendReminder'] = params
        if(params.smsNumber?.trim() == '') {
            preferences.reminderNumber = null
        } else {
            preferences.reminderNumber = params.smsNumber + '@' + params.smsProvider
        }

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
                flash.successMessage = 'Your Find Me / Follow Me configuration has been saved'
                redirect(action: 'configure')
            } else {
                status.setRollbackOnly()
                render(view: 'configure', model: [groups: groups, preferences: preferences])
            }
        }
    }
}
