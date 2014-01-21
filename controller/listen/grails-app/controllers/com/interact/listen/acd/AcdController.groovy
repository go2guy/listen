package com.interact.listen.acd

import com.interact.listen.User
import com.interact.listen.acd.AcdCall
import com.interact.listen.acd.AcdUserStatus
import com.interact.listen.acd.Skill
import com.interact.listen.User
import com.interact.listen.PhoneNumber
import com.interact.listen.history.*
import grails.converters.JSON
import org.joda.time.DateTime
import com.interact.listen.util.FileTypeDetector
import grails.plugin.springsecurity.annotation.Secured

@Secured(['ROLE_ACD_USER'])
class AcdController
{
    static allowedMethods = [
            index: 'GET',
            status: 'GET',
            toggleStatus: 'POST',
            updateNumber: 'POST',
            callQueue: 'GET',
            callHistory: 'GET',
            pollQueue: 'GET'
    ]

    def promptFileService
    def historyService
    def spotCommunicationService
    def acdService

    // TODO fix hard-coded path
    static final File storageLocation = new File('/interact/listen/artifacts/acd')

    def index = {
        redirect(action: 'status')
    }

    def callQueue = {
        params.sort = params.sort ?: 'enqueueTime'
        params.order = params.order ?: 'asc'
        params.max = params.max ?: 10
        params.offset = params.offset ?: 0

        def calls = AcdCall.createCriteria().list() {
            order(params.sort, params.order)
            maxResults(params.max.toInteger())
            firstResult(params.offset.toInteger())
        }

        def callTotal = AcdCall.findAll().size()

        def model = [
                calls: calls,
                callTotal: callTotal,
                sort: params.sort,
                order: params.order,
                max: params.max,
                offset: params.offset
        ]


        render(view: 'callQueue', model: model)
    }

    def callHistory = {
        params.sort = params.sort ?: 'enqueueTime'
        params.order = params.order ?: 'asc'
        params.max = params.max ?: 10
        params.offset = params.offset ?: 0

        def calls = AcdCallHistory.createCriteria().list() {
            order(params.sort, params.order)
            maxResults(params.max.toInteger())
            firstResult(params.offset.toInteger())
        }

        def callTotal = AcdCallHistory.findAll().size();

        def json = [:]
        def callJson = []
        for (AcdCallHistory call : calls)
        {
            def c = [:]
            c.ani = call.ani
            c.skill = call.skill.description
            c.callStatus = call.callStatus.viewable()
            c.callStart = call.callStart
            c.callEnd = call.callEnd
            c.enqueueTime = call.enqueueTime
            c.dequeueTime = call.dequeueTime
            c.user = ""
            if (call.user != null)
            {
                c.user = call.user.realName
            }
            callJson.add(c)
        }

        json.calls = callJson
        json.callTotal = callTotal
        json.sort = params.sort
        json.order = params.order
        json.max = params.max
        json.offset = params.offset

        render(view: 'callHistory', model: json)
    }

    def pollQueue = {
        params.sort = params.sort ?: 'enqueueTime'
        params.order = params.order ?: 'asc'
        params.max = params.max ?: 10
        params.offset = params.offset ?: 0

        def json = [:]

        List<AcdCall> calls = AcdCall.createCriteria().list {
            order(params.sort, params.order)
            maxResults(params.max.toInteger())
            firstResult(params.offset.toInteger())
        }

        def callJson = []
        for (AcdCall call : calls)
        {
            def c = [:]
            c.id = call.id
            c.ani = call.ani
            c.onHold = call.onHold
            c.sessionId = call.sessionId
            c.skill = call.skill.description
            c.callStatus = call.callStatus.viewable()
            c.enqueueTime = call.enqueueTime
            c.lastModified = call.lastModified
            c.user = ""
            if (call.user != null)
            {
                c.user = call.user.realName
            }
            callJson.add(c)
        }

        json.calls = callJson

        render(contentType: 'application/json') {
            json
        }
    }

