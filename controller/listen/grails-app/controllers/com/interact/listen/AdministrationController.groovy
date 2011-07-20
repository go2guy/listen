package com.interact.listen

import com.interact.listen.android.GoogleAuthConfiguration
import com.interact.listen.conferencing.ConferencingConfiguration
import com.interact.listen.history.*
import com.interact.listen.pbx.Extension
import com.interact.listen.pbx.NumberRoute
import com.interact.listen.voicemail.afterhours.AfterHoursConfiguration
import grails.plugins.springsecurity.Secured

@Secured(['ROLE_ORGANIZATION_ADMIN'])
class AdministrationController {
    def applicationService
    def directMessageNumberService
    def extensionService
    def realizeAlertUpdateService
    def springSecurityService

    static allowedMethods = [
        index: 'GET',
        addDirectMessageNumber: 'POST',
        addException: 'POST',
        addExtension: 'POST',
        addInternalRoute: 'POST',
        addRestriction: 'POST',
        android: 'GET',
        configuration: 'GET',
        deleteDirectMessageNumber: 'POST',
        deleteException: 'POST',
        deleteExtension: 'POST',
        deleteInternalRoute: 'POST',
        deleteRestriction: 'POST',
        history: 'GET',
        outdialing: 'GET',
        phones: 'GET',
        routing: 'GET',
        saveAndroid: 'POST',
        saveConfiguration: 'POST',
        updateDirectMessageNumber: 'POST',
        updateException: 'POST',
        updateExtension: 'POST',
        updateExternalRoute: 'POST',
        updateInternalRoute: 'POST',
        updateRestriction: 'POST',
        users: 'GET'
    ]

    def index = {
        redirect(action: 'routing')
    }

    def addDirectMessageNumber = {
        def directMessageNumber = directMessageNumberService.create(params)
        if(directMessageNumber.hasErrors()) {
            def model = routingModel()
            model.newDirectMessageNumber = directMessageNumber
            render(view: 'routing', model: model)
        } else {
            flash.successMessage = message(code: 'directMessageNumber.created.message')
            redirect(action: 'routing')
        }
    }

    def addException = {
        def exception = new OutdialRestrictionException()
        exception.properties['target', 'restriction'] = params

        if(exception.validate() && exception.save()) {
            flash.successMessage = message(code: 'outdialRestrictionException.created.message')
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
            flash.successMessage = message(code: 'numberRoute.created.message', args: [route.pattern])
            redirect(action: 'routing')
        } else {
            def model = routingModel()
            model.newRoute = route
            render(view: 'routing', model: model)
        }
    }

    def addExtension = {
        def extension = extensionService.create(params)
        if(extension.hasErrors()) {
            def model = phonesModel()
            model.newExtension = extension
            render(view: 'phones', model: model)
        } else {
            flash.successMessage = message(code: 'extension.created.message')
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
                flash.errorMessage = message(code: 'outdialRestriction.targetNotFound.message')
                render(view: 'outdialing', model: model)
                return
            }
            restriction.target = target
        }

        if(restriction.validate() && restriction.save()) {
            flash.successMessage = message(code: 'outdialRestriction.created.message', args: [restriction.pattern])
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
        def conferencing = ConferencingConfiguration.findByOrganization(organization)
        if(!conferencing) {
            conferencing = new ConferencingConfiguration(organization: organization)
        }

        render(view: 'configuration', model: [transcription: transcription, afterHours: afterHours, conferencing: conferencing])
    }

    def deleteDirectMessageNumber = {
        def directMessageNumber = DirectMessageNumber.get(params.id)
        if(!directMessageNumber) {
            flash.errorMessage = message(code: 'directMessageNumber.notFound.message')
            redirect(action: 'routing')
            return
        }

        directMessageNumberService.delete(directMessageNumber)
        flash.successMessage = message(code: 'directMessageNumber.deleted.message')
        redirect(action: 'routing')
    }

    def deleteException = {
        def exception = OutdialRestrictionException.get(params.id)
        if(!exception) {
            flash.errorMessage = message(code: 'outdialRestrictionException.notFound.message')
            redirect(action: 'outdialing')
            return
        }

        exception.delete()
        flash.successMessage = message(code: 'outdialRestrictionException.deleted.message')
        redirect(action: 'outdialing')
    }

    def deleteInternalRoute = {
        def route = NumberRoute.get(params.id)
        if(!route) {
            flash.errorMessage = message(code: 'numberRoute.notFound.message')
            redirect(action: 'routing')
            return
        }

        route.delete()
        flash.successMessage = message(code: 'numberRoute.deleted.message')
        redirect(action: 'routing')
    }

    def deleteExtension = {
        def extension = Extension.get(params.id)
        if(!extension) {
            flash.errorMessage = message(code: 'extension.notFound.message')
            redirect(action: 'phones')
            return
        }

        extensionService.delete(extension)
        flash.successMessage = message(code: 'extension.deleted.message')
        redirect(action: 'phones')
    }

