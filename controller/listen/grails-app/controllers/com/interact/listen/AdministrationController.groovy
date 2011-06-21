package com.interact.listen

import com.interact.listen.android.GoogleAuthConfiguration
import com.interact.listen.history.*
import com.interact.listen.pbx.NumberRoute
import com.interact.listen.voicemail.afterhours.AfterHoursConfiguration
import grails.plugins.springsecurity.Secured

@Secured(['ROLE_ORGANIZATION_ADMIN'])
class AdministrationController {
    def applicationService
    def createPhoneNumberService
    def deleteExtensionService
    def realizeAlertUpdateService
    def springSecurityService
    def updatePhoneNumberService

    static allowedMethods = [
        index: 'GET',
        addException: 'POST',
        addInternalRoute: 'POST',
        addPhoneNumber: 'POST',
        addRestriction: 'POST',
        android: 'GET',
        configuration: 'GET',
        deleteException: 'POST',
        deleteInternalRoute: 'POST',
        deletePhoneNumber: 'POST',
        deleteRestriction: 'POST',
        history: 'GET',
        outdialing: 'GET',
        phones: 'GET',
        routing: 'GET',
        saveAndroid: 'POST',
        saveConfiguration: 'POST',
        updateException: 'POST',
        updateExternalRoute: 'POST',
        updateInternalRoute: 'POST',
        updatePhoneNumber: 'POST',
        updateRestriction: 'POST',
        users: 'GET'
    ]

    def index = {
        redirect(action: 'routing')
    }

    def addException = {
        def exception = new OutdialRestrictionException()
        exception.properties['target', 'restriction'] = params

        if(exception.validate() && exception.save()) {
            flash.successMessage = 'Exception created'
            redirect(action: 'outdialing')
        } else {
            def model = outdialingModel()
            model.newException = exception
            render(view: 'outdialing', model: model)
        }
    }

    def addInternalRoute = {
        def user = springSecurityService.getCurrentUser()
        def route = new NumberRoute(params)
        route.type = NumberRoute.Type.INTERNAL
        route.organization = user.organization

        if(route.validate() && route.save()) {
            flash.successMessage = "Route ${route.pattern} created"
            redirect(action: 'routing')
        } else {
            def model = routingModel()
            model.newRoute = route
            render(view: 'routing', model: model)
        }
    }

    def addPhoneNumber = {
        def phoneNumber = createPhoneNumberService.createPhoneNumberByOperator(params)
        if(phoneNumber.hasErrors()) {
            def model = phonesModel()
            model.newPhoneNumber = phoneNumber
            render(view: 'phones', model: model)
        } else {
            flash.successMessage = 'Phone number created'
            redirect(action: 'phones')
        }
    }

    def addRestriction = {
        def user = springSecurityService.getCurrentUser()
        def restriction = new OutdialRestriction()
        restriction.pattern = params.pattern
        restriction.organization = user.organization
        if(params.target) {
            def target = User.get(params.target)
            if(!target) {
                def model = outdialingModel()
                model.newRestriction = restriction
                flash.errorMessage = 'Target user not found'
                render(view: 'outdialing', model: model)
                return
            }
            restriction.target = target
        }

        if(restriction.validate() && restriction.save()) {
            flash.successMessage = "Restriction for ${restriction.pattern} saved"
            redirect(action: 'outdialing')
        } else {
            def model = outdialingModel()
            model.newRestriction = restriction
            render(view: 'outdialing', model: model)
        }
    }

    def android = {
        def list = GoogleAuthConfiguration.list()
        def googleAuthConfiguration = list.size() > 0 ? list[0] : null
        render(view: 'android', model: [googleAuthConfiguration: googleAuthConfiguration])
    }

    def configuration = {
        def user = springSecurityService.getCurrentUser()
        def organization = user.organization

        def transcription = TranscriptionConfiguration.findByOrganization(organization)
        def afterHours = AfterHoursConfiguration.findByOrganization(organization)

        render(view: 'configuration', model: [transcription: transcription, afterHours: afterHours])
    }