    def pollHistory = {
        def user = authenticatedUser

        params.sort = params.sort ?: 'enqueueTime'
        params.order = params.order ?: 'asc'
        params.max = params.max ?: 10
        params.offset = params.offset ?: 0

        def json = [:]

        List<AcdCallHistory> calls = AcdCallHistory.createCriteria().list {
            order(params.sort, params.order)
            maxResults(params.max.toInteger())
            firstResult(params.offset.toInteger())
        }

        def callJson = []
        for (AcdCallHistory call : calls)
        {
            def c = [:]
            c.id = call.id
            c.ani = call.ani
            c.user = ""
            if (call.user != null)
            {
                c.user = call.user.realName
            }
            c.skill = call.skill.description
            c.start = call.callStart
            c.end = call.callEnd
            c.callStatus = call.callStatus.viewable()
            c.enqueueTime = call.enqueueTime
            c.dequeueTime = call.dequeueTime
            callJson.add(c)
        }

        json.calls = callJson

        if (log.isDebugEnabled())
        {
            log.debug "Rendering call queue as json [${json.toString()}]"
        }

        render(contentType: 'application/json') {
            json
        }
    }

    def status =
        {
            if (!authenticatedUser)
            {
                //Redirect to login
                redirect(controller: 'login', action: 'auth');
            }

            def user = authenticatedUser

            if (params.paginateOrigin == 'queue')
            {
                params.queueSort = params.sort
                params.queueOrder = params.order
                params.queueMax = params.max
                params.queueOffset = params.offset
            }
            else
            { // params.paginateOrigin == 'history'
                params.historySort = params.sort
                params.historyOrder = params.order
                params.historyMax = params.max
                params.historyOffset = params.offset
            }

            // Get Agent Status Details
            def acdUserStatus = AcdUserStatus.findByOwner(user)
            if (!acdUserStatus)
            {
                log.debug "user does not currently have an acd user status [${acdUserStatus}]"
                acdUserStatus = new AcdUserStatus()
                acdUserStatus.owner = user
                acdUserStatus.acdQueueStatus = AcdQueueStatus.Unavailable
                /* Create user acd status entry, as they should have had one already */
                if (acdUserStatus.validate() && acdUserStatus.save(failOnError: true, flush: true))
                {
                    log.error "Created acd user status for this user [${acdUserStatus.owner}]"
                }
                else
                {
                    log.error "Could not create Acd Status Entry for new user."
                }
            }

            def status = acdUserStatus?.acdQueueStatus?.toString()
            def statusDisabled = acdUserStatus?.acdQueueStatus?.isDisabled()
            def contactNumber = acdUserStatus?.contactNumber
            def phoneNumbers = []

            PhoneNumber.findAllByOwner(user).each() { number ->
                phoneNumbers.add(number)
            }

            if (log.isDebugEnabled())
            {
                log.debug "User status contact number [${contactNumber}]"
            }

            // Get Agent's Call Queue
            params.queueSort = params.queueSort ?: 'enqueueTime'
            params.queueOrder = params.queueOrder ?: 'asc'
            params.queueMax = params.queueMax ?: 5
            params.queueOffset = params.queueOffset ?: 0

            def calls = AcdCall.createCriteria().list() {
                order(params.queueSort, params.queueOrder)
                maxResults(params.queueMax.toInteger())
                firstResult(params.queueOffset.toInteger())
                eq("user", user)
                eq("callStatus", AcdCallStatus.WAITING)
            }

            def callTotal = AcdCall.findAllByUser(user).size()

            // Get Agent Call History Details
            params.historySort = params.historySort ?: 'enqueueTime'
            params.historyOrder = params.historyOrder ?: 'asc'
            params.historyMax = params.historyMax ?: 5
            params.historyOffset = params.historyOffset ?: 0

            def callHistory = AcdCallHistory.createCriteria().list() {
                order(params.historySort, params.historyOrder)
                maxResults(params.historyMax.toInteger())
                firstResult(params.historyOffset.toInteger())
                eq("user", user)
            }

            def historyTotal = AcdCallHistory.findAllByUser(user).size()

            // Get User Skills
            def userSkills = UserSkill.findAllByUser(user)

            def model = [
                    status: status,
                    statusDisabled: statusDisabled,
                    phoneNumbers: phoneNumbers,
                    contactNumber: contactNumber,
                    callHistory: callHistory,
                    callTotal: callTotal,
                    historyTotal: historyTotal,
                    calls: calls,
                    userSkills: userSkills
            ]

            if (log.isDebugEnabled())
            {
                log.debug "Rendering view [status] with model [${model}]"
            }

            render(view: 'status', model: model)
        }

