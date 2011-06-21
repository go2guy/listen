package com.interact.listen

import grails.plugins.springsecurity.Secured
import com.interact.listen.history.*
import com.interact.listen.voicemail.afterhours.AfterHoursConfiguration

@Secured(['IS_AUTHENTICATED_FULLY'])
class ProfileController {
    def createPhoneNumberService
    def deletePhoneNumberService
    def realizeAlertUpdateService
    def springSecurityService
    def updatePhoneNumberService

    static allowedMethods = [
        index: 'GET',
        addUserPhoneNumber: 'POST',
        afterHours: 'GET',
        deleteUserPhoneNumber: 'POST',
        settings: 'GET',
        history: 'GET',
        phones: 'GET',
        saveAfterHours: 'POST',
        saveSettings: 'POST',
        updateSystemPhoneNumber: 'POST',
        updateUserPhoneNumber: 'POST'
    ]

    def index = {
        redirect(action: 'settings')
    }

    def addUserPhoneNumber = {
        def phoneNumber = createPhoneNumberService.createPhoneNumberByUser(params)
        if(phoneNumber.hasErrors()) {
            def model = phonesModel()
            model.newPhoneNumber = phoneNumber
            render(view: 'phones', model: model)
        } else {
            flash.successMessage = 'Phone saved'
            redirect(action: 'phones')
        }
    }

    def afterHours = {
        def user = springSecurityService.getCurrentUser()
        def afterHoursConfiguration = AfterHoursConfiguration.findByOrganization(user.organization)
        render(view: 'afterHours', model: [afterHoursConfiguration: afterHoursConfiguration])
    }

    def deleteUserPhoneNumber = {
        def phoneNumber = PhoneNumber.get(params.id)
        if(!phoneNumber) {
            flash.errorMessage = 'Phone not found'
            redirect(action: 'phone')
            return
        }

        deletePhoneNumberService.deleteUserPhoneNumberByUser(phoneNumber)
        flash.successMessage = 'Phone deleted'
        redirect(action: 'phones')
    }

    def settings = {
        def user = springSecurityService.getCurrentUser()
        render(view: 'settings', model: [user: user])
    }

    def history = {
        def user = springSecurityService.getCurrentUser()

        // TODO since there are two tables on the page, none of the sorting
        // is being used yet. also, both tables will page together (i.e. not separately).
        // we should investigate an elegant way to accomplish this. its likely that
        // these pages will be changed in the future to display the data in a way that is
        // more appropriate to how theyre supposed to be viewed. this should be considered
        // at that time.

        params.offset = params.offset ? params.int('offset') : 0
        params.max = Math.min(params.max ? params.int('max') : 25, 100)
        
        // call history
        def callHistoryList = CallHistory.createCriteria().list([offset: params.offset, max: params.max, sort: 'dateTime', order: 'desc']) {
            or {
                eq('fromUser', user)
                eq('toUser', user)
            }
        }
        def callHistoryTotal = CallHistory.createCriteria().get {
            projections {
                count('id')
            }
            or {
                eq('fromUser', user)
                eq('toUser', user)
            }
        }

        // action history
        def actionHistoryList = ActionHistory.createCriteria().list([offset: params.offset, max: params.max, sort: 'dateCreated', order: 'desc']) {
            or {
                eq('byUser', user)
                eq('onUser', user)
            }
        }
        def actionHistoryTotal = ActionHistory.createCriteria().get {
            projections {
                count('id')
            }
            or {
                eq('byUser', user)
                eq('onUser', user)
            }
        }
        render(view: 'history', model: [callHistoryList: callHistoryList, callHistoryTotal: callHistoryTotal, actionHistoryList: actionHistoryList, actionHistoryTotal: actionHistoryTotal])
    }

    def phones = {
        // TODO custodian users shouldnt really be able to get here, or to any other phone actions
        render(view: 'phones', model: phonesModel())
    }

    def saveAfterHours = {
        def user = springSecurityService.getCurrentUser()
        def afterHours = AfterHoursConfiguration.findByOrganization(user.organization)
        if(!afterHours) {
            flash.errorMessage = 'After Hours has not been configured by an administrator; alternate number cannot be set.'
            redirect(action: 'afterHours')
            return
        }

        def originalAlternateNumber = afterHours.alternateNumber
        if(params.alternateNumber?.trim() == '') {
            afterHours.alternateNumber = ''
        } else {
            afterHours.alternateNumber = params.alternateNumber + '@' + params.provider
        }

        if(afterHours.validate() && afterHours.save()) {
            if(originalAlternateNumber != afterHours.alternateNumber) {
                realizeAlertUpdateService.sendUpdate(afterHours, originalAlternateNumber)
            }

            flash.successMessage = 'After Hours alternate number updated'
            redirect(action: 'afterHours')
        } else {
            render(view: 'afterHours', model: [afterHoursConfiguration: afterHours])
        }
    }

    def saveSettings = {
        def user = springSecurityService.getCurrentUser()
        user.properties['realName', 'emailAddress', 'pass', 'confirm'] = params
        if(user.pass?.trim()?.length() > 0) {
            user.password = springSecurityService.encodePassword(user.pass)
        }

        if(user.validate() && user.save()) {
            flash.successMessage = 'Saved'
            redirect(action: 'settings')
        } else {
            render(view: 'settings', model: [user: user])
        }
    }

    def updateSystemPhoneNumber = {
        def phoneNumber = PhoneNumber.get(params.id)
        if(!phoneNumber) {
            flash.errorMessage = 'Phone not found'
            redirect(action: 'phone')
            return
        }

        phoneNumber = updatePhoneNumberService.updateSystemPhoneNumberByUser(phoneNumber, params)
        if(phoneNumber.hasErrors()) {
            def model = phonesModel()
            model.updatedPhoneNumber = phoneNumber
            render(view: 'phones', model: model)
        } else {
            flash.successMessage = 'Phone updated'
            redirect(action: 'phones')
        }
    }

    def updateUserPhoneNumber = {
        def phoneNumber = PhoneNumber.get(params.id)
        if(!phoneNumber) {
            flash.errorMessage = 'Phone not found'
            redirect(action: 'phone')
            return
        }

        phoneNumber = updatePhoneNumberService.updateUserPhoneNumberByUser(phoneNumber, params)
        if(phoneNumber.hasErrors()) {
            def model = phonesModel()
            model.updatedPhoneNumber = phoneNumber
            render(view: 'phones', model: model)
        } else {
            flash.successMessage = 'Phone saved'
            redirect(action: 'phones')
        }
    }

    private def phonesModel() {
        def user = springSecurityService.getCurrentUser()
        def systemPhoneNumberList = PhoneNumber.withCriteria {
            eq('owner', user)
            or {
                PhoneNumberType.systemTypes().each {
                    eq('type', it)
                }
            }
        }
        def userPhoneNumberList = PhoneNumber.withCriteria {
            eq('owner', user)
            or {
                PhoneNumberType.userTypes().each {
                    eq('type', it)
                }
            }
        }
        return [
            systemPhoneNumberList: systemPhoneNumberList,
            userPhoneNumberList: userPhoneNumberList
        ]
    }
}