    def deleteException = {
        def exception = OutdialRestrictionException.get(params.id)
        if(!exception) {
            flash.errorMessage = 'Exception not found'
            redirect(action: 'outdialing')
            return
        }

        exception.delete()
        flash.successMessage = 'Exception deleted'
        redirect(action: 'outdialing')
    }

    def deleteInternalRoute = {
        if(!params.id) {
            flash.errorMessage = 'Route not found'
            redirect(action: 'routing')
            return
        }

        def route = NumberRoute.get(params.id)
        if(!route) {
            flash.errorMessage = 'Route not found'
            redirect(action: 'routing')
            return
        }

        route.delete()
        flash.successMessage = 'Route deleted'
        redirect(action: 'routing')
    }

    def deletePhoneNumber = {
        if(!params.id) {
            flash.errorMessage = 'Phone not found'
            redirect(action: 'phones')
            return
        }

        def phoneNumber = PhoneNumber.get(params.id)
        if(!phoneNumber) {
            flash.errorMessage = 'Phone not found'
            redirect(action: 'phones')
            return
        }

        deleteExtensionService.deleteExtension(phoneNumber)
        flash.successMessage = 'Phone deleted'
        redirect(action: 'phones')
    }

    def deleteRestriction = {
        if(!params.id) {
            flash.errorMessage = 'Restriction not found'
            redirect(action: 'outdialing')
            return
        }

        def restriction = OutdialRestriction.get(params.id)
        if(!restriction) {
            flash.errorMessage = 'Restriction not found'
            redirect(action: 'outdialing')
            return
        }

        restriction.delete()
        flash.successMessage = 'Restriction deleted'
        redirect(action: 'outdialing')
    }

    def history = {
        def user = springSecurityService.getCurrentUser()
        def organization = user.organization

        params.offset = params.offset ? params.int('offset') : 0
        params.max = Math.min(params.max ? params.int('max') : 25, 100)

        // call history
        def callHistory = CallHistory.createCriteria().list([offset: params.offset, max: params.max, sort: 'dateTime', order: 'desc']) {
            eq('organization', organization)
        }
        def callHistoryCount = CallHistory.createCriteria().get {
            projections {
                count('id')
            }
            eq('organization', organization)
        }

        // action history
        def actionHistoryList = ActionHistory.createCriteria().list([offset: params.offset, max: params.max, sort: 'dateCreated', order: 'desc']) {
            or {
            eq('organization', organization)
            }
        }
        def actionHistoryTotal = ActionHistory.createCriteria().get {
            projections {
                count('id')
            }
            eq('organization', organization)
        }
        render(view: 'history', model: [callHistoryList: callHistory, callHistoryTotal: callHistoryCount, actionHistoryList: actionHistoryList, actionHistoryTotal: actionHistoryTotal])
    }

    def outdialing = {
        render(view: 'outdialing', model: outdialingModel())
    }

    def phones = {
        render(view: 'phones', model: phonesModel())
    }

    def routing = {
        render(view: 'routing', model: routingModel())
    }

    def saveAndroid = {
        def list = GoogleAuthConfiguration.list()
        def googleAuthConfiguration = list.size() > 0 ? list[0] : new GoogleAuthConfiguration()
        googleAuthConfiguration.properties['authUser', 'authPass', 'authToken', 'isEnabled'] = params

        if(googleAuthConfiguration.validate() && googleAuthConfiguration.save()) {
            flash.successMessage = 'Android Cloud-to-Device settings saved'
            redirect(action: 'android')
        } else {
            render(view: 'android', model: [googleAuthConfiguration: googleAuthConfiguration])
        }
    }