    def toggleStatus =
        {
            if (log.isDebugEnabled())
            {
                log.debug "AcdController.toggleStatus: params[${params}]"
            }

            def acdUserStatus = AcdUserStatus.findByOwner(authenticatedUser)
            if (!acdUserStatus)
            {
                log.error "Failed to find acd user status, maybe not serious"
                flash.errorMessage = message(code: 'page.acd.status.statusChange.failure.message', args: [params?.toggle_status])
                redirect(action: 'status')
                return
            }

            //Don't let them go available if contact number is null
            if(acdUserStatus.AcdQueueStatus == AcdQueueStatus.Unavailable &&
                acdUserStatus.contactNumber == null)
            {
                flash.errorMessage = message(code: 'page.acd.status.statusChange.noNumber.failure.message');
            }
            else
            {
                acdUserStatus.toggleStatus()
                acdUserStatus.statusModified = DateTime.now()

                if (log.isDebugEnabled())
                {
                    log.debug "Updating user [${acdUserStatus.owner.username}] to status [${acdUserStatus.acdQueueStatus}]"
                }

                if (acdUserStatus.acdQueueStatus && acdUserStatus.validate() &&
                        acdUserStatus.save(failOnError: true, flush: true))
                {
                    historyService.toggleACDStatus(acdUserStatus)
                    flash.successMessage = message(code: 'page.acd.status.statusChange.successful.message', args: [acdUserStatus.acdQueueStatus])
                }
                else
                {
                    log.error "Could not update user acd status."
                    flash.errorMessage = message(code: 'page.acd.status.statusChange.failure.message', args: [params?.toggle_status])
                }
            }

            redirect(action: 'status')
            return
        }

    def updateNumber =
        {
            if (log.isDebugEnabled())
            {
                log.debug "AcdController.updateNumber: params[${params}]"
            }
            def acdUserStatus = AcdUserStatus.findByOwner(authenticatedUser)
            if (!acdUserStatus)
            {
                log.error "Failed to find acd user status, maybe not serious"
                flash.errorMessage =
                    message(code: 'page.acd.status.statusChange.failure.message', args: [params?.toggle_status])
                redirect(action: 'status')
                return
            }

            if(params.contactNumber != null && !params.contactNumber.isEmpty())
            {
                PhoneNumber theNumber = PhoneNumber.get(params.contactNumber);

                acdUserStatus.setContactNumber(theNumber);
                if (acdUserStatus.validate() && acdUserStatus.save(failOnError: true, flush: true))
                {
                    historyService.updatedACDContactNumber(acdUserStatus)
                    flash.successMessage =
                        message(code: 'page.acd.status.statusNumber.successful.message',
                                args: [acdUserStatus.contactNumber.number])
                }
                else
                {
                    log.error "Failed to find phone number [${params?.contactNumber}]"
                    flash.errorMessage =
                        message(code: 'page.acd.status.statusNumber.failure.message', args: [params?.contactNumber])
                }
            }

            redirect(action: 'status')
        }

    def uploadPrompt =
        {
            def file = request.getFile('uploadFile')
            if (!file)
            {
                render('Please select a file to upload')
                return
            }

            def detector = new FileTypeDetector()
            def detectedType = detector.detectContentType(file.inputStream, file.originalFilename)
            if (detectedType != 'audio/x-wav')
            {
                render('File must be a wav file')
                return
            }

            def user = authenticatedUser
            promptFileService.save(storageLocation, file, user.organization.id)

            render('Success')
        }

    def currentCall =
        {
            def call = AcdCall.findAllByUser(authenticatedUser)
            render(view: 'currentCall', model: [calls: call])
        }

