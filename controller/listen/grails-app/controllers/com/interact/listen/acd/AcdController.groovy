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
import org.joda.time.LocalDateTime
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

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
      pollQueue: 'GET',
      pollHistory: 'GET',
      pollStatus: 'GET'
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
      if (!authenticatedUser) {
        //Redirect to login
        redirect(controller: 'login', action: 'auth');
      }
      def user = authenticatedUser

      params.sort = params.sort ?: 'enqueueTime'
      params.order = params.order ?: 'asc'
      params.max = params.max ?: 10
      params.offset = params.offset ?: 0

      def calls = []
      def callTotal = 0

      if ( user.hasRole('ROLE_ORGANIZATION_ADMIN') ) { // get all calls
        calls = AcdCall.createCriteria().list() {
            order(params.sort, params.order)
            maxResults(params.max.toInteger())
            firstResult(params.offset.toInteger())
        }
        callTotal = AcdCall.findAll().size()
      }
      else { // Get calls for the current user
        def skills = []
        UserSkill.findAllByUser(user).each() { userSkill ->
          skills.add(userSkill.skill)
        }
        calls = AcdCall.createCriteria().list() {
            order(params.sort, params.order)
            maxResults(params.max.toInteger())
            firstResult(params.offset.toInteger())
            'in'("skill",skills)
        }
        def allCalls = AcdCall.createCriteria().list() {
          'in'("skill",skills)
        }
        callTotal = allCalls.size()
      }

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

    def pollQueue = {
      def user = authenticatedUser

      params.sort = params.sort ?: 'enqueueTime'
      params.order = params.order ?: 'asc'
      params.max = params.max ?: 10
      params.offset = params.offset ?: 0

      def json = [:]

      List<AcdCall> calls = []

      if ( user.hasRole('ROLE_ORGANIZATION_ADMIN') ) { // get all calls
        calls = AcdCall.createCriteria().list {
          order(params.sort, params.order)
          maxResults(params.max.toInteger())
          firstResult(params.offset.toInteger())
        }
      }
      else { // get calls for current users skillset
        def skills = []
        UserSkill.findAllByUser(user).each() { userSkill ->
          skills.add(userSkill.skill)
        }
        calls = AcdCall.createCriteria().list {
          order(params.sort, params.order)
          maxResults(params.max.toInteger())
          firstResult(params.offset.toInteger())
          'in'("skill",skills)
        }
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

      List<AcdCallHistory> calls = []

      if ( user.hasRole('ROLE_ORGANIZATION_ADMIN') ) { // get all call history
        calls = AcdCallHistory.createCriteria().list {
            order(params.sort, params.order)
            maxResults(params.max.toInteger())
            firstResult(params.offset.toInteger())
        }
      }
      else { // get call history for current user
        calls = AcdCallHistory.createCriteria().list {
            order(params.sort, params.order)
            maxResults(params.max.toInteger())
            firstResult(params.offset.toInteger())
            eq("user",user)
        }
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

    def pollStatus = {
      def user = authenticatedUser

      params.queueSort = params.queueSort ?: 'enqueueTime'
      params.queueOrder = params.queueOrder ?: 'asc'
      params.queueMax = params.queueMax ?: 5

      def json = [:]

      // get agent call queue
      def skills = []
      UserSkill.findAllByUser(user).each() { userSkill ->
        skills.add(userSkill.skill)
      }

      List<AcdCall> calls = AcdCall.createCriteria().list {
        order(params.queueSort,params.queueOrder)
        maxResults(params.queueMax.toInteger())
        // firstResult(params.offset.toInteger())
        eq("callStatus",AcdCallStatus.WAITING)
        'in'("skill",skills)
      }

      def callJson = []
      for(AcdCall call : calls) {
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
        if ( call.user != null ) {
          c.user = call.user.realName
        }
        callJson.add(c)
      }

      json.calls = callJson

      if(log.isDebugEnabled()) {
        log.debug "Rendering agent status as json [${json.toString()}]"
      }

      render(contentType: 'application/json') {
        json
      }
    }

    def status = {
      if (!authenticatedUser)
      {
        //Redirect to login
        redirect(controller: 'login', action: 'auth');
      }

      def user = authenticatedUser

      // Get Agent Status Details
      def acdUserStatus = AcdUserStatus.findByOwner(user)
      if (!acdUserStatus) {
          log.debug "user does not currently have an acd user status [${acdUserStatus}]"
          acdUserStatus = new AcdUserStatus()
          acdUserStatus.owner = user
          acdUserStatus.acdQueueStatus = AcdQueueStatus.Unavailable
          /* Create user acd status entry, as they should have had one already */
          if (acdUserStatus.validate() && acdUserStatus.save(failOnError: true, flush: true)) {
              log.error "Created acd user status for this user [${acdUserStatus.owner}]"
          } else {
              log.error "Could not create Acd Status Entry for new user."
          }
      }

      def status = acdUserStatus?.acdQueueStatus?.toString()
      def statusDisabled = acdUserStatus?.acdQueueStatus?.isDisabled()
      def contactNumber = acdUserStatus?.contactNumber
      def phoneNumbers = []

      // Get List of Agent's Available Contact Numbers
      PhoneNumber.findAllByOwner(user).each() { number ->
        phoneNumbers.add(number)
      }

      // Get Agent's Skillset
      def userSkills = UserSkill.findAllByUser(user)

      // Get Current Call
      def currentCalls = AcdCall.findAllByUser(user)
      def currentCallTotal = AcdCall.countByUser(user)

      // Query for waiting calls associated with agent's skillset
      params.queueSort = params.queueSort ?: 'enqueueTime'
      params.queueOrder = params.queueOrder ?: 'asc'
      params.queueMax = params.queueMax ?: 5
      params.queueOffset = params.queueOffset ?: 0

      def skills = []
      userSkills.each() { userSkill ->
        skills.add(userSkill.skill)
      }

      def calls = AcdCall.createCriteria().list() {
        order(params.queueSort,params.queueOrder)
        maxResults(params.queueMax.toInteger())
        firstResult(params.queueOffset.toInteger())
        eq("callStatus",AcdCallStatus.WAITING)
        'in'("skill",skills)
      }

      if (log.isDebugEnabled())
      {
          log.debug "User status contact number [${contactNumber}]"
      }

      def allCalls = AcdCall.createCriteria().list() {
        eq("callStatus",AcdCallStatus.WAITING)
        'in'("skill",skills)
      }
      def callTotal = allCalls.size()

      // Get Agent's Call History
      params.historySort = params.historySort ?: 'enqueueTime'
      params.historyOrder = params.historyOrder ?: 'asc'

      LocalDate today = new LocalDate()

      def callHistory = AcdCallHistory.createCriteria().list() {
        order(params.historySort, params.historyOrder)
        eq("user", user)
        ge("enqueueTime",today.toDateTimeAtStartOfDay())
      }

      // def historyTotal = AcdCallHistory.findAllByUser(user).size()
      def historyTotal = AcdCallHistory.countByUser(user);

      def model = [
        status: status,
        statusDisabled: statusDisabled,
        phoneNumbers: phoneNumbers,
        contactNumber: contactNumber,
        callHistory: callHistory,
        callTotal: callTotal,
        historyTotal: historyTotal,
        calls: calls,
        userSkills: userSkills,
        currentCalls: currentCalls,
        currentCallTotal: currentCallTotal
      ]

      if (log.isDebugEnabled()) {
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
                users = acdService.getAcdAgentList();
            }

            def json = [:]

            def userJson = [];

            for (User thisUser : users)
            {
                def c = [:]
                c.id = thisUser.id;
                c.realName = thisUser.realName;
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
                    acdService.acdCallVoicemail(thisCall);
                    success = true;
                }
                catch (Exception e)
                {
                    log.error("Exception sending voicemail event: " + e);
                }
            }

            def json = [:]
            json.success = success.toString();

            render(contentType: 'application/json')
                    {
                        json
                    }
        }

    def exportHistoryToCsv =
    {
        if (log.isDebugEnabled())
        {
            log.debug "AcdController.exportHistoryToCsv: params[${params}]"
        }

        params.sort = 'enqueueTime'
        params.order = 'desc'
        params.max = '1000'
        params.offset = '0'

        DateTime theStart = getStartDate(params.startDate);
        DateTime theEnd = getEndDate(params.endDate);

        def calls = acdService.callHistoryList(params.sort, params.order, params.max, params.offset, theStart, theEnd,
                params.agent, params.skill);

        String filename = "acdCallHistoryRecords${new LocalDateTime().toString('yyyyMMddHHmmss')}.csv";

        File tmpfile
        try
        {
            if(log.isDebugEnabled())
            {
                log.debug("Creating temp file to extract ACD Call History Records")
            }
            tmpfile = File.createTempFile("./" + filename,".tmp");
            tmpfile.deleteOnExit();
            if(log.isDebugEnabled())
            {
                log.debug("Created tmp file [${tmpfile.getName()}] to extract ACD Call History Records");
            }
        }
        catch (IOException e)
        {
            log.error("Failed to create temp file for export ${e}")
            flash.errorMessage = message(code: 'page.administration.acd.callHistory.exportCSV.fileCreateFailed');
            redirect(action: "callHistory");
            //TODO perhaps do something to notify system administrators of an important event?
            return
        }

        //Create header row
        tmpfile << AcdCallHistory.csvHeader();
        tmpfile << "\n";

        //Write each row
        for(AcdCallHistory thisHistory : calls)
        {
            tmpfile << thisHistory.csvRow();
            tmpfile << "\n";
        }

        if(log.isDebugEnabled())
        {
            log.debug "Generated ACD Call History Records report of size [${tmpfile.length()}]"
        }

        //Now write the outputfile
        response.contentType = 'text/csv';
        response.setHeader('Content-disposition', "attachment;filename=" + filename);
        response.setHeader('Content-length', "${tmpfile.length()}")

        OutputStream outStream = new BufferedOutputStream(response.outputStream)
        InputStream inStream = tmpfile.newInputStream()

        byte[] bytes = new byte[4096]
        int bytesRead;

        while((bytesRead = inStream.read(bytes)) != -1)
        {
            outStream.write(bytes, 0, bytesRead)
        }

        //Close down
        inStream.close()
        outStream.flush()
        outStream.close()

        if (!tmpfile.delete())
        {
            log.error("Failed to delete temporary file [${tmpfile.getName()}]")
        }
        else if(log.isDebugEnabled())
        {
            log.debug("Succeeded in deleting temporary file [${tmpfile.getName()}]")
        }
    }

    def callHistory =
    {
      if (!authenticatedUser) {
        //Redirect to login
        redirect(controller: 'login', action: 'auth');
      }
      def user = authenticatedUser

      if (log.isDebugEnabled())
      {
          log.debug "AcdController.callHistory: params[${params}]";
      }

      params.sort = params.sort ?: 'enqueueTime'
      params.order = params.order ?: 'desc'
      params.max = params.max ?: '100'
      params.offset = params.offset ?: '0'

      DateTimeFormatter dtf = DateTimeFormat.forPattern("MM/dd/yyyy");
      DateTime theStart = getStartDate(params.startDate);
      DateTime theEnd = getEndDate(params.endDate);

      def calls = []
      if ( !user.hasRole( 'ROLE_ORGANIZATION_ADMIN' ) ) {
        params.agent = params.agent ?: user.id
      }
      params.agent = params.agent ?: ""
      calls = acdService.callHistoryList(params.sort, params.order, params.max, params.offset, theStart, theEnd,
                      params.agent.toString(), params.skill);
      def callTotal = calls.totalCount;

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
      json.filtered = true
      json.startDate =  dtf.print(theStart);
      json.endDate = dtf.print(theEnd);
      json.agent = params.agent;

      def agentJson = [];
      List<User> agents = []
      if ( user.hasRole('ROLE_ORGANIZATION_ADMIN') ) { // find all agents
        agents = acdService.acdAgentList;
      }
      else { // list only the current user
        agents.add(user)
      }
      for (User agent : agents)
      {
          def d = [:]
          d.id = agent.id;
          d.realName = agent.realName;
          agentJson.add(d);
      }

      json.skill = params.skill;

      def skillJson = [];
      List<Skill> skills = []
      if ( user.hasRole('ROLE_ORGANIZATION_ADMIN') ) { // find all skills
        skills = Skill.list(sort: "skillname");
      }
      else { // find only skills associated with current user
        UserSkill.findAllByUser(user).each() { userSkill ->
          skills.add(userSkill.skill)
        }
      }
      for (Skill skill : skills)
      {
          def e = [:]
          e.id = skill.id;
          e.skillname = skill.description;
          skillJson.add(e);
      }

      json.skillList = skillJson;
      json.agentList = agentJson;

      render(view: 'callHistory', model: json)
    }

    private def getStartDate(String inputStart)
    {
        DateTimeFormatter dtf = DateTimeFormat.forPattern("MM/dd/yyyy");
        DateTime theStart;

        if(inputStart && !inputStart.isEmpty())
        {
            theStart = DateTime.parse(inputStart, dtf);
        }
        else
        {
            theStart = DateTime.now().minusDays(1);
        }

        theStart = theStart.withHourOfDay(0);
        theStart = theStart.withMinuteOfHour(0);
        theStart = theStart.withSecondOfMinute(0);

        return theStart;
    }

    private def getEndDate(String inputEnd)
    {
        DateTimeFormatter dtf = DateTimeFormat.forPattern("MM/dd/yyyy");
        DateTime theEnd;

        if(inputEnd && !inputEnd.isEmpty())
        {
            theEnd = DateTime.parse(inputEnd, dtf);
        }
        else
        {
            theEnd = DateTime.now();
        }

        theEnd = theEnd.withHourOfDay(23);
        theEnd = theEnd.withMinuteOfHour(59);
        theEnd = theEnd.withSecondOfMinute(59);

        return theEnd;
    }
}