    def saveConfiguration = {
        def user = springSecurityService.getCurrentUser()
        def organization = user.organization

        def transcription = TranscriptionConfiguration.findByOrganization(organization)
        if(!transcription) {
            transcription = new TranscriptionConfiguration(organization: organization)
        }

        // using .properties = params['transcription'] didnt work here, resorting to bindData() :(
        bindData(transcription, params['transcription'], 'isEnabled')
        bindData(transcription, params['transcription'], 'phoneNumber')

        def afterHours = AfterHoursConfiguration.findByOrganization(organization)
        if(!afterHours) {
            afterHours = new AfterHoursConfiguration(organization: organization)
        }

        def originalAlternateNumber = afterHours.alternateNumber
        if(params['afterHours.phoneNumber.id'] == '') {
            afterHours.phoneNumber = null
        } else {
            bindData(afterHours, params['afterHours'], 'phoneNumber')
        }
        bindData(afterHours, params['afterHours'], 'realizeAlertName')
        bindData(afterHours, params['afterHours'], 'realizeUrl')
        if(params['afterHours'].alternateNumber?.trim() == '') {
            afterHours.alternateNumber = ''
        } else {
            afterHours.alternateNumber = params['afterHours'].alternateNumber + '@' + params['afterHours'].provider
        }

        // TODO use a transaction
        if(transcription.validate() && transcription.save() && afterHours.validate() && afterHours.save()) {
            if(originalAlternateNumber != afterHours.alternateNumber) {
                realizeAlertUpdateService.sendUpdate(afterHours, originalAlternateNumber)
            }

            flash.successMessage = 'Saved'
            redirect(action: 'configuration')
        } else {
            render(view: 'configuration', model: [transcription: transcription, afterHours: afterHours])
        }
    }

    def updateException = {
        def exception = OutdialRestrictionException.get(params.id)
        if(!exception) {
            flash.errorMessage = 'Exception not found'
            redirect(action: 'outdialing')
            return
        }

        exception.properties['target', 'restriction'] = params
        if(exception.validate() && exception.save()) {
            flash.successMessage = 'Exception updated'
            redirect(action: 'outdialing')
        } else {
            def model = outdialingModel()
            model.updatedException = exception
            render(view: 'outdialing', model: model)
        }
    }

    def updateExternalRoute = {
        if(!params.id) {
            flash.errorMessage = 'Route not found'
            redirect(action: 'routing')
            return
        }

        def route = NumberRoute.get(params.id)
        if(!route) {
            flash.errorMessage = 'Route not found'
            redirect(action: 'routing')
            return
        }

        def user = springSecurityService.getCurrentUser()
        if(user.organization != route.organization) {
            redirect(controller: 'login', action: 'denied')
            return
        }

        route.properties['destination', 'label'] = params
        if(route.validate() && route.save()) {
            flash.successMessage = 'Route updated'
            redirect(action: 'routing')
        } else {
            def model = routingModel()
            model.updatedRoute = route
            render(view: 'routing', model: model)
        }
    }

    def updateInternalRoute = {
        if(!params.id) {
            flash.errorMessage = 'Route not found'
            redirect(action: 'routing')
            return
        }

        def route = NumberRoute.get(params.id)
        if(!route) {
            flash.errorMessage = 'Route not found'
            redirect(action: 'routing')
            return
        }

        def user = springSecurityService.getCurrentUser()
        if(user.organization != route.organization) {
            redirect(controller: 'login', action: 'denied')
            return
        }

        route.properties['destination', 'label', 'pattern'] = params
        if(route.validate() && route.save()) {
            flash.successMessage = 'Route updated'
            redirect(action: 'routing')
        } else {
            def model = routingModel()
            model.updatedRoute = route
            render(view: 'routing', model: model)
        }
    }

