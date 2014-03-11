package com.interact.listen.acd

import com.interact.listen.PhoneNumber
import com.interact.listen.User
import com.interact.listen.exceptions.ListenAcdException
import com.interact.listen.pbx.Extension
import com.interact.listen.spot.SpotCommunicationException
import com.interact.listen.acd.AcdUserStatus

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
        def results = userSkillCriteria.list() {
            eq("skill", requestedSkill)
            user {
                acdUserStatus {
                    eq("acdQueueStatus", AcdQueueStatus.Available)
                    or {
                        le("onacallModified", agentTime)
                        isNull("onacallModified")
                    }

                    eq("onACall", false)
                    order("onacallModified", "asc")
                }
            }
        }

        if(results != null && results.size() > 0)
        {
            results.sort{it.priority};
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
    def acdCallStatusUpdate(String sessionId, String thisStatus, String event) throws ListenAcdException
    {
        AcdCallStatus acdStatus = AcdCallStatus.valueOf(thisStatus);
        return acdCallStatusUpdate(sessionId, acdStatus, event);
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
        return acdCallStatusUpdate(sessionId, acdStatus, null);
    }

    /**
     * Update the status of an ACD Call in the queue.
     *
     * @param sessionId The sessionId of the call.
     * @param status The status to set the call to.
     */
    def acdCallStatusUpdate(String sessionId, AcdCallStatus thisStatus, String event) throws ListenAcdException
    {
        if(event != null)
        {
            if(log.isDebugEnabled())
            {
                log.debug("Sending generic event back to IVR.");
            }

            //if event's not null, we need to just fire back to the event
            //Send request to ivr
            try
            {
                spotCommunicationService.sendAcdGenericEvent(sessionId, event);
            }
            catch(SpotCommunicationException sce)
            {
                log.error("Exception sending generic event: " + sce, sce);
            }

        }
        else
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
                    case AcdCallStatus.ENDED:
                        acdCallEnded(acdCall, event);
                        break;
                }
            }
            else
            {
                throw new ListenAcdException("Unable to locate call sessionId[" + sessionId + "]");
            }
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
        String stringAni = ani;

        if(ani.contains(":") && ani.contains("@"))
        {
            stringAni = ani.substring( ani.indexOf(':')+1, ani.indexOf('@'));
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

        if(acdCall.validate() && acdCall.save(flush: true))
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

    public void connectCall(AcdCall thisCall, User agent) throws ListenAcdException
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

            if(agent.validate())
            {
                agent.save(flush: true);
            }
            else
            {
                agent.refresh();
                log.error("Unable to save Agent User Status: " + agent.errors.toString());
                throw new ListenAcdException("Unable to save Agent[" + agent.username + "] status: " +
                        agent.errors.toString());
            }

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

    public void transferCall(AcdCall thisCall, User agent) throws ListenAcdException
    {
        if(log.isDebugEnabled())
        {
            log.debug("Agent to connect to: " + agent.realName);
        }

        if(agent.acdUserStatus && agent.acdUserStatus.AcdQueueStatus == AcdQueueStatus.VoicemailBox)
        {
            //Connecting to voicemail, use the voicemail logic
            acdCallVoicemailPrivate(thisCall, agent.acdUserStatus.contactNumber.number);
        }
        else
        {
            boolean sessionExistsOnIvr = true;

            //Determine Phone Number to which to send
            String theNumber = null;

            if(agent.acdUserStatus != null && agent.acdUserStatus.contactNumber != null)
            {
                theNumber = agent.acdUserStatus.contactNumber.number;
            }
            else
            {
                Set<PhoneNumber> numbers = agent.phoneNumbers;
                if(numbers != null && !numbers.isEmpty())
                {
                    for(PhoneNumber number : numbers)
                    {
                        if(number != null && number instanceof Extension)
                        {
                            theNumber = number.number;
                            break;
                        }
                    }
                }
            }

            if(theNumber == null)
            {
                throw new ListenAcdException("Unable to determine phone number for transfer");
            }

            //Send request to ivr
            try
            {
                spotCommunicationService.sendAcdConnectEvent(thisCall.sessionId,
                        theNumber);
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

    public void acdCallEnded(AcdCall thisCall, String event)
    {
        //Send request to ivr
        try
        {
            spotCommunicationService.sendAcdGenericEvent(thisCall.sessionId, event);
        }
        catch(SpotCommunicationException sce)
        {
            log.error("Exception sending generic event: " + sce, sce);
        }

        //Free up the agent
        freeAgent(thisCall.user);

        //Delete call from queue. Leave status since we don't really know what happened.
        removeCall(thisCall, null);
    }

    /**
     * Send the caller to another queue.
     *
     * @param thisCall The call.
     * @param theSkill The skill of the other queue.
     * @return boolean for succes
     */
    public boolean transferCallToQueue(AcdCall thisCall, Skill skill)
    {
        //Tell the IVR to switch the queue
        boolean returnVal = false;
        boolean success = false;

        if (thisCall)
        {
            try
            {
                String artifactsDirectory = grailsApplication.config.com.interact.listen.artifactsDirectory;
                String onHoldMsg = artifactsDirectory + '/acd/' + skill.organization.id + '/' + skill.onHoldMsg;
                String onHoldMsgExtended = artifactsDirectory + '/acd/' + skill.organization.id  + '/' +
                        skill.onHoldMsgExtended;
                String onHoldMusic = artifactsDirectory + '/acd/' + skill.organization.id  + '/' + skill.onHoldMusic;
                String connectMsg = artifactsDirectory + '/acd/' + skill.organization.id  + '/' + skill.connectMsg;

                spotCommunicationService.sendAcdSwitchQueueEvent(thisCall.sessionId, onHoldMsg, onHoldMusic,
                    connectMsg, onHoldMsgExtended,);
                success = true;
            }
            catch (Exception e)
            {
                log.error("Exception sending switch queue event: " + e);
            }
        }

        if (success && skill != null)
        {
            //Free up the agent
            freeAgent(thisCall.user);

            //Set the call in the other queue
            thisCall.skill = skill;
            thisCall.callStatus = AcdCallStatus.WAITING;
            thisCall.user = null;
            thisCall.save(flush: true);
            returnVal = true;
        }

        return returnVal;
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

        if(acdCall.validate() && acdCall.save(flush: true))
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
            log.warn("Attempting to Connect Failed a call in invalid status[" +
                    acdCall.callStatus.toString() + "]");
        }

        acdCall.callStatus = AcdCallStatus.WAITING;

        disableAgent(acdCall.user);

        acdCall.user = null;

        if(acdCall.validate() && acdCall.save(flush: true))
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
     * Set an agent unavailable.
     *
     * @param user The user to set as unavailable.
     */
    private static void disableAgent(User user)
    {
        if(user != null)
        {
            LogFactory.getLog(this).warn("Setting user[" + user.realName + "] unavailable due to connect failed.");

            //Free the user
            user.acdUserStatus.onACall = false;
            user.acdUserStatus.AcdQueueStatus = AcdQueueStatus.Unavailable;
            user.save(flush: true);
        }
        else
        {
            LogFactory.getLog(this).warn("Attempted to disable a non existent user.");
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

        try
        {
            AcdCallHistory history = new AcdCallHistory(call);
            history.insert(flush: true);
        }
        catch(Exception e)
        {
            log.error("Exception writing AcdCallHistory record: " + e, e);
        }

        //Delete from the queue
        call.delete(flush: true);
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
    public static User getVoicemailUserBySkillname(String skillname)
    {
        if (skillVoicemailUsers.isEmpty())
        {
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
          // rebuild our internal hash map
          populateVoicemailUsers()
      } else {
          LogFactory.getLog(this).error("Set voice mail user by skill received invalid input")
      }
      
      return 
    }

    /**
     * Given an Acd Skill, remove the associated voicemail box for the skill
     * @param skill: The associated skill
     */
    public static void deleteVoicemailBox(Skill skill) {
      if ( skill ) {
        def vmUser = getVoicemailUserBySkillname(skill.skillname)
        if ( vmUser ) {
          LogFactory.getLog(this).debug("Removing vm user [${vmUser}] for skill [${skill.skillname}]")
          def userStatus = vmUser.acdUserStatus
          vmUser.acdUserStatus.makeUnavailable()
          vmUser.acdUserStatus.merge(failonerror: true, flush: true)
          populateVoicemailUsers()
        }
      }
    }
    
    /**
     * Populates hash map associating skills with their respective
     * voicemail user.
     */
    public static void populateVoicemailUsers()
    {
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