    def deleteRestriction = {
        def restriction = OutdialRestriction.get(params.id)
        if(!restriction) {
            flash.errorMessage = message(code: 'outdialRestriction.notFound.message')
            redirect(action: 'outdialing')
            return
        }

        restriction.delete()
        flash.successMessage = message(code: 'outdialRestriction.deleted.message')
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
            flash.successMessage = message(code: 'googleAuthConfiguration.updated.message')
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
        if(params['afterHours.mobilePhone.id'] == '') {
            afterHours.mobilePhone = null
        } else {
            bindData(afterHours, params['afterHours'], 'mobilePhone')
        }
        bindData(afterHours, params['afterHours'], 'realizeAlertName')
        bindData(afterHours, params['afterHours'], 'realizeUrl')
        if(params['afterHours'].alternateNumber?.trim() == '') {
            afterHours.alternateNumber = ''
        } else {
            afterHours.alternateNumber = params['afterHours'].alternateNumber + '@' + params['afterHours'].provider
        }

        def conferencing = ConferencingConfiguration.findByOrganization(organization)
        if(!conferencing) {
            conferencing = new ConferencingConfiguration(organization: organization)
        }

        bindData(conferencing, params['conferencing'], 'pinLength')

        // TODO use a transaction
        if(transcription.validate() && transcription.save() && afterHours.validate() && afterHours.save() && conferencing.validate() && conferencing.save()) {
            if(originalAlternateNumber != afterHours.alternateNumber) {
                realizeAlertUpdateService.sendUpdate(afterHours, originalAlternateNumber)
            }

            flash.successMessage = message(code: 'default.saved.message')
            redirect(action: 'configuration')
        } else {
            render(view: 'configuration', model: [transcription: transcription, afterHours: afterHours, conferencing: conferencing])
        }
    }

    def updateDirectMessageNumber = {
        def directMessageNumber = DirectMessageNumber.get(params.id)
        if(!directMessageNumber) {
            flash.errorMessage = message(code: 'directMessageNumber.notFound.message')
            redirect(action: 'routing')
            return
        }

        directMessageNumber = directMessageNumberService.update(directMessageNumber, params)
        if(directMessageNumber.hasErrors()) {
            def model = routingModel()
            model.updatedDirectMessageNumber = directMessageNumber
            render(view: 'routing', model: model)
        } else {
            flash.successMessage = message(code: 'directMessageNumber.updated.message')
            redirect(action: 'routing')
        }
    }

    def updateException = {
        def exception = OutdialRestrictionException.get(params.id)
        if(!exception) {
            flash.errorMessage = message(code: 'outdialRestrictionException.notFound.message')
            redirect(action: 'outdialing')
            return
        }

        exception.properties['target', 'restriction'] = params
        if(exception.validate() && exception.save()) {
            flash.successMessage = message(code: 'outdialRestrictionException.updated.message')
            redirect(action: 'outdialing')
        } else {
            def model = outdialingModel()
            model.updatedException = exception
            render(view: 'outdialing', model: model)
        }
    }

    def updateExternalRoute = {
        def route = NumberRoute.get(params.id)
        if(!route) {
            flash.errorMessage = message(code: 'numberRoute.notFound.message')
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
            flash.successMessage = message(code: 'numberRoute.updated.message')
            redirect(action: 'routing')
        } else {
            def model = routingModel()
            model.updatedRoute = route
            render(view: 'routing', model: model)
        }
    }

    def updateInternalRoute = {
        def route = NumberRoute.get(params.id)
        if(!route) {
            flash.errorMessage = message(code: 'numberRoute.notFound.message')
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
            flash.successMessage = message(code: 'numberRoute.updated.message')
            redirect(action: 'routing')
        } else {
            def model = routingModel()
            model.updatedRoute = route
            render(view: 'routing', model: model)
        }
    }

    def updateExtension = {
        def extension = Extension.get(params.id)
        if(!extension) {
            flash.errorMessage = message(code: 'extension.notFound.message')
            redirect(action: 'phones')
            return
        }

        extension = extensionService.update(extension, params)
        if(extension.hasErrors()) {
            def model = phonesModel()
            model.updatedExtension = extension
            render(view: 'phones', model: model)
        } else {
            flash.successMessage = message(code: 'extension.updated.message')
            redirect(action: 'phones')
        }
    }

    def updateRestriction = {
        def restriction = OutdialRestriction.get(params.id)
        if(!restriction) {
            flash.errorMessage = message(code: 'outdialRestriction.notFound.message')
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
                flash.errorMessage = message(code: 'outdialRestriction.targetNotFound.message')
                render(view: 'outdialing', model: model)
                return
            }
            restriction.target = target
        }

        if(restriction.validate() && restriction.save()) {
            flash.successMessage = message(code: 'outdialRestriction.updated.message', args: [restriction.pattern])
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
        def extensionList = Extension.createCriteria().list(params) {
            owner {
                eq('organization', user.organization)
            }
        }
        def extensionTotal = Extension.createCriteria().get {
            projections {
                count('id')
            }
            owner {
                eq('organization', user.organization)
            }
        }
        def users = User.findAllByOrganization(user.organization, [sort: 'realName', order: 'asc'])
        return [
            extensionList: extensionList,
            extensionTotal: extensionTotal,
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
        def directMessageNumbers = DirectMessageNumber.withCriteria {
            owner {
                eq('organization', user.organization)
            }
            order('number', 'asc')
        }
        def users = User.findAllByOrganization(user.organization, [sort: 'realName', order: 'asc'])
        def destinations = applicationService.listApplications()
        return [
            destinations: destinations,
            external: external,
            internal: internal,
            directMessageNumbers: directMessageNumbers,
            users: users
        ]
    }
}
