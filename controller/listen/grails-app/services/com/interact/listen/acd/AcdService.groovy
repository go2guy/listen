package com.interact.listen.acd

import com.interact.listen.PhoneNumber
import com.interact.listen.User
import com.interact.listen.exceptions.ListenAcdException
import com.interact.listen.spot.SpotCommunicationException

import grails.validation.ValidationErrors
import org.apache.commons.logging.LogFactory
import org.joda.time.DateTime
import org.springframework.validation.FieldError

import java.security.InvalidParameterException

/**
 * Service class for ACD processing.
 */
class AcdService
{
    def grailsApplication
    def spotCommunicationService
    private static Map<String,User> skillVoicemailUsers = new HashMap<String,User>()

    /**
     * Convert an input selection to a Skill object.
     *
     * @param selection The caller's selection.
     * @return The skill associated with this selection
     */
    Skill menuSelectionToSkill(String selection)
    {
        Skill skill = Skill.get(Integer.parseInt(selection));

        return skill;
    }

    /**
     * List all calls currently in "waiting" status, ordered by queued time.
     *
     * @return List of calls in waiting status, ordered by queued time.
     */
    def listWaitingCalls()
    {
        def waitingList;

        String ivr = getIvr();
        if(ivr != null)
        {
            if(log.isDebugEnabled())
            {
                log.debug("Getting waiting calls using ivr[" + ivr + "]");
            }
            waitingList = AcdCall.findAllByCallStatusAndIvr(AcdCallStatus.WAITING, ivr,
                    [sort: 'enqueueTime', order: 'asc']);
        }
        else
        {
            log.warn("IVR not configured, getting waiting calls for any ivr.");

            waitingList = AcdCall.findAllByCallStatus(AcdCallStatus.WAITING, [sort: 'enqueueTime', order: 'asc']);
        }

        return waitingList;
    }

    /**
     * List all calls.
     *
     * @return List of calls.
     */
    def listAllCalls()
    {
        def returnVal;

        if(getIvr() != null)
        {
            returnVal = AcdCall.findAllByIvr(getIvr());
        }
        else
        {
            returnVal = AcdCall.list();
        }

        return returnVal;
    }

    /**
     * Get a user who is available based on the requested skill.
     *
     * @param requestedSkill The skill requested.
     * @return User who has this skill, and has been available for the longest time.
     */
    User getAvailableUser(Skill requestedSkill)
    {
        User returnVal = null;

        int agentWaitTime = 0;

        //For some reason we can't guarantee what the type of this config value is
        if(grailsApplication.config.com.interact.listen.acd.agent.waitTime instanceof String)
        {
            agentWaitTime = Integer.parseInt(grailsApplication.config.com.interact.listen.acd.agent.waitTime)
        }
        else
        {
            agentWaitTime = grailsApplication.config.com.interact.listen.acd.agent.waitTime;
        }

        DateTime agentTime = DateTime.now().minusSeconds(agentWaitTime);

        def userSkillCriteria = UserSkill.createCriteria();
        def results = userSkillCriteria.list(max: 1) {
            eq("skill", requestedSkill)
            user {
                acdUserStatus {
                    eq("acdQueueStatus", AcdQueueStatus.Available)
                    le("onacallModified", agentTime)
                    eq("onACall", false)
                    order("onacallModified", "asc")
                }
            }
        }

        if(results != null && results.size() > 0)
        {
            returnVal = results.get(0).user;
        }

        return returnVal;
    }

    /**
     * Get a list of users who are available based on the requested skill.
     *
     * @param requestedSkill The skill requested.
     * @return Users who have this skill.
     */
    List<UserSkill> getAvailableUsers(Skill requestedSkill)
    {
        def userSkillCriteria = UserSkill.createCriteria();
        def results = userSkillCriteria.list()
        {
            eq("skill", requestedSkill)
            user {
                acdUserStatus {
                    eq("onACall", false)
                    'in'("acdQueueStatus",[AcdQueueStatus.Available, AcdQueueStatus.VoicemailBox])
                    order("acdQueueStatus", "asc")
                }
            }
        }

        return results;
    }

