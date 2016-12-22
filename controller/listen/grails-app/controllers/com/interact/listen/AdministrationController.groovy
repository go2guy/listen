package com.interact.listen

import com.interact.listen.exceptions.ListenExportException
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import com.interact.listen.android.GoogleAuthConfiguration
import com.interact.listen.conferencing.ConferencingConfiguration
import com.interact.listen.history.*
import com.interact.listen.license.ListenFeature
import com.interact.listen.acd.*
import com.interact.listen.attendant.action.RouteToAnACDAction
import com.interact.listen.pbx.Extension
import com.interact.listen.pbx.SipPhone
import com.interact.listen.pbx.NumberRoute
import com.interact.listen.voicemail.afterhours.AfterHoursConfiguration
import org.apache.commons.lang.ObjectUtils
import org.joda.time.DateTime
import org.joda.time.LocalDateTime
import org.joda.time.Period
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import com.interact.listen.exceptions.ListenExportException

import javax.validation.constraints.Null


@Secured(['ROLE_ORGANIZATION_ADMIN'])
class AdministrationController {
    def applicationService
    def callService
    def directInwardDialNumberService
    def directMessageNumberService
    def extensionService
    def historyService
    def licenseService
    def messageLightService
    def realizeAlertUpdateService
    def springSecurityService

    static allowedMethods = [
            index                       : 'GET',
            addDirectInwardDialNumber   : 'POST',
            addDirectMessageNumber      : 'POST',
            addException                : 'POST',
            addExtension                : 'POST',
            addInternalRoute            : 'POST',
            addRestriction              : 'POST',
            android                     : 'GET',
            calls                       : 'GET',
            callsData                   : 'GET',
            configuration               : 'GET',
            deleteDirectInwardDialNumber: 'POST',
            deleteDirectMessageNumber   : 'POST',
            deleteException             : 'POST',
            deleteExtension             : 'POST',
            deleteInternalRoute         : 'POST',
            deleteRestriction           : 'POST',
            outdialing                  : 'GET',
            phones                      : 'GET',
            toggleMsgLight              : 'GET',
            updateMsgLight              : 'POST',
            pollAvailableUsers          : 'POST',
            routing                     : 'GET',
            saveAndroid                 : 'POST',
            saveConfiguration           : 'POST',
            updateDirectMessageNumber   : 'POST',
            updateDirectInwardDialNumber: 'POST',
            updateException             : 'POST',
            updateExtension             : 'POST',
            updateExternalRoute         : 'POST',
            updateInternalRoute         : 'POST',
            updateRestriction           : 'POST',
            users                       : 'GET',
            showHistory                 : 'GET'
    ]

    def index = {
        redirect(action: 'routing')
    }