    def polledCalls =
        {
            List<AcdCall> calls = AcdCall.findAllByUser(authenticatedUser)

            def json = [:]

            def callJson = [];

            for (AcdCall thisCall : calls)
            {
                def c = [:]
                c.id = thisCall.id;
                c.ani = thisCall.ani;
                c.onHold = thisCall.onHold;
                c.sessionId = thisCall.sessionId;
                c.skill = thisCall.skill.description;
                callJson.add(c);
            }

            json.calls = callJson;

            render(contentType: 'application/json') {
                json
            }
        }

    def callerOffHold =
        {
            if (log.isDebugEnabled())
            {
                log.debug "AcdController.callerOffHold: params[${params}]"
            }

            boolean success = false;

            AcdCall thisCall = AcdCall.get(params.id);

            if (thisCall)
            {
                try
                {
                    spotCommunicationService.sendAcdOffHoldEvent(thisCall.sessionId);
                    success = true;
                }
                catch (Exception e)
                {
                    log.error("Exception sending off hold event: " + e);
                }
            }

            if (success)
            {
                thisCall.onHold = false;
                thisCall.save(flush: true);
                flash.successMessage = message(code: 'page.acd.call.offHold.success.message');
            }
            else
            {
                flash.errorMessage = message(code: 'page.acd.call.offHold.fail.message');
            }

            render(contentType: 'application/json')
                    {
                        thisCall
                    }
        }

    def callerOnHold =
        {
            if (log.isDebugEnabled())
            {
                log.debug "AcdController.callerOnHold: params[${params}]"
            }

            boolean success = false;
            AcdCall thisCall = AcdCall.get(params.id);

            if (thisCall)
            {
                try
                {
                    spotCommunicationService.sendAcdOnHoldEvent(thisCall.sessionId);
                    success = true;
                }
                catch (Exception e)
                {
                    log.error("Exception sending on hold event: " + e);
                }
            }

            if (success)
            {
                thisCall.onHold = true;
                thisCall.save(flush: true);
                flash.successMessage = message(code: 'page.acd.call.hold.success.message');
            }
            else
            {
                flash.errorMessage = message(code: 'page.acd.call.hold.fail.message');
            }

            render(contentType: 'application/json')
                    {
                        thisCall
                    }
        }

    def transferCaller =
        {
            if (log.isDebugEnabled())
            {
                log.debug "AcdController.transferCaller: params[${params}]"
            }

            boolean success = false;

            AcdCall thisCall = AcdCall.get(params.id);
            User transferTo = User.get(params.userId);

            if (thisCall && transferTo)
            {
                try
                {
                    acdService.connectCall(thisCall, transferTo);
                    success = true;
                }
                catch (Exception e)
                {
                    log.error("Exception sending transfer event: " + e);
                }
            }

            def json = [:]
            json.success = success.toString();

            render(contentType: 'application/json')
                    {
                        json
                    }
        }

    def availableTransferAgents =
        {
            if (log.isDebugEnabled())
            {
                log.debug "AcdController.availableTransferAgents: params[${params}]"
            }

            def users;

            AcdCall thisCall = AcdCall.get(params.id);

            if (thisCall)
            {
                users = acdService.getAvailableUsers(thisCall.skill);
            }

            def json = [:]

            def userJson = [];

            for (UserSkill thisUser : users)
            {
                def c = [:]
                c.id = thisUser.user.id;
                c.realName = thisUser.user.realName;
                userJson.add(c);
            }

            json.users = userJson;

            render(contentType: 'application/json')
                    {
                        json
                    }
        }

    def disconnectCaller =
        {
            if (log.isDebugEnabled())
            {
                log.debug "AcdController.disconnectCaller: params[${params}]"
            }

            boolean success = false;

            AcdCall thisCall = AcdCall.get(params.id);

            if (thisCall)
            {
                try
                {
                    acdService.disconnectCall(thisCall);
                    success = true;
                }
                catch (Exception e)
                {
                    log.error("Exception sending disconnect event: " + e);
                }
            }

            def json = [:]
            json.success = success.toString();

            render(contentType: 'application/json')
                    {
                        json
                    }
        }
}