    /**
     * Update the status of an ACD Call in the queue.
     *
     * @param sessionId The sessionId of the call.
     * @param status The status to set the call to.
     */
    def acdCallStatusUpdate(String sessionId, String thisStatus) throws ListenAcdException
    {
        AcdCallStatus acdStatus = AcdCallStatus.valueOf(thisStatus);
        return acdCallStatusUpdate(sessionId, acdStatus);
    }

    /**
     * Update the status of an ACD Call in the queue.
     *
     * @param sessionId The sessionId of the call.
     * @param status The status to set the call to.
     */
    def acdCallStatusUpdate(String sessionId, AcdCallStatus thisStatus) throws ListenAcdException
    {
        AcdCall acdCall = AcdCall.findBySessionId(sessionId);

        if(acdCall != null)
        {
            switch(thisStatus)
            {
                case AcdCallStatus.CONNECTED:
                    //Call was connected
                    acdCallConnected(acdCall);
                    break;
                case AcdCallStatus.COMPLETED:
                case AcdCallStatus.DISCONNECTED:
                case AcdCallStatus.VOICEMAIL:
                    acdCallCompleted(acdCall, thisStatus);
                    break;
                case AcdCallStatus.CONNECT_FAIL:
                    acdCallConnectFailed(acdCall);
                    break;
                case AcdCallStatus.WAITING:
                    acdCallWaiting(acdCall);
                    break;
            }
        }
        else
        {
            throw new ListenAcdException("Unable to locate call sessionId[" + sessionId + "]");
        }
    }

    /**
     * Add a call to the ACD Queue.
     *
     * @param ani The ani of the call.
     * @param dnis The dnis of the call.
     * @param selection The callers skill selection.
     * @param sessionId The sessionid of the call.
     * @throws ListenAcdException If an exception adding the call to the queue.
     */
    void acdCallAdd(String ani, String dnis, String selection, String sessionId, String ivr) throws ListenAcdException
    {
        AcdCall acdCall = new AcdCall();
        String stringAni = ani.substring( ani.indexOf(':')+1, ani.indexOf('@'));
        if(stringAni.equalsIgnoreCase("unknown"))
        {
            stringAni = "4024768786";
        }
        acdCall.setAni(stringAni);
        acdCall.setDnis(dnis);
        Skill skill = menuSelectionToSkill(selection);
        if(skill == null)
        {
            throw new InvalidParameterException("Invalid skill requested: " + selection);
        }

        acdCall.setSkill(skill);
        acdCall.setSessionId(sessionId);
        acdCall.setCallStatus(AcdCallStatus.WAITING);
        acdCall.setIvr(ivr);

        if(acdCall.validate() && acdCall.save())
        {
            log.debug("Successfully added call to queue.")
        }
        else
        {
            throw new ListenAcdException(beanErrors(acdCall));
        }
    }

    /**
     * Process a waiting call from the queue.
     *
     * @param thisCall The call to process.
     */
    public void processWaitingCall(AcdCall thisCall) throws ListenAcdException
    {
        try
        {
            if(log.isDebugEnabled())
            {
                log.debug("Waiting call: " + thisCall.ani + ", skill: " + thisCall.skill.toString() + ", enqueueTime: " +
                    thisCall.enqueueTime.toString());
            }

            User agent = getAvailableUser(thisCall.skill);

            if(agent != null)
            {
                connectCall(thisCall, agent);
            }
            else
            {
                if(log.isInfoEnabled())
                {
                    log.info("No agents available to handle call");
                }
            }
        }
        catch(Exception e)
        {
            log.error("Exception processing waiting call: " + e, e);
            throw new ListenAcdException(e.getMessage());
        }
    }

    public void connectCall(AcdCall thisCall, User agent)
    {
        if(log.isDebugEnabled())
        {
            log.debug("Agent to connect to: " + agent.realName);
        }

        if(agent.acdUserStatus.AcdQueueStatus == AcdQueueStatus.VoicemailBox)
        {
            //Connecting to voicemail, use the voicemail logic
            acdCallVoicemailPrivate(thisCall, agent.acdUserStatus.contactNumber.number);
        }
        else
        {
            boolean sessionExistsOnIvr = true;

            //Set agent onacall to true
            agent.acdUserStatus.onACall = true;
            agent.save(flush: true);


            //Send request to ivr
            try
            {
                spotCommunicationService.sendAcdConnectEvent(thisCall.sessionId,
                        agent.acdUserStatus.contactNumber.number);
            }
            catch(SpotCommunicationException sce)
            {
                //for now, we are going to assume this means that this session does not exist any longer
                sessionExistsOnIvr = false;
            }

            if(sessionExistsOnIvr)
            {
                //Set call status to "ivrconnectRequested"
                thisCall.setCallStatus(AcdCallStatus.CONNECT_REQUESTED);
                thisCall.setUser(agent);
                thisCall.save(flush: true);

                //Now we just have to wait for the IVR to respond that it was connected
            }
            else
            {
                //Free up the agent
                freeAgent(agent);

                //Delete call from queue. Leave status since we don't really know what happened.
                removeCall(thisCall, null);
            }
        }
    }

