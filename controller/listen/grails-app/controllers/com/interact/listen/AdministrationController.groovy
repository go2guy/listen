package com.interact.listen

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
//import grails.plugins.springsecurity.Secured
import com.interact.listen.android.GoogleAuthConfiguration
import com.interact.listen.conferencing.ConferencingConfiguration
import com.interact.listen.history.*
import com.interact.listen.license.ListenFeature
import com.interact.listen.acd.*
import com.interact.listen.pbx.Extension
import com.interact.listen.pbx.NumberRoute
import com.interact.listen.voicemail.afterhours.AfterHoursConfiguration
import org.joda.time.LocalDateTime
import org.joda.time.Period
import org.apache.log4j.Logger

@Secured(['ROLE_ORGANIZATION_ADMIN'])
class AdministrationController {
    def applicationService
    def directMessageNumberService
    def extensionService
    def historyService
    def licenseService
    def realizeAlertUpdateService

    static allowedMethods = [
        index: 'GET',
        addDirectMessageNumber: 'POST',
        addException: 'POST',
        addExtension: 'POST',
        addInternalRoute: 'POST',
        addRestriction: 'POST',
        android: 'GET',
        calls: 'GET',
        callsData: 'GET',
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
            historyService.createdOutdialRestrictionException(exception)
            flash.successMessage = message(code: 'outdialRestrictionException.created.message')
            redirect(action: 'outdialing')
        } else {
            def model = outdialingModel()
            model.newException = exception
            render(view: 'outdialing', model: model)
        }
    }

    def addInternalRoute = {
        def route = new NumberRoute(params)
        route.type = NumberRoute.Type.INTERNAL
        route.organization = authenticatedUser.organization

        if(route.validate() && route.save()) {
            historyService.createdRoute(route)
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
        def restriction = new OutdialRestriction()
        restriction.pattern = params.pattern
        restriction.organization = authenticatedUser.organization
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
            historyService.createdOutdialRestriction(restriction)
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

    def calls = {
        render(view: 'calls')
    }

    def callsData = {
        def now = new LocalDateTime()
        def cutoff = new LocalDateTime().minusMinutes(25)
        def data = []
        CallData.withCriteria {
            or {
                isNull('ended')
                ge('ended', cutoff)
            }
        }.each { call ->
            data << [
                ani: call.ani,
                dnis: call.dnis,
                started: new Period(call.started, now).toStandardSeconds().seconds,
                ended: call.ended ? new Period(call.ended, now).toStandardSeconds().seconds : 0,
                sessionId: call.sessionId
            ]
        }
        render data as JSON
    }

    def configuration = {
        def organization = authenticatedUser.organization

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
        historyService.deletedOutdialRestrictionException(exception)
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
        historyService.deletedRoute(route)
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
        historyService.deletedOutdialRestriction(restriction)
        flash.successMessage = message(code: 'outdialRestriction.deleted.message')
        redirect(action: 'outdialing')
    }

    def skills = {
        
        log.debug "Lets access ACD skills"
        
        render(view: 'skills', model: skillModel())
    }
    
    def addSkill = {
        
        log.debug "Add new skill with params [${params}]"
        
        def organization = authenticatedUser.organization
        def skill = new Skill()
        
        skill.skillname = params.skillname
        skill.organization = organization
        skill.description = params.description

        if(skill.validate() && skill.save()) {
            historyService.createdSkill(skill)
            flash.successMessage = message(code: 'skill.created.message')
            redirect(action: 'skills')
        } else {
            def model = skillModel()
            model.newSkill = skill
            render(view: 'skills', model: model)
        }
    }

    def editSkill = {
        
        log.debug "Edit skill with params [${params}]"
        
        def organization = authenticatedUser.organization
        def skill = Skill.get(params.id)
        if(!skill) {
            flash.errorMessage = message(code: 'skill.notFound.message')
            redirect(action: 'skills')
            return
        }
        
        def orgUsers = User.findAllByOrganizationAndEnabled(organization, true, [sort: 'realName', order: 'asc'])
        def skillUsers = UserSkill.findAllBySkill(skill)
        
        render(view: 'editSkill', model: [skill: skill, orgUsers: orgUsers, skillUsers: skillUsers] )
    }

    def updateSkill = {
        
        log.debug "Update skill with params [${params}]"
        
        def skill = Skill.get(params.id)
        if(!skill) {
            flash.errorMessage = message(code: 'skill.notFound.message')
            redirect(action: 'skills')
            return
        }
        
        // Do the simple part and update the skill
        skill.skillname = params.skillname
        skill.description = params.description
        skill.save()
        
        // We're going to make a list of our skills so we can work with it easier
        def userIds = []
        if (params.userIds.getClass() == String) {
            userIds << params.userIds
        } else {
            params.userIds.each { id ->
                userIds << id
            }
        }
        
        log.debug "Skill [${skill.skillname}] is needed for userIds [${userIds}]"

        // loop through existing users.  Remove users that are no longer selected.  Remove users that we aleady have from the users list that we plan on adding
        def skillUsers = UserSkill.findAllBySkill(skill)
        skillUsers.each { skillUser ->
            
            if ( userIds.contains(skillUser.user.id.toString()) ) {
                log.debug "User [${skillUser.user.username}] already has skill [${skill.skillname}] and we are keeping it"
                // we'll remove it from the users list, since we already have it and don't need to add it to the db
                userIds.remove(skillUser.user.id.toString())
            } else {
                log.debug "User [${skillUser.user.username}] has skill [${skill.skillname}] and we need to delete it"
                historyService.deletedUserSkill(skillUser)
                skillUser.delete()
            }
        }
         
        // We should now be left with a list of users that has removed users that already have the skill, and we've deleted user skills from the db that are no longer selected
        userIds.each { userId ->
            
            def userskill = new UserSkill()
            userskill.user = User.findById(userId.toInteger())
            if(!userskill.user) {
                flash.errorMessage = message(code: 'user.notFound.message')
                redirect(action: 'skills')
                return
            }
            
            log.debug "Working to add skill [${skill.skillname}] to user [${userskill.user.username}]"

            userskill.skill = skill
            
            if(userskill.validate() && userskill.save()) {
                historyService.addedUserSkill(userskill)
            } else {
                log.error "Failed to add skill [${skill.skillname}] to user [${userskill.user.username}]"
            }
        }
        
        historyService.updatedSkill(skill)
        flash.successMessage = message(code: 'skill.updated.message')
        
        def model = skillModel()
        render(view: 'skills', model: model)
    }
    
    def deleteSkill = {

        log.debug "Delete skill with params [${params}]"
        
        def skill = Skill.get(params.id)
        if(!skill) {
            flash.errorMessage = message(code: 'skill.notFound.message')
            redirect(action: 'skills')
            return
        }

        skill.delete()
        historyService.deletedSkill(skill)
        flash.successMessage = message(code: 'skill.deleted.message')
        
        def model = skillModel()
        render(view: 'skills', model: model)
    }
    
    def history = {
        def organization = authenticatedUser.organization

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

        def oldIsEnabled = googleAuthConfiguration.isEnabled

        googleAuthConfiguration.properties['authUser', 'authPass', 'authToken', 'isEnabled'] = params

        if(googleAuthConfiguration.validate() && googleAuthConfiguration.save()) {
            if(oldIsEnabled != googleAuthConfiguration.isEnabled) {
                if(googleAuthConfiguration.isEnabled) {
                    historyService.enabledAndroidCloudToDevice()
                } else {
                    historyService.disabledAndroidCloudToDevice()
                }
            }

            flash.successMessage = message(code: 'googleAuthConfiguration.updated.message')
            redirect(action: 'android')
        } else {
            render(view: 'android', model: [googleAuthConfiguration: googleAuthConfiguration])
        }
    }

    def saveConfiguration = {
        def organization = authenticatedUser.organization

        def transcription = TranscriptionConfiguration.findByOrganization(organization)
        if(!transcription) {
            transcription = new TranscriptionConfiguration(organization: organization)
        }

        def oldTranscriptionIsEnabled = transcription.isEnabled
        def oldTranscriptionPhoneNumber = transcription.phoneNumber
        if(licenseService.canAccess(ListenFeature.VOICEMAIL) && licenseService.canAccess(ListenFeature.TRANSCRIPTION)) {
            // using .properties = params['transcription'] didnt work here, resorting to bindData()
            bindData(transcription, params['transcription'], 'isEnabled')
            bindData(transcription, params['transcription'], 'phoneNumber')
        }
        
        def afterHours = AfterHoursConfiguration.findByOrganization(organization)
        if(!afterHours) {
            afterHours = new AfterHoursConfiguration(organization: organization)
        }

        def originalMobilePhone = afterHours.mobilePhone
        def originalAlternateNumber = afterHours.alternateNumber
        def originalRealizeUrl = afterHours.realizeUrl
        def originalRealizeAlertName = afterHours.realizeAlertName
        if(licenseService.canAccess(ListenFeature.VOICEMAIL)) {
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
        }

        def conferencing = ConferencingConfiguration.findByOrganization(organization)
        if(!conferencing) {
            conferencing = new ConferencingConfiguration(organization: organization)
        }

        def originalPinLength = conferencing.pinLength
        if(licenseService.canAccess(ListenFeature.CONFERENCING)) {
            if(params['conferencing'].pinLength == '' || !params['conferencing'].pinLength.matches('^[0-9]*$')) {
                flash.errorMessage = message(code: 'conferencingConfiguration.pattern.matches.invalid')
                render(view: 'configuration', model: [transcription: transcription, afterHours: afterHours, conferencing: conferencing])   
            }
            bindData(conferencing, params['conferencing'], 'pinLength')
        }

        // TODO use a transaction
        if(transcription.validate() && transcription.save() && afterHours.validate() && afterHours.save() && conferencing.validate() && conferencing.save()) {

            if(licenseService.canAccess(ListenFeature.VOICEMAIL) && licenseService.canAccess(ListenFeature.TRANSCRIPTION)) {
                boolean wasJustEnabled = false
                if(oldTranscriptionIsEnabled != transcription.isEnabled) {
                    if(transcription.isEnabled) {
                        wasJustEnabled = true
                        historyService.enabledTranscription(transcription)
                    } else {
                        historyService.disabledTranscription()
                    }
                }

                if(!wasJustEnabled && oldTranscriptionPhoneNumber != transcription.phoneNumber) {
                    historyService.enabledTranscription(transcription)
                }
            }

            if(licenseService.canAccess(ListenFeature.VOICEMAIL)) {
                if(originalMobilePhone != afterHours.mobilePhone) {
                    historyService.changedAfterHoursMobileNumber(afterHours, originalMobilePhone)
                }

                if(originalAlternateNumber != afterHours.alternateNumber) {
                    realizeAlertUpdateService.sendUpdate(afterHours, originalAlternateNumber)
                    historyService.changedAfterHoursAlternateNumber(afterHours, originalAlternateNumber)
                }

                if(originalRealizeUrl != afterHours.realizeUrl || originalRealizeAlertName != afterHours.realizeAlertName) {
                    historyService.changedRealizeConfiguration(afterHours)
                }
            }

            if(licenseService.canAccess(ListenFeature.CONFERENCING)) {
                if(originalPinLength != conferencing.pinLength) {
                    historyService.changedNewConferencePinLength(conferencing, originalPinLength)
                }
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

        def oldTarget = exception.target
        def oldRestriction = exception.restriction

        exception.properties['target', 'restriction'] = params
        if(exception.validate() && exception.save()) {
            if(oldTarget != exception.target || oldRestriction != exception.restriction) {
                def fake = new Expando(target: oldTarget,
                                       restriction: oldRestriction)
                historyService.deletedOutdialRestrictionException(fake)
                historyService.createdOutdialRestrictionException(exception)
            }
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

        if(authenticatedUser.organization != route.organization) {
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

        if(authenticatedUser.organization != route.organization) {
            redirect(controller: 'login', action: 'denied')
            return
        }

        def oldDestination = route.destination
        def oldPattern = route.pattern

        route.properties['destination', 'label', 'pattern'] = params
        if(route.validate() && route.save()) {
            if(oldDestination != route.destination || oldPattern != route.pattern) {
                def fake = new Expando(type: NumberRoute.Type.INTERNAL,
                                       destination: oldDestination,
                                       pattern: oldPattern)
                historyService.deletedRoute(fake)
                historyService.createdRoute(route)
            }

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

        if(authenticatedUser.organization != restriction.organization) {
            redirect(controller: 'login', action: 'denied')
            return
        }

        def originalPattern = restriction.pattern
        def originalTarget = restriction.target

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
            if(originalPattern != restriction.pattern || originalTarget != restriction.target) {
                def fake = new Expando(pattern: originalPattern,
                                       target: originalTarget)
                historyService.deletedOutdialRestriction(fake)
                historyService.createdOutdialRestriction(restriction)
            }

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
        def organization = authenticatedUser.organization
        params.max = Math.min(params.max ? params.int('max') : 100, 100)
        params.sort = params.sort ?: 'number'
        params.order = params.order ?: 'asc'
        def extensionList = Extension.createCriteria().list(params) {
            owner {
                eq('organization', organization)
            }
        }
        def extensionTotal = Extension.createCriteria().get {
            projections {
                count('id')
            }
            owner {
                eq('organization', organization)
            }
        }
        return [
            extensionList: extensionList,
            extensionTotal: extensionTotal
        ]
    }

    private def outdialingModel() {
        def organization = authenticatedUser.organization
//        def globalRestrictions = GlobalOutdialRestriction.findAll([sort: 'pattern', order: 'asc'])
        def globalRestrictions = GlobalOutdialRestriction.createCriteria().list(){
            order('pattern','asc')
        }
        def restrictions = OutdialRestriction.findAllByOrganization(organization, [sort: 'pattern', order: 'asc'])
        def exceptions = OutdialRestrictionException.createCriteria().list([sort: 'restriction', order: 'asc']) {
            // TODO ultimately i would like to provide [sort: 'restriction.target'] to the list() method above. however,
            // theres a grails bug getting in the way:
            // http://jira.grails.org/browse/GRAILS-7324

            restriction {
                eq('organization', organization)
            }
        }
        def everyoneRestrictions = OutdialRestriction.findAllByOrganizationAndTarget(organization, null)
        return [
            globalRestrictions: globalRestrictions,
            restrictions: restrictions,
            everyoneRestrictions: everyoneRestrictions,
            exceptions: exceptions
        ]
    }

    private def skillModel() {
        def organization = authenticatedUser.organization
        log.debug "Checking skills for organization [${organization.name}]"
        def skills = Skill.findAllByOrganization(organization, [sort: 'skillname', order: 'asc'])
        log.debug "Organization [${organization.id}] has [${skills.size()}] skills configured"
        def skillsCount = []
        skills.each { skill ->
            def userskills = UserSkill.findAllBySkill(skill)
            def userCount = 0
            userskills.each { userskill ->
                if (userskill.user.enabled == true){
                    userCount++
                } else {
                    log.debug "Will not count user [${userskill.user.realName}] as they are disabled"
                }
            }
            skill.userCount = userCount
            skillsCount << skill
            log.debug "Organization [${organization.id}] has [${skill}] skill [${skill.userCount}]"
        }
        
        return [
            skills: skillsCount
        ] 
    }
    
    private def routingModel() {
        def organization = authenticatedUser.organization
        def external = NumberRoute.findAllByOrganizationAndType(organization, NumberRoute.Type.EXTERNAL, [sort: 'pattern', order: 'asc'])
        def internal = NumberRoute.findAllByOrganizationAndType(organization, NumberRoute.Type.INTERNAL, [sort: 'pattern', order: 'asc'])
        def directMessageNumbers = DirectMessageNumber.withCriteria {
            owner {
                eq('organization', organization)
            }
            order('number', 'asc')
        }
        def destinations = applicationService.listApplications()
        return [
            destinations: destinations,
            external: external,
            internal: internal,
            directMessageNumbers: directMessageNumbers
        ]
    }
}