    def addDirectInwardDialNumber = {
        log.debug "addDirectInwardDialNumber with params [${params}]"

        log.debug "Add number patter [${params.number}] to params"

        def directInwardDialNumber = directInwardDialNumberService.create(params)
        if (directInwardDialNumber.hasErrors()) {
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

        log.debug "Add number patter [${params.number}] to params"

        def directMessageNumber = directMessageNumberService.create(params)

        if (directMessageNumber.hasErrors()) {
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

        if (exception.validate() && exception.save()) {
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
        route.organization = session.organization

        if (route.validate() && route.save()) {
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
        log.debug "addExtension with params [${params}]"
        def organization = session.organization

        def extInfo = extensionService.create(params, organization);

        if (extInfo.extension?.hasErrors() || extInfo?.sipPhone?.hasErrors()) {
            log.debug "addExtension failed to be added"
            log.debug("extInfo extension errors [${extInfo?.extension?.getErrors()}]")
            log.debug("extInfo sipPhone errors [${extInfo?.sipPhone?.getErrors()}]")

            if (extInfo?.sipPhone) {
                log.debug("Extension has sipPhone parameters [${extInfo?.sipPhone?.username}]")
            } else {
                log.debug("Extension does not have sipPhone parameters, use params [${params?.username}]")
                extInfo.sipPhone = new SipPhone(params)
//                extInfo.sipPhone.passwordConfirm = params?.passwordConfirm
            }
            model.newDirectInwardDialNumber = directInwardDialNumber
            render(view: 'routing', model: model)
        } else {
            flash.successMessage = message(code: 'directInwardDialNumber.created.message')
            redirect(action: 'routing')
        }
    }


    def showHistory = {
        log.debug "showHistory with params [${params}]"

        def callHistoryInstance = CallHistory.get(params.id)
        if (!callHistoryInstance){
            log.error("Can not find callHistory with id [${params.id}]")
            redirect("callHistory")
            return
        }
        log.debug("We've found callHistory with id [${callHistoryInstance.id}] and session id [${callHistoryInstance.sessionId}]")

        def acdCallHistoryInstance = AcdCallHistory.findAllBySessionId(callHistoryInstance.sessionId)
        if (acdCallHistoryInstance) {
            acdCallHistoryInstance.each { acdCallHistory ->
                log.debug("We've found acdHistory [${acdCallHistory.id}] for callHistory [${callHistoryInstance.id}]")
                log.debug("We've found acdHistory for skill [${acdCallHistory.skill}]")
            }
        } else {
            log.debug("We've not found acdCallHistory [${acdCallHistoryInstance.id}] for callHistory [${callHistoryInstance.id}]")
        }
        render(view: 'showHistory', model:[callHistoryInstance: callHistoryInstance, acdCallHistoryInstance: acdCallHistoryInstance])
    }

    def toggleMsgLight = {
        log.debug "toggleMsgLight with params [${params}]"
        render(view: 'toggleMsgLight', model: phonesModel())
    }

    def updateMsgLight = {
        log.debug "updateMsgLight with params [${params}]"

        def extension = Extension.get(params?.id)
        if (!extension) {
            log.error("updateMsgLight passed invalid extension [${params?.id}]")
            flash.errorMessage = message(code: 'extension.notFound.message')
            redirect(action: 'toggleMsgLight')
            return
        }

        if (params?.lightStatus == message(code: 'page.administration.phones.toggleMsgLight.button.on')) {
            log.debug("updateMsgLight to turn it on")
            messageLightService.toggle(extension?.sipPhone?.phoneUserId, extension?.sipPhone?.ip, true)
            flash.successMessage = message(code: 'page.administration.phones.toggleMsgLight.action.on', args: [extension.number])
        } else if (params?.lightStatus == message(code: 'page.administration.phones.toggleMsgLight.button.off')) {
            log.debug("updateMsgLight to turn it off")
            messageLightService.toggle(extension?.sipPhone?.phoneUserId, extension?.sipPhone?.ip, false)
            flash.successMessage = message(code: 'page.administration.phones.toggleMsgLight.action.off', args: [extension.number])
        } else {
            log.error("updateMsgLight invalid or missing parameter [lightStatus] [${params?.lightStatus}]")
            flash.errorMessage = message(code: 'page.administration.phones.toggleMsgLight.input.error')
        }

        render(view: 'toggleMsgLight', model: phonesModel())
    }

    def addRestriction = {
        def restriction = new OutdialRestriction()
        restriction.pattern = params.pattern
        restriction.organization = session.organization
        if (params.target) {
            def target = User.get(params.target)
            if (!target) {
                def model = outdialingModel()
                model.newRestriction = restriction
                flash.errorMessage = message(code: 'outdialRestriction.targetNotFound.message')
                render(view: 'outdialing', model: model)
                return
            }
            restriction.target = target
        }

        if (restriction.validate() && restriction.save()) {
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
                    ani      : call.ani,
                    dnis     : call.dnis,
                    started  : new Period(call.started, now).toStandardSeconds().seconds,
                    ended    : call.ended ? new Period(call.ended, now).toStandardSeconds().seconds : 0,
                    sessionId: call.sessionId
            ]
        }
        render data as JSON
    }

    def configuration = {
        def organization = session.organization

        def transcription = TranscriptionConfiguration.findByOrganization(organization)
        def afterHours = AfterHoursConfiguration.findByOrganization(organization)
        def conferencing = ConferencingConfiguration.findByOrganization(organization)
        if (!conferencing) {
            conferencing = new ConferencingConfiguration(organization: organization)
        }

        render(view: 'configuration', model: [transcription: transcription, afterHours: afterHours, conferencing: conferencing, organization: organization])
    }

    def deleteDirectInwardDialNumber = {
        def directInwardDialNumber = DirectInwardDialNumber.get(params.id)
        if (!directInwardDialNumber) {
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
        if (!directMessageNumber) {
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
        if (!exception) {
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
        if (!route) {
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
        if (!extension) {
            flash.errorMessage = message(code: 'extension.notFound.message')
            redirect(action: 'listPhones')
            return
        }

        extensionService.delete(extension)
        flash.successMessage = message(code: 'extension.deleted.message')
        redirect(action: 'listPhones')
    }

    def deleteRestriction = {
        def restriction = OutdialRestriction.get(params.id)
        if (!restriction) {
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

        def organization = session.organization
        def skill = new Skill()

        skill.skillname = params.skillname
        skill.organization = organization
        skill.description = params.description
        skill.onHoldMsg = ''
        skill.onHoldMsgExtended = ''
        skill.onHoldMusic = ''
        skill.connectMsg = ''

        if (skill.validate() && skill.save()) {
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

        def organization = session.organization
        def skill = Skill.get(params.id)
        if (!skill) {
            flash.errorMessage = message(code: 'skill.notFound.message')
            redirect(action: 'skills')
            return
        }

        def orgUsers = User.findAllByOrganizationAndEnabled(organization, true, [sort: 'realName', order: 'asc'])
        def skillUsers = UserSkill.findAllBySkill(skill)
        def testUsers = []
        // users not already serving as a voicemail user
        def freeUsers = []
        skillUsers.each { skilluser ->
            log.debug "User [${skilluser.user}][${skilluser.user.realName}] [${skilluser.user.id}] has skill [${skilluser.skill.skillname}]"
            testUsers << skilluser.user
            // if user is not already serving as a voicemail user
            if (skilluser.user.acdUserStatus.acdQueueStatus != AcdQueueStatus.VoicemailBox) {
                // they are free to be assigned as one
                log.debug "Adding userskill [${skilluser}] to freeUsers."
                freeUsers << skilluser
            }
        }

        def vmUser = AcdService.getVoicemailUserBySkillname(skill.skillname)
        // We still want to keep the currently assigned voicemail user on the list of assignable users
        freeUsers << UserSkill.findByUser(vmUser)

        if (vmUser) {
            log.debug "Skill [${skill.skillname}] is assigned to user [${vmUser.realName}] for voicemail"
        }

        render(view: 'editSkill', model: [skill: skill, orgUsers: orgUsers, skillUsers: skillUsers, freeUsers: freeUsers, vmUser: vmUser, testUsers: testUsers])
    }

    def updateSkill = {
        log.debug "Update skill with params [${params}]"

        def skill = Skill.get(params.id)
        if (!skill) {
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

        if (skill.validate() && skill.save()) {
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
            if (userIds.contains(skillUser.user.id.toString())) {
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
            if (!userskill.user) {
                flash.errorMessage = message(code: 'user.notFound.message')
                redirect(action: 'skills')
                return
            }

            log.debug "Working to add skill [${skill.skillname}] to user [${userskill.user.username}]"

            userskill.skill = skill

            if (userskill.validate() && userskill.save()) {
                historyService.addedUserSkill(userskill)
            } else {
                log.error "Failed to add skill [${skill.skillname}] to user [${userskill.user.username}]"
            }
        }

        // remove the previous voicemail user
        AcdService.deleteVoicemailBox(Skill.findBySkillname(skill))

        // If a voicemail user was selected make sure they are assigned the current skill
        if (params.vmUserId != "" && params.vmUserId != null) {
            def isAssociated = false
            def vmUser
            vmUser = User.findById(params.vmUserId.toInteger())
            UserSkill.findAllBySkill(skill).each() { userSkill ->
                if (userSkill?.user?.id == vmUser?.id) {
                    isAssociated = true
                }
            }

            // if the desired voicemail user is eligible to become a voicemail user, then make it happen
            if (isAssociated) {
                if (vmUser) {
                    log.debug "We have vmUserId of [${params.vmUserId}] for user [${vmUser.username}]"
                    AcdService.setVoicemailUserBySkillname(skill, vmUser)
                } else {
                    log.error "vmUserId [${params.vmUserId}] is invalid!"
                }
            } else {
                flash.errorMessage = message(code: 'skill.vmumissingskill.message')
                redirect(action: 'skills')
            }
        }

        flash.successMessage = message(code: 'skill.updated.message')

        def model = skillModel()
        render(view: 'skills', model: model)
    }

    private filterACDPrompts(String promptName) {
        if (promptName) {
            if (promptName == "-- No Prompt --")
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
        if (!skill) {
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

    def callHistory = {
        log.debug("callHistory with params [${params}]")
        def organization = session.organization
        if (!organization){
            log.error("Failed to evaluate organization from [${session.organization}]")
            redirect(action: 'callHistory')
            return
        }

        log.debug("callHistory for organization [${organization.id}]")
        def users = User.findAllByOrganization(organization)

        params.offset = params.offset ? params.int('offset') : 0
        params.max = Math.min(params.max ? params.int('max') : 25, 100)

        def startDate
        def endDate
        if (params.startDate && params.searchButton) {
            startDate = getStartDate(params)
        }

        if (params.endDate && params.searchButton) {
            endDate = getEndDate(params)
        }

        if (endDate && startDate && (endDate.isBefore(startDate) || endDate.isEqual(startDate))) {
            flash.errorMessage = message(code: "callHistory.endDate.before.label", default: "End Date is before Start Date")
            endDate = null
            params.remove("endDate")
        }

        def selectedUsers
        if (params.user && params.searchButton) {
            selectedUsers = User.findAllByIdInListAndOrganization([params.user].flatten().collect{ Long.valueOf(it)}, organization)
        }

        log.debug("startDate [${startDate}]")
        log.debug("endDate [${endDate}]")
        log.debug("selectedUsers [${selectedUsers}]")
        // call history
        def callHistory = CallHistory.createCriteria().list([offset: params.offset, max: params.max, sort: 'dateTime', order: 'desc']) {
            if (startDate && endDate && params.searchButton) {
                and {
                    ge('dateTime', startDate)
                    le('dateTime', endDate)
                }
            } else if (startDate && params.searchButton) {
                ge('dateTime', startDate)
            } else if (endDate && params.searchButton) {
                le('dateTime', endDate)
            }

            if (params.caller && params.searchButton) {
                ilike('ani', '%'+params.caller.replaceAll("\\D+", "")+'%')
            }

            if (params.callee && params.searchButton) {
                ilike('dnis', '%'+params.callee.replaceAll("\\D+", "")+'%')
            }

            if (selectedUsers && params.searchButton) {
                or {
                    'in'('toUser', selectedUsers)
                    'in'('fromUser', selectedUsers)
                }
            }

            if (params.callResult && params.searchButton) {
                ilike('result', '%'+params.callResult+'%')
            }

            eq('organization', organization)
        }

        callHistory.each { callHist ->
            def acdCallHist = AcdCallHistory.findBySessionId(callHist.sessionId)
            if (acdCallHist) {
                log.debug("Found acd call history records for session id [${callHist.sessionId}]")
                callHist.acdCall = true
            } else {
                log.debug("Did not find any acd call history records for session id [${callHist.sessionId}][${callHist?.acdCall}]")
                callHist.acdCall = false
            }
        }

        callHistory.each { callHist ->
            log.debug("Found [${callHist.id}] call history id [${callHist?.acdCall}]")
        }
        log.debug("Now lets get call count")
        def callHistoryCount = CallHistory.createCriteria().get {
            projections {
                count('id')
            }

            if (startDate && endDate && params.searchButton) {
                and {
                    ge('dateTime', startDate)
                    le('dateTime', endDate)
                }
            } else if (startDate && params.searchButton) {
                ge('dateTime', startDate)
            } else if (endDate && params.searchButton) {
                le('dateTime', endDate)
            }

            if (params.caller && params.searchButton) {
                ilike('ani', '%'+params.caller.replaceAll("\\D+", "")+'%')
            }

            if (params.callee && params.searchButton) {
                ilike('dnis', '%'+params.callee.replaceAll("\\D+", "")+'%')
            }

            if (selectedUsers && params.searchButton) {
                or {
                    'in'('toUser', selectedUsers)
                    'in'('fromUser', selectedUsers)
                }
            }

            if (params.callResult && params.searchButton) {
                ilike('result', '%'+params.callResult+'%')
            }

            eq('organization', organization)
        }

        log.debug("Found [${callHistoryCount}] CDR records")
        render(view: 'callHistory', model: [callHistoryList: callHistory, callHistoryTotal: callHistoryCount, users: users, selectedUsers: selectedUsers])
    }

    def callHistory_orig = {
        def organization = session.organization
        def users = User.findAllByOrganization(organization)

        params.offset = params.offset ? params.int('offset') : 0
        params.max = Math.min(params.max ? params.int('max') : 25, 100)

        def startDate
        def endDate
        if (params.startDate && params.searchButton) {
            startDate = getStartDate(params)
        }

        if (params.endDate && params.searchButton) {
            endDate = getEndDate(params)
        }

        if (endDate && startDate && (endDate.isBefore(startDate) || endDate.isEqual(startDate))) {
            flash.errorMessage = message(code: "callHistory.endDate.before.label", default: "End Date is before Start Date")
            endDate = null
            params.remove("endDate")
        }

        def selectedUsers
        if (params.user && params.searchButton) {
            selectedUsers = User.findAllByIdInListAndOrganization([params.user].flatten().collect{ Long.valueOf(it)}, organization)
        }

        // call history
        def callHistory = CallHistory.createCriteria().list([offset: params.offset, max: params.max, sort: 'dateTime', order: 'desc']) {
            if (startDate && endDate && params.searchButton) {
                and {
                    ge('dateTime', startDate)
                    le('dateTime', endDate)
                }
            } else if (startDate && params.searchButton) {
                ge('dateTime', startDate)
            } else if (endDate && params.searchButton) {
                le('dateTime', endDate)
            }

            if (params.caller && params.searchButton) {
                ilike('ani', '%'+params.caller.replaceAll("\\D+", "")+'%')
            }

            if (params.callee && params.searchButton) {
                ilike('dnis', '%'+params.callee.replaceAll("\\D+", "")+'%')
            }

            if (selectedUsers && params.searchButton) {
                or {
                    'in'('toUser', selectedUsers)
                    'in'('fromUser', selectedUsers)
                }
            }

            if (params.callResult && params.searchButton) {
                ilike('result', '%'+params.callResult+'%')
            }

            eq('organization', organization)
        }
        def callHistoryCount = CallHistory.createCriteria().get {
            projections {
                count('id')
            }

            if (startDate && endDate && params.searchButton) {
                and {
                    ge('dateTime', startDate)
                    le('dateTime', endDate)
                }
            } else if (startDate && params.searchButton) {
                ge('dateTime', startDate)
            } else if (endDate && params.searchButton) {
                le('dateTime', endDate)
            }

            if (params.caller && params.searchButton) {
                ilike('ani', '%'+params.caller.replaceAll("\\D+", "")+'%')
            }

            if (params.callee && params.searchButton) {
                ilike('dnis', '%'+params.callee.replaceAll("\\D+", "")+'%')
            }

            if (selectedUsers && params.searchButton) {
                or {
                    'in'('toUser', selectedUsers)
                    'in'('fromUser', selectedUsers)
                }
            }

            if (params.callResult && params.searchButton) {
                ilike('result', '%'+params.callResult+'%')
            }

            eq('organization', organization)
        }

        render(view: 'callHistory', model: [callHistoryList: callHistory, callHistoryTotal: callHistoryCount, users: users, selectedUsers: selectedUsers])
    }

    def exportCallHistoryToCSV = {
        log.debug("exportCallHistoryToCSV called with params [${params}]")
        def organization = session.organization
        params.organization = organization

        try {
            callService.exportCallHistoryToCSV(organization, params)

        }
        catch(ListenExportException lae)
        {
            log.error("Listen Export Exception: " + lae.getMessage());
            flash.errorMessage = message(code: 'callHistory.exportCSV.fileCreateFail')
            redirect(action: "callHistory", params: params)
            return
        }
        catch(Exception e)
        {
            log.error("Exception exporting cvs: " + e.getMessage(), e);
            flash.errorMessage = message(code: 'callHistory.exportCSV.fileCreateFail')
            redirect(action: "callHistory", params: params)
            return
        }

        log.debug("completion of exportCallHistoryToCSV")
        return
    }

    def actionHistory = {
        def organization = session.organization
        def users = User.findAllByOrganization(organization)

        params.offset = params.offset ? params.int('offset') : 0
        params.max = Math.min(params.max ? params.int('max') : 25, 100)

        def startDate
        def endDate
        if (params.startDate && params.searchButton) {
            startDate = getStartDate(params)
        }

        if (params.endDate && params.searchButton) {
            endDate = getEndDate(params)
        }

        if (endDate && startDate && (endDate.isBefore(startDate) || endDate.isEqual(startDate))) {
            flash.errorMessage = message(code: "actionHistory.endDate.before.label", default: "End Date is before Start Date")
            endDate = null
            params.remove("endDate")
        }


        def selectedUsers
        if (params.user && params.searchButton) {
            selectedUsers = User.findAllByIdInListAndOrganization([params.user].flatten().collect{ Long.valueOf(it)}, organization)
        }

        // action history
        def actionHistoryList = ActionHistory.createCriteria().list([offset: params.offset, max: params.max, sort: 'dateCreated', order: 'desc']) {
            if (startDate && endDate && params.searchButton) {
                log.debug("startDate is ${startDate} and endDate is ${endDate}")
                and {
                    ge('dateCreated', startDate)
                    le('dateCreated', endDate)
                }
            } else if (startDate && params.searchButton) {
                log.debug("startDate is ${startDate}")
                ge('dateCreated', startDate)
            } else if (endDate && params.searchButton) {
                log.debug("endDate is ${endDate}")
                le('dateCreated', endDate)
            }

            if (selectedUsers && params.searchButton) {
                or {
                    'in'('byUser', selectedUsers)
                    'in'('onUser', selectedUsers)
                }
            }

            eq('organization', organization)
        }
        def actionHistoryTotal = ActionHistory.createCriteria().get {
            projections {
                count('id')
            }

            if (startDate && endDate && params.searchButton) {
                and {
                    ge('dateCreated', startDate)
                    le('dateCreated', endDate)
                }
            } else if (startDate && params.searchButton) {
                ge('dateCreated', startDate)
            } else if (endDate && params.searchButton) {
                le('dateCreated', endDate)
            }

            if (selectedUsers && params.searchButton) {
                or {
                    'in'('byUser', selectedUsers)
                    'in'('onUser', selectedUsers)
                }
            }

            eq('organization', organization)
        }

        render(view: 'actionHistory', model: [actionHistoryList: actionHistoryList, actionHistoryTotal: actionHistoryTotal, users: users, selectedUsers: selectedUsers])
    }

    def exportActionHistoryToCSV = {
        def organization = session.organization
        def users = User.findAllByOrganization(organization)

        params.offset = params.offset ? params.int('offset') : 0
        params.max = Math.min(params.max ? params.int('max') : 25, 100)

        def startDate
        def endDate
        if (params.startDate) {
            startDate = getStartDate(params)
        }

        if (params.endDate) {
            endDate = getEndDate(params)
        }

        def selectedUsers
        if (params.user) {
            selectedUsers = User.findAllByIdInListAndOrganization([params.user].flatten().collect{ Long.valueOf(it)}, organization)
        }

        File tmpFile
        try {
            log.debug("Creating temp file to extract for action history [${params}]")
            tmpFile = File.createTempFile("./listen-actionhistory-${new LocalDateTime().toString('yyyyMMddHHmmss')}", ".csv")
            tmpFile.deleteOnExit()
            log.debug("tmpFile [${tmpFile.getName()}] created.")
        } catch (IOException e) {
            log.error("Failed to create temp file for export: ${e}")
            flash.errorMessage = message(code: 'actionHistory.exportCSV.fileCreateFail')
            redirect(action: "actionHistory", params: params)
            return
        }

        log.debug("Pulling records")

        def actionHistory = ActionHistory.createCriteria().list([sort: 'dateCreated', order: 'desc']) {
            if (startDate && endDate) {
                and {
                    ge('dateCreated', startDate)
                    le('dateCreated', endDate)
                }
            } else if (startDate) {
                ge('dateCreated', startDate)
            } else if (endDate) {
                le('dateCreated', endDate)
            }

            if (selectedUsers) {
                or {
                    'in'('byUser', selectedUsers)
                    'in'('onUser', selectedUsers)
                }
            }

            eq('organization', organization)
        }

        // Build the data now
        try {
            // Build header
            tmpFile << "date,user,description,channel\n"

            actionHistory.each {
                tmpFile << "${it.dateCreated?.toString("yyyy-MM-dd HH:mm:ss")},"
                tmpFile << "${it.byUser?.realName}"
                if (it.byUser && it.onUser && it.byUser != it.onUser) {
                    tmpFile << " > ${it.onUser.realName}"
                }
                tmpFile << ","
                tmpFile << "${it.description},"
                tmpFile << "${it.channel}\n"
            }
        } catch (Exception e) {
            log.error("Exception building csv file: ${e}")
	        flash.errorMessage = message(code: 'actionHistory.exportCSV.fileCreateFail')
	        redirect(action: "actionHistory", params: params)
	        return
        }

        def filename = "listen-actionhistory-${new LocalDateTime().toString('yyyyMMddHHmmss')}.csv"
        response.contentType = "text/csv"
        response.setHeader("Content-disposition", "attachment;filename=${filename}")
        response.setHeader("Content-length", "${tmpFile.length()}")

        OutputStream outputStream = new BufferedOutputStream(response.outputStream)
        InputStream inputStream = tmpFile.newInputStream()

        byte[] bytes = new byte[4096]
        int bytesRead;

        while ((bytesRead = inputStream.read(bytes)) != -1) {
            outputStream.write(bytes, 0, bytesRead)
        }

        inputStream.close()
        outputStream.flush()
        outputStream.close()
        response.flushBuffer()

        if (tmpFile.delete() == false) {
            log.error("Failed to delete temp file [${tmpFile.getName()}]")
            return
        }

        log.debug("temp file deleted")
        return
    }

    def outdialing = {
        render(view: 'outdialing', model: outdialingModel())
    }

    def listPhones = {
        render(view: 'listPhones', model: phonesModel())
    }

    def phones = {
        render(view: 'listPhones', model: phonesModel())
    }

    // ajax
    def pollAvailableUsers = {
        log.debug "pollAvailableUsers: selected [${params.selected}]"

        // users not already serving as a voicemail user
        def freeUsers = []
        def selectedUser
        def currentVoicemailUser = AcdService.getVoicemailUserBySkillname(params.skill)
        log.debug "current voicemail user [${currentVoicemailUser?.realName}]"

        params.selected.split(",").each() { username ->
            selectedUser = User.findByRealName(username)
            log.debug "Checking whether user [${selectedUser?.realName}] is free"
            if (selectedUser?.acdUserStatus?.acdQueueStatus != AcdQueueStatus?.VoicemailBox ||
                    selectedUser?.id == currentVoicemailUser?.id) {
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

        if (googleAuthConfiguration.validate() && googleAuthConfiguration.save()) {
            if (oldIsEnabled != googleAuthConfiguration.isEnabled) {
                if (googleAuthConfiguration.isEnabled) {
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
        def organization = session.organization

        def transcription = TranscriptionConfiguration.findByOrganization(organization)
        if (!transcription) {
            transcription = new TranscriptionConfiguration(organization: organization)
        }

        def oldTranscriptionIsEnabled = transcription.isEnabled
        def oldTranscriptionPhoneNumber = transcription.phoneNumber
        if (licenseService.canAccess(ListenFeature.VOICEMAIL) && licenseService.canAccess(ListenFeature.TRANSCRIPTION)) {
            bindData(transcription, params['transcription'], [include: ['isEnabled']])
            bindData(transcription, params['transcription'], [include: ['phoneNumber']])
        }

        def afterHours = AfterHoursConfiguration.findByOrganization(organization)
        if (!afterHours) {
            afterHours = new AfterHoursConfiguration(organization: organization)
        }

        def originalMobilePhone = afterHours.mobilePhone
        def originalAlternateNumber = afterHours.alternateNumber
        def originalRealizeUrl = afterHours.realizeUrl
        def originalRealizeAlertName = afterHours.realizeAlertName

        if (licenseService.canAccess(ListenFeature.AFTERHOURS)) {
            log.debug "After hours is licensed, saving configuration"

            if (params['afterHours.mobilePhone.id'] == '') {
                afterHours.mobilePhone = null
            } else {
                bindData(afterHours, params['afterHours'], [include: ['mobilePhone']])
            }
            bindData(afterHours, params['afterHours'], [include: ['realizeAlertName']])
            bindData(afterHours, params['afterHours'], [include: ['realizeUrl']])
            if (params['afterHours'].alternateNumber?.trim() == '') {
                afterHours.alternateNumber = ''
            } else {
                afterHours.alternateNumber = params['afterHours'].alternateNumber + '@' + params['afterHours'].provider
            }
        }

        def conferencing = ConferencingConfiguration.findByOrganization(organization)
        if (!conferencing) {
            conferencing = new ConferencingConfiguration(organization: organization)
        }

        def originalPinLength = conferencing.pinLength
        if (licenseService.canAccess(ListenFeature.CONFERENCING)) {
            log.debug "Conferencing is licensed, saving configuration"
            if (params['conferencing'].pinLength == '' || !params['conferencing'].pinLength.matches('^[0-9]*$')) {
                flash.errorMessage = message(code: 'conferencingConfiguration.pattern.matches.invalid')
                render(view: 'configuration', model: [transcription: transcription, afterHours: afterHours, conferencing: conferencing, organization: organization])
            }
            bindData(conferencing, params['conferencing'], [include: ['pinLength']])
        }

        def originalExtLength = organization.extLength
        if (licenseService.canAccess(ListenFeature.IPPBX)) {
            log.debug "IPPBX is licensed, saving configuration [${params['organization'].extLength}]"
            if (params['organization'].extLength == '' || !params['organization'].extLength.matches('^[0-9]*$')) {
                flash.errorMessage = message(code: 'organizationConfiguration.extLength.pattern.matches.invalid')
                render(view: 'configuration', model: [transcription: transcription, afterHours: afterHours, conferencing: conferencing, organization: organization])
            }
            bindData(organization, params['organization'], [include: ['extLength']])
        }

        // TODO use a transaction
        if (licenseService.canAccess(ListenFeature.AFTERHOURS) && afterHours.validate() && afterHours.save()) {
            log.debug "Saving after hours configuration successful"
            if (originalMobilePhone != afterHours.mobilePhone) {
                historyService.changedAfterHoursMobileNumber(afterHours, originalMobilePhone)
            }

            if (originalAlternateNumber != afterHours.alternateNumber) {
                realizeAlertUpdateService.sendUpdate(afterHours, originalAlternateNumber)
                historyService.changedAfterHoursAlternateNumber(afterHours, originalAlternateNumber)
            }

            if (originalRealizeUrl != afterHours.realizeUrl || originalRealizeAlertName != afterHours.realizeAlertName) {
                historyService.changedRealizeConfiguration(afterHours)
            }
        } else if (licenseService.canAccess(ListenFeature.AFTERHOURS)) {
            log.debug "Didn't save the after hours configuration"
            render(view: 'configuration', model: [transcription: transcription, afterHours: afterHours, conferencing: conferencing, organization: organization])
        }

        if (transcription.validate() && transcription.save() && conferencing.validate() && conferencing.save() && organization.validate() && organization.save()) {
            log.debug "Saving transcription, conferencing, and organization configuration successful"
            if (licenseService.canAccess(ListenFeature.VOICEMAIL) && licenseService.canAccess(ListenFeature.TRANSCRIPTION)) {
                boolean wasJustEnabled = false
                if (oldTranscriptionIsEnabled != transcription.isEnabled) {
                    if (transcription.isEnabled) {
                        wasJustEnabled = true
                        historyService.enabledTranscription(transcription)
                    } else {
                        historyService.disabledTranscription()
                    }
                }

                if (!wasJustEnabled && oldTranscriptionPhoneNumber != transcription.phoneNumber) {
                    historyService.enabledTranscription(transcription)
                }
            }

            if (licenseService.canAccess(ListenFeature.CONFERENCING)) {
                if (originalPinLength != conferencing.pinLength) {
                    historyService.changedNewConferencePinLength(conferencing, originalPinLength)
                }
            }

            if (licenseService.canAccess(ListenFeature.IPPBX)) {
                if (originalExtLength != organization.extLength) {
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
        if (!directInwardDialNumber) {
            log.error "Couldn't find direct inward number [${params}]"
            flash.errorMessage = message(code: 'directInwardDialNumber.notFound.message')
            redirect(action: 'routing')
            return
        }

        log.debug "Found did [${directInwardDialNumber.number}]"

        directInwardDialNumber = directInwardDialNumberService.update(directInwardDialNumber, params)
        if (directInwardDialNumber.hasErrors()) {
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
        if (!directMessageNumber) {
            flash.errorMessage = message(code: 'directMessageNumber.notFound.message')
            redirect(action: 'routing')
            return
        }

        directMessageNumber = directMessageNumberService.update(directMessageNumber, params)
        if (directMessageNumber.hasErrors()) {
            def model = routingModel()
            model.updatedDirectMessageNumber = directMessageNumber
            render(view: 'routing', model: model)
        } else {
            flash.successMessage = message(code: 'directMessageNumber.updated.message')
            redirect(action: 'routing')
        }
    }

    def editExtension = {
        log.debug "Edit extension with params [${params}]"

        def extension = Extension.get(params?.id)
        if (!extension) {
            flash.errorMessage = message(code: 'extension.notFound.message')
            redirect(action: 'listPhones')
            return
        }

        log.debug("We want to edit extension number [${extension?.number}] id [${extension?.id}]")

        def sipPhone = extension?.sipPhone
        if (sipPhone) {
            log.debug("Extension number [${extension.number}] has username [${sipPhone?.username}]")
            sipPhone.passwordConfirm = sipPhone?.password
            // because we want the passwordConfirm on the edit screen to be available
        } else {
            log.debug("Extension number [${extension.number}] doesn't have sipPhone entry")
        }

	    def templates = ProvisionerTemplate.getAll()
	    def userFields = []
	    def fields = []

	    if (sipPhone.provisionerTemplate) {
		    fields = ProvisionerTemplateField.findAllByProvisionerTemplate(sipPhone.provisionerTemplate)
		    userFields = ProvisionerTemplateFieldValue.findAllByProvisionerTemplateFieldInListAndSipPhone(fields, sipPhone)
	    }

        render(view: 'editPhone', model: [extension: extension, sipPhone: sipPhone, templates: templates, fields: fields, userFields: userFields])
    }

    def updateException = {
        def exception = OutdialRestrictionException.get(params.id)
        if (!exception) {
            flash.errorMessage = message(code: 'outdialRestrictionException.notFound.message')
            redirect(action: 'outdialing')
            return
        }

        def oldTarget = exception.target
        def oldRestriction = exception.restriction

        exception.properties['target', 'restriction'] = params
        if (exception.validate() && exception.save()) {
            if (oldTarget != exception.target || oldRestriction != exception.restriction) {
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
        if (!route) {
            flash.errorMessage = message(code: 'numberRoute.notFound.message')
            redirect(action: 'routing')
            return
        }

        if (session.organization != route.organization) {
            log.error "Authorization denied for user [${params}]"
            redirect(controller: 'login', action: 'denied')
            return
        }

        route.properties['destination', 'label'] = params
        if (route.validate() && route.save()) {
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
        if (!route) {
            flash.errorMessage = message(code: 'numberRoute.notFound.message')
            redirect(action: 'routing')
            return
        }

        if (session.organization != route.organization) {
            redirect(controller: 'login', action: 'denied')
            return
        }

        def oldDestination = route.destination
        def oldPattern = route.pattern

        route.properties['destination', 'label', 'pattern'] = params
        if (route.validate() && route.save()) {
            if (oldDestination != route.destination || oldPattern != route.pattern) {
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
        log.debug("updateExtension with params [${params}]")
        def extension = Extension.get(params.id)
        if (!extension) {
            log.error("updateExtension with params failed [${params}]")
            flash.errorMessage = message(code: 'extension.notFound.message')
            redirect(action: 'listPhones')
            return
        }

        def organization = session.organization
        def extInfo = extensionService.update(params, extension, organization)
        if (extInfo.extension?.hasErrors() || extInfo?.sipPhone?.hasErrors()) {
            log.debug "addExtension failed to be added"
            log.debug("extInfo extension errors [${extInfo?.extension?.getErrors()}]")
            log.debug("extInfo sipPhone errors [${extInfo?.sipPhone?.getErrors()}]")
            def sipPhone = extInfo?.sipPhone
            log.debug("extInfo extension ownerid [${extension.owner}]")
            render(view: 'editPhone', model: [extension: extension, sipPhone: sipPhone])
            //render(view: 'editPhone', model: [extension: extInfo?.extension, sipPhone: extInfo?.sipPhone ])
        } else {
            log.debug("updateExtension was successful")
            flash.successMessage = message(code: 'extension.updated.message')
            redirect(action: 'listPhones')
        }

        return
    }

    def updateRestriction = {
        def restriction = OutdialRestriction.get(params.id)
        if (!restriction) {
            flash.errorMessage = message(code: 'outdialRestriction.notFound.message')
            redirect(action: 'restrictions')
            return
        }

        if (session.organization != restriction.organization) {
            redirect(controller: 'login', action: 'denied')
            return
        }

        def originalPattern = restriction.pattern
        def originalTarget = restriction.target

        restriction.pattern = params.pattern
        restriction.target = null
        if (params.target) {
            def target = User.get(params.target)
            if (!target) {
                def model = outdialingModel()
                model.newRestriction = restriction
                flash.errorMessage = message(code: 'outdialRestriction.targetNotFound.message')
                render(view: 'outdialing', model: model)
                return
            }
            restriction.target = target
        }

        if (restriction.validate() && restriction.save()) {
            if (originalPattern != restriction.pattern || originalTarget != restriction.target) {
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

	def provisionerTemplates = {
		def templates = ProvisionerTemplate.getAll()
		render(view: 'templates', model: [templates: templates])
	}

	def addTemplate = {
		log.debug("Adding template with params ${params}")

		def template = new ProvisionerTemplate(params)
		template.template = "";

		if (!template.validate() || !template.save()) {
			log.error("Error saving template: ${template.errors}")
			flash.errorMessage = "Could not save template."
		}

		redirect(action: "provisionerTemplates")
	}

	def editTemplate = {
		log.debug("Attempting to load template ${params.id}")

		def template = ProvisionerTemplate.get(params.id)
		def fields = template.provisionerTemplateFields.sort { it.name.toLowerCase() }

		render(view: 'editTemplate', model: [template: template, fields: fields])
	}

	def deleteTemplate = {
		log.debug("Deleting Template ${params.id}")

		def template = ProvisionerTemplate.get(params.id)

		if (template) {
			template.delete()
			flash.successMessage = "Template deleted"
		} else {
			flash.errorMessage = "Could not find template to delete"
		}

		redirect(action: "provisionerTemplates")
	}

	def provisionerTemplateService

	def updateTemplate = {
		log.debug("Attempting to update template ${params.id}")
		log.debug("updateTemplate params are: ${params}")

		def template = ProvisionerTemplate.get(params.id)
		def fields = ProvisionerTemplateField.findAllByProvisionerTemplate(template)
		log.debug("Template is ${template}")

		template.name = params.name
		template.template = params.template

		log.debug("template name is now: ${template.name}")
		log.debug("template template is now: ${template.template}")
		if (!template.validate() || !template.save(failOnError: true)) {
			log.error("Error saving template: ${template.errors}")
		}

		params.fields.each { k, v ->
			if (v instanceof org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap) {
				if (v.id) {
					def vid = v.id.toInteger()
					def deleted = v.deleted.toBoolean()
					def field = fields.find {
						it.id == vid
					}

					if (field && deleted) {
						try {
							provisionerTemplateService.removeField(field)
						} catch (Exception e) {
							log.error("Error removing: ${e}")
							log.error(template.errors)
						}
					}

					if (field && !deleted && !(field.name == v.name && field.defaultValue == v.defaultValue)) {
						try {
							provisionerTemplateService.updateField(field, v.name.toString(), v.defaultValue.toString())
						} catch (Exception e) {
							log.error("Error Updating ${field}: [${e}]")
							log.error(field.errors)
						}
					}
				}

				if (!v.id && !v.deleted.toBoolean()) {
					provisionerTemplateService.addField(template, v.name.toString(), v.defaultValue.toString())
				}
			}
		}

		flash.successMessage = message(code: "page.administration.templates.updated", default: "Template Updated");

		redirect(controller: "administration", action: "editTemplate", id: params.id)
		return
	}

    private def phonesModel() {
        def organization = session.organization
        params.max = Math.min(params.max ? params.int('max') : 100, 100)
        params.sort = params.sort ?: 'number'
        params.order = params.order ?: 'asc'
        def extensionList = Extension.createCriteria().list(params) {
            owner {
                eq('organization', organization)
            }
        }

        if (log.isDebugEnabled()) {
            extensionList.each { ext ->
                ext.sipPhone.each { sipP ->
                    log.debug("Extension [${ext.number}] IP [${sipP?.ip}] Date Registered [${sipP?.dateRegistered}]")
                }
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
                extensionList : extensionList,
                extensionTotal: extensionTotal
        ]
    }

    private def outdialingModel() {
        def organization = session.organization
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
                globalRestrictions  : globalRestrictions,
                restrictions        : restrictions,
                everyoneRestrictions: everyoneRestrictions,
                exceptions          : exceptions
        ]
    }

    private def skillModel() {
        def organization = session.organization
        log.debug "Checking skills for organization [${organization.name}]"
        def skills = Skill.findAllByOrganization(organization, [sort: 'skillname', order: 'asc'])
        log.debug "Organization [${organization.id}] has [${skills.size()}] skills configured"
        def skillsCount = []
        skills.each { skill ->
            def userskills = UserSkill.findAllBySkill(skill)
            def userCount = 0
            userskills.each { userskill ->
                if (userskill.user.enabled == true) {
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
        def organization = session.organization
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
        externalDIDs.each { did ->
            log.debug "Found external DID [${did.pattern}]"
            def didMatch = directInwardDialNumbers.find { extDID ->
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
        externalDMs.each { dm ->
            log.debug "Found external DM [${dm.pattern}]"
            def dmMatch = directMessageNumbers.find { extDM ->
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
                destinations           : destinations,
                external               : external,
                externalDIDs           : filteredExternalDIDs,
                externalDMs            : filteredExternalDMs,
                internal               : internal,
                directInwardDialNumbers: directInwardDialNumbers,
                directMessageNumbers   : directMessageNumbers
        ]
    }

    private DateTime getStartDate(def params) {
        def startDate = null

        try {
            DateTimeFormatter format = DateTimeFormat.forPattern("MM/dd/yyyy")
            startDate = format.parseDateTime(params.startDate)
            startDate = startDate.withTime(0, 0, 0, 0)
        } catch (Exception e) {
            log.error("Unable to parse start date: ${params.startDate}: ${e}")
        }

        return startDate
    }

    private DateTime getEndDate(def params) {
        def endDate = null

        try {
            DateTimeFormatter format = DateTimeFormat.forPattern("MM/dd/yyyy")
            endDate = format.parseDateTime(params.endDate)
            endDate = endDate.withTime(0, 0, 0, 0).plusDays(1)
        } catch (Exception e) {
            log.error("Unable to parse end date: ${params.startDate}: ${e}")
        }

        return endDate
    }
}