    public void disconnectCall(AcdCall thisCall)
    {
        //Send request to ivr
        try
        {
            spotCommunicationService.sendAcdDisconnectEvent(thisCall.sessionId);
        }
        catch(SpotCommunicationException sce)
        {
            log.error("Exception disconnecting call: " + sce, sce);
        }

        //Free up the agent
        freeAgent(thisCall.user);

        //Delete call from queue. Leave status since we don't really know what happened.
        removeCall(thisCall, null);
    }

    public int getWaitingMax()
    {
        int returnVal;

        if(grailsApplication.config.com.interact.listen.acd.waiting.max instanceof String)
        {
            returnVal = Integer.parseInt(grailsApplication.config.com.interact.listen.acd.waiting.max);
        }
        else
        {
            returnVal = grailsApplication.config.com.interact.listen.acd.waiting.max;
        }

        return returnVal;
    }

    public int getConnectMax()
    {
        int returnVal;

        if(grailsApplication.config.com.interact.listen.acd.connect_request.max instanceof String)
        {
            returnVal = Integer.parseInt(grailsApplication.config.com.interact.listen.acd.connect_request.max);
        }
        else
        {
            returnVal = grailsApplication.config.com.interact.listen.acd.connect_request.max;
        }

        return returnVal;
    }

    public int getEnqueueMax()
    {
        int returnVal;

        if(grailsApplication.config.com.interact.listen.acd.enqueue.max instanceof String)
        {
            returnVal = Integer.parseInt(grailsApplication.config.com.interact.listen.acd.enqueue.max);
        }
        else
        {
            returnVal = grailsApplication.config.com.interact.listen.acd.enqueue.max;
        }

        return returnVal;
    }

    public String getVoicemailBox(String sessionId) throws ListenAcdException
    {
        String returnVal;

        //Get the skill requested in the session
        AcdCall theCall = AcdCall.findBySessionId(sessionId);

        def userSkillCriteria = UserSkill.createCriteria();
        def results = userSkillCriteria.list(max: 1) {
            eq("skill", theCall.skill)
            user {
                acdUserStatus {
                    eq("acdQueueStatus", AcdQueueStatus.VoicemailBox)
                }
            }
        }

        if(results != null && results.size() > 0)
        {
            PhoneNumber vmNumber = results.get(0).user.acdUserStatus.contactNumber;
            if(vmNumber != null)
            {
                returnVal = vmNumber.number;
            }
            else
            {
                log.error("Voicemail box not configured for skill[" + theCall.skill + "]");
                throw new ListenAcdException("Invalid voicmailbox for skill[" + theCall.skill + "]");
            }
        }

        return returnVal;
    }

    
    /**
     * Set a call to voicemail status.
     *
     * @param acdCall The call to set as voicemail.
     * @throws ListenAcdException If unable to set call to voicemail.
     */
    public void acdCallVoicemail(AcdCall acdCall) throws ListenAcdException
    {
        String number = getVoicemailBox(acdCall.sessionId);
        acdCallVoicemailPrivate(acdCall, number);
    }

