package com.interact.listen.acd

import com.interact.listen.User
import com.interact.listen.exceptions.ListenAcdException
import com.interact.listen.spot.SpotCommunicationException
import grails.validation.ValidationErrors
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
        def waitingList = AcdCall.findAllByCallStatus(AcdCallStatus.WAITING, [sort: 'enqueueTime', order: 'asc']);

        return waitingList;
    }

    /**
     * List all calls.
     *
     * @return List of calls.
     */
    def listAllCalls()
    {
        def callList = AcdCall.list();

        return callList;
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
     * Update the status of an ACD Call in the queue.
     *
     * @param sessionId The sessionId of the call.
     * @param status The status to set the call to.
     */
    def acdCallStatusUpdate(String sessionId, String status) throws ListenAcdException
    {
        AcdCallStatus thisStatus = AcdCallStatus.valueOf(status);
        return acdCallStatusUpdate(sessionId, thisStatus);
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
        acdCall.setAni(ani);
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
                if(log.isDebugEnabled())
                {
                    log.debug("Agent to handle call: " + agent.realName);
                }

                //Set agent onacall to true
                agent.acdUserStatus.onACall = true;
                agent.save(flush: true);

                boolean sessionExistsOnIvr = true;

                //Send request to ivr
                try
                {
                    spotCommunicationService.sendAcdConnectEvent(thisCall.sessionId,
                            agent.phoneNumbers.asList().get(0).number);
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

    public int getWaitingMax()
    {
        return Integer.parseInt(grailsApplication.config.com.interact.listen.acd.waiting.max);
    }

    public int getConnectMax()
    {
        return Integer.parseInt(grailsApplication.config.com.interact.listen.acd.connect_request.max);
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
    private void freeAgent(User user)
    {
        if(user != null)
        {
            //Free the user
            user.acdUserStatus.onACall = false;
            user.save(flush: true);
        }
        else
        {
            log.warn("Attempted to free a non existent user.");
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
}