    def updatePhoneNumber = {
        if(!params.id) {
            flash.errorMessage = 'Phone not found'
            redirect(action: 'phones')
            return
        }

        def phoneNumber = PhoneNumber.get(params.id)
        if(!phoneNumber) {
            flash.errorMessage = 'Phone not found'
            redirect(action: 'phones')
            return
        }

        phoneNumber = updatePhoneNumberService.updatePhoneNumberByOperator(phoneNumber, params)
        if(phoneNumber.hasErrors()) {
            def model = phonesModel()
            model.updatedPhoneNumber = phoneNumber
            render(view: 'phones', model: model)
        } else {
            flash.successMessage = 'Phone number saved'
            redirect(action: 'phones')
        }
    }

    def updateRestriction = {
        if(!params.id) {
            flash.errorMessage = 'Restriction not found'
            redirect(action: 'restrictions')
            return
        }

        def restriction = OutdialRestriction.get(params.id)
        if(!restriction) {
            flash.errorMessage = 'Restriction not found'
            redirect(action: 'restrictions')
            return
        }

        def user = springSecurityService.getCurrentUser()
        if(user.organization != restriction.organization) {
            redirect(controller: 'login', action: 'denied')
            return
        }

        restriction.pattern = params.pattern
        restriction.target = null
        if(params.target) {
            def target = User.get(params.target)
            if(!target) {
                def model = outdialingModel()
                model.newRestriction = restriction
                flash.errorMessage = 'Target user not found'
                render(view: 'outdialing', model: model)
                return
            }
            restriction.target = target
        }

        if(restriction.validate() && restriction.save()) {
            flash.successMessage = "Restriction for ${restriction.pattern} saved"
            redirect(action: 'outdialing')
        } else {
            def model = outdialingModel()
            model.updatedRestriction = restriction
            render(view: 'outdialing', model: model)
        }
    }

    def users = {
        render(view: 'users')
    }

    private def phonesModel() {
        params.max = Math.min(params.max ? params.int('max') : 100, 100)
        params.sort = params.sort ?: 'number'
        params.order = params.order ?: 'asc'
        def user = springSecurityService.getCurrentUser()
        def phoneNumbers = PhoneNumber.createCriteria().list(params) {
            owner {
                eq('organization', user.organization)
            }
        }
        def phoneNumberTotal = PhoneNumber.createCriteria().get {
            projections {
                count('id')
            }
            owner {
                eq('organization', user.organization)
            }
        }
        def users = User.findAllByOrganization(user.organization)
        return [
            phoneNumberList: phoneNumbers,
            phoneNumberTotal: phoneNumberTotal,
            users: users
        ]
    }

    private def outdialingModel() {
        def user = springSecurityService.getCurrentUser()
        def globalRestrictions = GlobalOutdialRestriction.findAll([sort: 'pattern', order: 'asc'])
        def restrictions = OutdialRestriction.findAllByOrganization(user.organization, [sort: 'pattern', order: 'asc'])
        def exceptions = OutdialRestrictionException.createCriteria().list([sort: 'restriction', order: 'asc']) {
            // TODO ultimately i would like to provide [sort: 'restriction.target'] to the list() method above. however,
            // theres a grails bug getting in the way:
            // http://jira.grails.org/browse/GRAILS-7324

            restriction {
                eq('organization', user.organization)
            }
        }
        def everyoneRestrictions = OutdialRestriction.findAllByOrganizationAndTarget(user.organization, null)
        def users = User.findAllByOrganization(user.organization, [sort: 'realName', order: 'asc'])
        return [
            globalRestrictions: globalRestrictions,
            restrictions: restrictions,
            everyoneRestrictions: everyoneRestrictions,
            exceptions: exceptions,
            users: users
        ]
    }

    private def routingModel() {
        def user = springSecurityService.getCurrentUser()
        def external = NumberRoute.findAllByOrganizationAndType(user.organization, NumberRoute.Type.EXTERNAL, [sort: 'pattern', order: 'asc'])
        def internal = NumberRoute.findAllByOrganizationAndType(user.organization, NumberRoute.Type.INTERNAL, [sort: 'pattern', order: 'asc'])
        def destinations = applicationService.listApplications()
        return [
            destinations: destinations,
            external: external,
            internal: internal
        ]
    }
}