    /**
     * Set a call to voicemail status. Had to add Private to the classname because Grails is bogus and doesn't allow
     * overloading a method, which is a standard object oriented practice. Bogus.
     *
     * @param acdCall The call to set as voicemail.
     * @throws ListenAcdException If unable to set call to voicemail.
     */
    private void acdCallVoicemailPrivate(AcdCall acdCall, String number) throws ListenAcdException
    {
        //Send request to the IVR
        try
        {
            spotCommunicationService.sendAcdVoicemailEvent(acdCall.sessionId, number);
        }
        catch(IOException ioe)
        {
            log.error("IOException sending ACD Voicemail Event: " + ioe, ioe);
        }
        catch(SpotCommunicationException sce)
        {
            log.error("SPOT Communication Exception sending ACD Voicemail Event: " + sce, sce);
        }

        //Go on, though
        if(acdCall.user)
        {
            freeAgent(acdCall.user);
        }

        //Delete call from queue.
        removeCall(acdCall, AcdCallStatus.VOICEMAIL);
    }


    /**
     * Execute when ACD Call has completed.
     *
     * @param acdCall The call to complete.
     * @throws ListenAcdException If unable to set call to completed.
     */
    private void acdCallCompleted(AcdCall acdCall, AcdCallStatus status) throws ListenAcdException
    {
        //Free the user
        freeAgent(acdCall.user);

        //Delete from queue.
        removeCall(acdCall, status);
    }

    /**
     * Execute when an ACD Call has connected.
     *
     * @param acdCall The call to set as connected.
     * @throws ListenAcdException If unable to set call to completed.
     */
    private void acdCallConnected(AcdCall acdCall) throws ListenAcdException
    {
        if(acdCall.callStatus != AcdCallStatus.CONNECT_REQUESTED)
        {
            log.warn("Attempting to Connect a call in invalid status[" + acdCall.callStatus.toString() + "]");
        }

        acdCall.callStatus = AcdCallStatus.CONNECTED;
        acdCall.callStart = DateTime.now();

        if(acdCall.validate() && acdCall.save())
        {
            if(log.isDebugEnabled())
            {
                log.debug("Call connect processing completed successfully.")
            }
        }
        else
        {
            throw new ListenAcdException(beanErrors(acdCall));
        }
    }

    /**
     * Set a call to waiting status.
     *
     * @param acdCall The call to set as connected.
     * @throws ListenAcdException If unable to set call to waiting.
     */
    private void acdCallWaiting(AcdCall acdCall) throws ListenAcdException
    {
        if(acdCall.user != null)
        {
            freeAgent(acdCall.user);
        }

        acdCall.callStatus = AcdCallStatus.WAITING;
        acdCall.user = null;

        if(acdCall.validate() && acdCall.save(flush: true))
        {
            if(log.isDebugEnabled())
            {
                log.debug("Call status update completed successfully.")
            }
        }
        else
        {
            throw new ListenAcdException(beanErrors(acdCall));
        }
    }

    /**
     * Execute when an ACD Call has failed to connect.
     *
     * @param acdCall The call to process as failed.
     * @throws ListenAcdException If an exception setting call to connect failed.
     */
    private void acdCallConnectFailed(AcdCall acdCall) throws ListenAcdException
    {
        if(acdCall.callStatus != AcdCallStatus.CONNECT_REQUESTED)
        {
            throw new ListenAcdException("Attempting to Connect Failed a call in invalid status[" +
                    acdCall.callStatus.toString() + "]");
        }

        acdCall.callStatus = AcdCallStatus.WAITING;

        freeAgent(acdCall.user);

        acdCall.user = null;

        if(acdCall.validate() && acdCall.save())
        {
            if(log.isDebugEnabled())
            {
                log.debug("ACD Call set to waiting state.")
            }
        }
        else
        {
            throw new Exception(beanErrors(acdCall));
        }
    }

    /**
     * Free an agent.
     *
     * @param user The user to set as available.
     */
    private static void freeAgent(User user)
    {
        if(user != null)
        {
            //Free the user
            user.acdUserStatus.onACall = false;
            user.save(flush: true);
        }
        else
        {
            LogFactory.getLog(this).warn("Attempted to free a non existent user.");
        }
    }

    /**
     * Display the errors from the "bean". Using real code that humans can understand. And semicolons.
     *
     * @param bean The "bean" (could be anything!)
     * @return A "def", you know, so that nobody knows what it is.
     */
    private def beanErrors(def bean)
    {
        StringBuilder result = new StringBuilder();
        ValidationErrors theseErrors = bean.getErrors();

        for(FieldError thisError : theseErrors.fieldErrors)
        {
            result.append(thisError.toString()).append("\n");
        }

        String resultString = result.toString();

        if(log.isDebugEnabled())
        {
            log.debug("Built beanErrors: " + resultString);
        }

        return resultString;
    }

