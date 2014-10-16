package com.interact.listen

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import com.interact.listen.android.GoogleAuthConfiguration
import com.interact.listen.conferencing.ConferencingConfiguration
import com.interact.listen.history.*
import com.interact.listen.license.ListenFeature
import com.interact.listen.acd.*
import com.interact.listen.attendant.action.RouteToAnACDAction
import com.interact.listen.pbx.Extension
import com.interact.listen.pbx.NumberRoute
import com.interact.listen.voicemail.afterhours.AfterHoursConfiguration
import org.joda.time.LocalDateTime
import org.joda.time.Period

@Secured(['ROLE_ORGANIZATION_ADMIN'])
class AdministrationController {
    def applicationService
    def directInwardDialNumberService
    def directMessageNumberService
    def extensionService
    def historyService
    def licenseService
    def realizeAlertUpdateService

    static allowedMethods = [
        index: 'GET',
        addDirectInwardDialNumber: 'POST',
        addDirectMessageNumber: 'POST',
        addException: 'POST',
        addExtension: 'POST',
        addInternalRoute: 'POST',
        addRestriction: 'POST',
        android: 'GET',
        calls: 'GET',
        callsData: 'GET',
        configuration: 'GET',
        deleteDirectInwardDialNumber: 'POST',
        deleteDirectMessageNumber: 'POST',
        deleteException: 'POST',
        deleteExtension: 'POST',
        deleteInternalRoute: 'POST',
        deleteRestriction: 'POST',
        history: 'GET',
        outdialing: 'GET',
        phones: 'GET',
        pollAvailableUsers: 'POST',
        routing: 'GET',
        saveAndroid: 'POST',
        saveConfiguration: 'POST',
        updateDirectMessageNumber: 'POST',
        updateDirectInwardDialNumber: 'POST',
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

    def addDirectInwardDialNumber = {
        log.debug "addDirectInwardDialNumber with params [${params}]"
        /*
        def numberRoute = NumberRoute.get(params.id)
        if(!numberRoute){
            log.error "No number route found for [${params}]"
            flash.errorMessage = message(code: 'directInwardDialNumber.notFound.message')
            redirect(action: 'routing')
            return
        }
        */
        /*
        if(!params.owner.id){
            log.error "No owner was selected for direct message number add [${params}]"
            flash.errorMessage = message(code: 'directInwardDialNumber.owner.notProvided.message')
            redirect(action: 'routing')
            return
        }
        */
        //params.number = numberRoute.pattern
        log.debug "Add number patter [${params.number}] to params"
        
        def directInwardDialNumber = directInwardDialNumberService.create(params)
        if(directInwardDialNumber.hasErrors()) {
            def model = routingModel()
            model.newDirectInwardDialNumber = directInwardDialNumber
            render(view: 'routing', model: model)
        } else {
            flash.successMessage = message(code: 'directInwardDialNumber.created.message')
            redirect(action: 'routing')
        }
    }
    
    def addDirectMessageNumber = {
        log.debug "addDirectMessageNumber with params [${params}]"
        /*
        def numberRoute = NumberRoute.get(params.id)
        if(!numberRoute){
            log.error "No number route found for [${params}]"
            flash.errorMessage = message(code: 'directMessageNumber.notFound.message')
            redirect(action: 'routing')
            return
        }
        */
        /*
        if(!params.owner.id){
            log.error "No owner was selected for direct message number add [${params}]"
            flash.errorMessage = message(code: 'directMessageNumber.owner.notProvided.message')
            redirect(action: 'routing')
            return
        }
        */
        //params.number = numberRoute.pattern
        log.debug "Add number patter [${params.number}] to params"
        
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
        log.debug "addInternalRoute with params [${params}]"
        def route = new NumberRoute(params)
        route.type = NumberRoute.Type.INTERNAL
        route.organization = authenticatedUser.organization

        if(route.validate() && route.save()) {
            log.debug "We've saved a new internal route"
            historyService.createdRoute(route)
            flash.successMessage = message(code: 'numberRoute.created.message', args: [route.pattern])
            redirect(action: 'routing')
        } else {
            log.debug "addInternalRoute failed validation [${route.errors}]"
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

        render(view: 'configuration', model: [transcription: transcription, afterHours: afterHours, conferencing: conferencing, organization: organization])
    }

    def deleteDirectInwardDialNumber = {
        def directInwardDialNumber = DirectInwardDialNumber.get(params.id)
        if(!directInwardDialNumber) {
            flash.errorMessage = message(code: 'directInwardDialNumber.notFound.message')
            redirect(action: 'routing')
            return
        }

        directInwardDialNumberService.delete(directInwardDialNumber)
        flash.successMessage = message(code: 'directInwardDialNumber.deleted.message')
        redirect(action: 'routing')
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
        skill.onHoldMsg = ''
        skill.onHoldMsgExtended = ''
        skill.onHoldMusic = ''
        skill.connectMsg = ''

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
        def testUsers = []
        // users not already serving as a voicemail user
        def freeUsers = []
        skillUsers.each{skilluser ->
            log.debug "User [${skilluser.user}][${skilluser.user.realName}] [${skilluser.user.id}] has skill [${skilluser.skill.skillname}]"
            testUsers << skilluser.user
            // if user is not already serving as a voicemail user
            if ( skilluser.user.acdUserStatus.acdQueueStatus != AcdQueueStatus.VoicemailBox ) {
              // they are free to be assigned as one
              log.debug "Adding userskill [${skilluser}] to freeUsers."
              freeUsers << skilluser
            }
        }
       
        def vmUser = AcdService.getVoicemailUserBySkillname(skill.skillname)
        // We still want to keep the currently assigned voicemail user on the list of assignable users
        freeUsers << UserSkill.findByUser(vmUser)
 
        if(vmUser) {
            log.debug "Skill [${skill.skillname}] is assigned to user [${vmUser.realName}] for voicemail"
        }
        
        render(view: 'editSkill', model: [skill: skill, orgUsers: orgUsers, skillUsers: skillUsers, freeUsers: freeUsers, vmUser: vmUser, testUsers: testUsers] )
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
        
        skill.onHoldMsg = filterACDPrompts(params?.onHoldMsg)
        log.debug "set onHoldMsg to [${skill.onHoldMsg}]"
        
        skill.onHoldMsgExtended = filterACDPrompts(params?.onHoldMsgExtended)
        log.debug "set onHoldMsgExtended to [${skill.onHoldMsgExtended}]"
        
        skill.onHoldMusic = filterACDPrompts(params?.onHoldMusic)
        log.debug "set onHoldMusic to [${skill.onHoldMusic}]"
        
        skill.connectMsg = filterACDPrompts(params?.connectMsg)
        log.debug "set connectMsg to [${skill.connectMsg}]"
        
        if(skill.validate() && skill.save()) {
            log.debug "We've saved the changes to the skill record"
        } else {
            log.error "We've failed to validate skill prior to saving: [${skill.errors}]"
        }

        // We want to mark the history as soon as any portion of the skill was updated
        // this doesn't mean the action was successful as a whole.
        historyService.updatedSkill(skill)
 
        // We're going to make a list of our skills so we can work with it easier
        // The first step is to get the user ids selected on the edit skill screen
        def userIds = []
        if (params.userIds.getClass() == String) {
            userIds << params.userIds
        } else {
            params.userIds.each { id ->
                userIds << id
            }
        }
        
        log.debug "Skill [${skill.skillname}] is needed for userIds [${userIds}]"

        // loop through existing users.  Remove users that are no longer selected.  Remove users that already have the skill (no use adding them again)
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

        // remove the previous voicemail user
        AcdService.deleteVoicemailBox(Skill.findBySkillname(skill))
        
        // If a voicemail user was selected make sure they are assigned the current skill
        if ( params.vmUserId != "" && params.vmUserId != null ) {
          def isAssociated = false
          def vmUser
          vmUser = User.findById(params.vmUserId.toInteger())
          UserSkill.findAllBySkill(skill).each() { userSkill ->
            if ( userSkill?.user?.id == vmUser?.id ) {
              isAssociated = true
            }
          }

          // if there was a previous voicemail user for the skill we need to remove it
          // def previousVoicemailUser = AcdService.getVoicemailUserBySkillname(skill)
          // if ( vmUser?.id != previousVoicemailUser.id ) {
            // AcdService.deleteVoicemailBox(Skill.findBySkillname(skill))
          // }

          // if the desired voicemail user is eligible to become a voicemail user, then make it happen
          if ( isAssociated ) {
            if (vmUser) {
                log.debug "We have vmUserId of [${params.vmUserId}] for user [${vmUser.username}]"
                AcdService.setVoicemailUserBySkillname(skill, vmUser)
            } else {
                log.error "vmUserId [${params.vmUserId}] is invalid!"
            }
          }
          else {
            flash.errorMessage = message(code: 'skill.vmumissingskill.message')
            redirect(action: 'skills')
          }
        }
          
        flash.successMessage = message(code: 'skill.updated.message')
       
        def model = skillModel()
        render(view: 'skills', model: model)
    }
    
    private filterACDPrompts( String promptName ) {
        if(promptName) {
            if(promptName == "-- No Prompt --")
                return ''
            else
                return promptName
        } else {
            return ''
        }
    }
    
    def deleteSkill = {

        log.debug "Delete skill with params [${params}]"
        
        def skill = Skill.get(params.id)
        if(!skill) {
            flash.errorMessage = message(code: 'skill.notFound.message')
            redirect(action: 'skills')
            return
        }
        
        // Check to see if there are any actions configured with this skill id
        def menuCount = RouteToAnACDAction.countBySkill(skill)
        if (menuCount > 0) {
            log.info "Found [${menuCount}] actions associated with skill [${skill.skillname}]"
            flash.errorMessage = message(code: 'page.administration.acd.skills.assocaitedWithMenu.message')
            redirect(action: 'skills')
            return
        }
        
        log.debug "Actually delete skill [${skill.skillname}]"
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

    // ajax
    def pollAvailableUsers = {
      log.debug "pollAvailableUsers: selected [${params.selected}]"

      // users not already serving as a voicemail user
      def freeUsers = []
      def selectedUser
      def currentVoicemailUser = AcdService.getVoicemailUserBySkillname(params.skill)
      log.debug "current voicemail user [${currentVoicemailUser?.realName}]"

      params.selected.split(",").each() {username ->
        selectedUser = User.findByRealName(username)
        log.debug "Checking whether user [${selectedUser?.realName}] is free"
        if ( selectedUser?.acdUserStatus?.acdQueueStatus != AcdQueueStatus?.VoicemailBox ||
             selectedUser?.id == currentVoicemailUser?.id ) {
          // bypassing grails render error (concerning json subfields)
          log.debug "Adding user [${selectedUser?.realName}] to free users."
          def user = [:]
          user.id = selectedUser?.id
          user.realName = selectedUser?.realName
          freeUsers << user
        }
      }

      def data = [:]
      data.voicemailUsers = freeUsers
      data.currentVoicemailUser = currentVoicemailUser?.realName

      render data as JSON
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
        log.debug "Saving administration configuration [${params}]"
        def organization = authenticatedUser.organization

        def transcription = TranscriptionConfiguration.findByOrganization(organization)
        if(!transcription) {
            transcription = new TranscriptionConfiguration(organization: organization)
        }

        def oldTranscriptionIsEnabled = transcription.isEnabled
        def oldTranscriptionPhoneNumber = transcription.phoneNumber
        if(licenseService.canAccess(ListenFeature.VOICEMAIL) && licenseService.canAccess(ListenFeature.TRANSCRIPTION)) {
            bindData(transcription, params['transcription'], [include: ['isEnabled']])
            bindData(transcription, params['transcription'], [include: ['phoneNumber']])
        }

        def afterHours = AfterHoursConfiguration.findByOrganization(organization)
        if(!afterHours) {
            afterHours = new AfterHoursConfiguration(organization: organization)
        }

        if(licenseService.canAccess(ListenFeature.AFTERHOURS)) {
            log.debug "After hourse is licensed, saving configuration"
            def originalMobilePhone = afterHours.mobilePhone
            def originalAlternateNumber = afterHours.alternateNumber
            def originalRealizeUrl = afterHours.realizeUrl
            def originalRealizeAlertName = afterHours.realizeAlertName

            if(params['afterHours.mobilePhone.id'] == '') {
                afterHours.mobilePhone = null
            } else {
                bindData(afterHours, params['afterHours'], [include: ['mobilePhone']])
            }
            bindData(afterHours, params['afterHours'], [include: ['realizeAlertName']])
            bindData(afterHours, params['afterHours'], [include: ['realizeUrl']])
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
            log.debug "Conferencing is licensed, saving configuration"
            if(params['conferencing'].pinLength == '' || !params['conferencing'].pinLength.matches('^[0-9]*$')) {
                flash.errorMessage = message(code: 'conferencingConfiguration.pattern.matches.invalid')
                render(view: 'configuration', model: [transcription: transcription, afterHours: afterHours, conferencing: conferencing, organization: organization])
            }
            bindData(conferencing, params['conferencing'], [include: ['pinLength']])
        }

        def originalExtLength = organization.extLength
        if(licenseService.canAccess(ListenFeature.IPPBX)) {
            log.debug "IPPBX is licensed, saving configuration [${params['organization'].extLength}]"
            if(params['organization'].extLength == '' || !params['organization'].extLength.matches('^[0-9]*$')) {
                flash.errorMessage = message(code: 'organizationConfiguration.extLength.pattern.matches.invalid')
                render(view: 'configuration', model: [transcription: transcription, afterHours: afterHours, conferencing: conferencing, organization: organization])
            }
            bindData(organization, params['organization'], [include: ['extLength']])
        }

        // TODO use a transaction
        if(licenseService.canAccess(ListenFeature.AFTERHOURS) && afterHours.validate() && afterHours.save()) {
            log.debug "Saving after hourse configuration successful"
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
        } else if (licenseService.canAccess(ListenFeature.AFTERHOURS)) {
            log.debug "Didn't save the after hours configuration"
            render(view: 'configuration', model: [transcription: transcription, afterHours: afterHours, conferencing: conferencing, organization: organization])
        }

        if(transcription.validate() && transcription.save() && conferencing.validate() && conferencing.save() && organization.validate() && organization.save()) {
            log.debug "Saving transcription, conferencing, and organization configuration successful"
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

            if(licenseService.canAccess(ListenFeature.CONFERENCING)) {
                if(originalPinLength != conferencing.pinLength) {
                    historyService.changedNewConferencePinLength(conferencing, originalPinLength)
                }
            }

            if(licenseService.canAccess(ListenFeature.IPPBX)) {
                if(originalExtLength != organization.extLength) {
                    historyService.changedOrganizationExtLength(organization, originalExtLength)
                }
            }

            flash.successMessage = message(code: 'organizationConfiguration.saved.message')
            redirect(action: 'configuration')
        } else {
            log.debug "Didn't save any of the configuration"
            render(view: 'configuration', model: [transcription: transcription, afterHours: afterHours, conferencing: conferencing, organization: organization])
        }
    }

    def updateDirectInwardDialNumber = {
        log.debug "updateDirectInwardDialNumber with params [${params}]"
        def directInwardDialNumber = DirectInwardDialNumber.get(params.id)
        if(!directInwardDialNumber) {
            log.error "Couldn't find direct inward number [${params}]"
            flash.errorMessage = message(code: 'directInwardDialNumber.notFound.message')
            redirect(action: 'routing')
            return
        }

        log.debug "Found did [${directInwardDialNumber.number}]"
        
        directInwardDialNumber = directInwardDialNumberService.update(directInwardDialNumber, params)
        if(directInwardDialNumber.hasErrors()) {
            log.error "failed to update direct inward number with error [${directInwardDialNumber.errors}]"
            def model = routingModel()
            model.updatedDirectInwardDialNumber = directInwardDialNumber
            render(view: 'routing', model: model)
        } else {
            flash.successMessage = message(code: 'directInwardDialNumber.updated.message')
            redirect(action: 'routing')
        }
        
        return
    }
    
    def updateDirectMessageNumber = {
        log.debug "updateDirectMessageNumber with params [${params}]"
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
        log.debug "updateExternalRoute with params [${params}]"
        def route = NumberRoute.get(params.id)
        if(!route) {
            flash.errorMessage = message(code: 'numberRoute.notFound.message')
            redirect(action: 'routing')
            return
        }

        if(authenticatedUser.organization != route.organization) {
            log.error "Authorization denied for user [${params}]"
            redirect(controller: 'login', action: 'denied')
            return
        }
        
        route.properties['destination', 'label'] = params
        if(route.validate() && route.save()) {
            flash.successMessage = message(code: 'numberRoute.updated.message')
            redirect(action: 'routing')
        } else {
            log.error "Failed route update with errors [${route.errors}]"
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
        def globalRestrictions = GlobalOutdialRestriction.findAllByPatternLike('%', [sort: 'pattern', order: 'asc'])
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
            
            // Check to see if there are any actions configured with this skill id
            def menuCount = RouteToAnACDAction.countBySkill(skill)
            log.info "Found [${menuCount}] menus using skill [${skill.skillname}]"
            skill.menuCount = menuCount
        }

        return [
            skills: skillsCount
        ] 
    }
    
    private def routingModel() {
        def organization = authenticatedUser.organization
        def external = NumberRoute.findAllByOrganizationAndType(organization, NumberRoute.Type.EXTERNAL, [sort: 'pattern', order: 'asc'])
        def internal = NumberRoute.findAllByOrganizationAndType(organization, NumberRoute.Type.INTERNAL, [sort: 'pattern', order: 'asc'])
        def directInwardDialNumbers = DirectInwardDialNumber.withCriteria {
            owner {
                eq('organization', organization)
            }
            order('number', 'asc')
        }
        
        def externalDIDs = NumberRoute.findAllByOrganizationAndTypeAndDestination(organization, NumberRoute.Type.EXTERNAL, 'Direct Inward Dial', [sort: 'pattern', order: 'asc'])
        
        def filteredExternalDIDs = []
        externalDIDs.each{ did ->
            log.debug "Found external DID [${did.pattern}]"
            def didMatch = directInwardDialNumbers.find{ extDID ->
                if (extDID.number.toString() == did.pattern.toString()) {
                    log.debug "Matched configured DID [${extDID.number}] to external DID [${did.pattern}]"
                    return true
                } else {
                    //log.debug "No match for configured DID [${extDID.number}] to external DID [${did.pattern}]"
                    return false
                }
            }
            if (!didMatch) {
                log.debug "Adding direct inward dial number [${did.pattern}]"
                filteredExternalDIDs.add(did)
            }
        }
        
        def directMessageNumbers = DirectMessageNumber.withCriteria {
            owner {
                eq('organization', organization)
            }
            order('number', 'asc')
        }
        
        def externalDMs = NumberRoute.findAllByOrganizationAndTypeAndDestination(organization, NumberRoute.Type.EXTERNAL, 'Direct Message', [sort: 'pattern', order: 'asc'])
        
        def filteredExternalDMs = []
        externalDMs.each{ dm ->
            log.debug "Found external DM [${dm.pattern}]"
            def dmMatch = directMessageNumbers.find{ extDM ->
                if (extDM.number.toString() == dm.pattern.toString()) {
                    log.debug "Matched configured DM [${extDM.number}] to external DM [${dm.pattern}]"
                    return true
                } else {
                    log.debug "No match for configured DM [${extDM.number}] to external DM [${dm.pattern}]"
                    return false
                }
            }
            if (!dmMatch) {
                log.debug "Adding direct message number [${dm.pattern}]"
                filteredExternalDMs.add(dm)
            }
        }
        
        def destinations = applicationService.listApplications()
        
        return [
            destinations: destinations,
            external: external,
            externalDIDs: filteredExternalDIDs,
            externalDMs: filteredExternalDMs,
            internal: internal,
            directInwardDialNumbers: directInwardDialNumbers,
            directMessageNumbers: directMessageNumbers
        ]
    }
}