    /**
     * Remove a call from the queue. Update status first to keep the history.
     *
     * @param call The call to remove.
     */
    private void removeCall(AcdCall call, AcdCallStatus lastStatus)
    {
        //Set the status so it is preserved in the history
        if(lastStatus != null)
        {
            call.callStatus = lastStatus;
            call.callEnd = DateTime.now();
            call.save(flush: true);
        }

        AcdCallHistory history = new AcdCallHistory(call);
        history.insert();

        //Delete from the queue
        call.delete();
    }

    /**
     * Return the ivr for this controller
     * @return The Ivr.
     */
    private String getIvr()
    {
        String returnVal = null;

        if(grailsApplication.config.com.interact.listen.ivr != null &&
                !grailsApplication.config.com.interact.listen.ivr.isEmpty())
        {
            returnVal = grailsApplication.config.com.interact.listen.ivr;
        }

        return returnVal;
    }

    /**
     * Given an Acd Skill, return the associated voicemail user
     * @param skill: The associated skill
     */
    public static User getVoicemailUserBySkillname(String skillname) {
      if (skillVoicemailUsers.isEmpty()) {
        this.populateVoicemailUsers()
      }
      return skillVoicemailUsers.get(skillname);
    }

    /**
     * Given an Acd Skill and a User, set the user to be the voicemailbox for the skill
     * @param skill: The associated skill
     * @param user: The associated user
     */
    public static void setVoicemailUserBySkillname(Skill skill, User user) {
      if((skill) && (user)) {
          LogFactory.getLog(this).debug("Set vm user [${user.realName}] for skill [${skill.skillname}]")
          def currUser = getVoicemailUserBySkillname(skill.skillname)
          if (currUser) {
              LogFactory.getLog(this).debug("Currently user [${currUser.realName}] is associated with skill [${skill.skillname}]")
              def currUserAcdUserStatus = AcdUserStatus.findByOwner(currUser)
              if(currUserAcdUserStatus) {
                  currUserAcdUserStatus.makeUnavailable()
              }
          }
          def newUserAcdUserStatus = AcdUserStatus.findByOwner(user)
          if(newUserAcdUserStatus) {
              LogFactory.getLog(this).debug("Setting user [${user.realName}] as vm box for skill [${skill.skillname}]")
              newUserAcdUserStatus.makeVoicemailBox()
          }
          // rebuild our internal has map
          populateVoicemailUsers()
      } else {
          LogFactory.getLog(this).error("Set voice mail user by skill received invalid input")
      }
      
      return 
    }
    
    /**
     * Populates hash map associating skills with their respective
     * voicemail user.
     */
    public static void populateVoicemailUsers() {
        skillVoicemailUsers = [:]
        Skill.findAll().each() { skill ->
        UserSkill.findAllBySkill(skill).find { userSkill ->
            if ( userSkill.user.acdUserStatus.AcdQueueStatus == AcdQueueStatus.VoicemailBox ) {
                skillVoicemailUsers.put(skill.skillname, userSkill.user)
                return true
            }
            return false
        }
      }
    }

    /**
     * Get a list of acd users.
     *
     * @return Users.
     */
    List<User> getAcdAgentList()
    {
        def userCriteria = User.createCriteria();
        def results = userCriteria.list(sort: "realName")
        {
            isNotNull("organization")
            acdUserStatus {
                ne("acdQueueStatus",AcdQueueStatus.VoicemailBox)
            }

            order("realName", "asc")
        }


        return results;
    }

    def callHistoryList(String sort, String order, String max, String offset, DateTime theStart, DateTime theEnd,
                        String agentId, String skillId)
    {
        def calls = AcdCallHistory.createCriteria().list(
            sort: sort, order: order, max: max, offset: offset)
            {
                ge("enqueueTime", theStart)
                le("enqueueTime", theEnd)

                if(agentId && !agentId.isEmpty())
                {
                    user {
                        eq("id", Long.parseLong(agentId))
                    }
                }

                if(skillId && !skillId.isEmpty())
                {
                    skill {
                        eq("id", Long.parseLong(skillId))
                    }
                }
            }

        return calls;
    }
}
