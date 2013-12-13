package com.interact.listen.acd

import com.interact.listen.User
import org.joda.time.DateTime

class AcdService
{
    def grailsApplication
    def spotCommunicationService

    /**
     * TODO: Add the logic to do this
     * @param selection
     * @return
     */

    Skill menuSelectionToSkill(String selection)
    {
        Skill skill = null;

        if(selection.equals("1"))
        {
            skill = Skill.findBySkillname("testSkill");
        }

        return skill;
    }

    def listWaitingCalls()
    {
        def waitingList = AcdCall.findAllByCallStatus(AcdCallStatus.WAITING, [sort: 'enqueueTime', order: 'asc']);

        return waitingList;
    }

    User getAvailableUser(Skill requestedSkill)
    {
        User returnVal = null;

        def availStatus = AcdQueueStatus.findByName("Available");
        DateTime agentTime =
            DateTime.now().minusSeconds(Integer.parseInt(grailsApplication.config.com.interact.listen.acd.agent.waitTime))

        def userSkillCriteria = UserSkill.createCriteria();
        def results = userSkillCriteria.list(max: 1) {
            eq("skill", requestedSkill)
            user {
                acdUserStatus {
                    eq("acdQueueStatus", availStatus)
                    le("statusModified", agentTime)
                    eq("onACall", false)
                    order("statusModified", "asc")
                }
            }
        }

        if(results != null && results.size() > 0)
        {
            returnVal = results.get(0).user;
        }

        return returnVal;
    }

    def acdCallStatusUpdate(String sessionId, String status)
    {
        AcdCall acdCall = AcdCall.findBySessionId(sessionId);

        if(acdCall != null)
        {
            if(status.equalsIgnoreCase(AcdCallStatus.CONNECTED.toString()))
            {
                //Call was connected
                acdCallConnected(acdCall);
            }
            else if(status.equalsIgnoreCase(AcdCallStatus.COMPLETED.toString()))
            {
                acdCallCompleted(acdCall);
            }
        }
    }

    void acdCallCompleted(AcdCall acdCall)
    {
        //Free the user
        User user = acdCall.user;
        user.acdUserStatus.onACall = false;
        user.acdUserStatus.statusModified = DateTime.now();
        user.save();

        //Delete from queue
        acdCall.delete();
    }

    void acdCallConnected(AcdCall acdCall)
    {
        acdCall.callStatus = AcdCallStatus.CONNECTED;

        if(acdCall.validate() && acdCall.save())
        {
            response.flushBuffer();
        }
        else
        {
            throw new Exception(beanErrors(acdCall));
        }
    }

    void processWaitingCall(AcdCall thisCall)
    {
        log.debug("Waiting call: " + thisCall.ani + ", skill: " + thisCall.skill.toString() + ", enqueueTime: " +
                thisCall.enqueueTime.toString());

        User agent = getAvailableUser(thisCall.skill);

        if(agent != null)
        {
            log.debug("Agent to handle call: " + agent.realName);

            //Set agent onacall to true
            agent.acdUserStatus.onACall = true;
            agent.acdUserStatus.statusModified = DateTime.now();
            agent.save(flush: true)

            //Send request to ivr
            spotCommunicationService.sendAcdConnectEvent(thisCall.sessionId, agent.phoneNumbers.asList().get(0).number);

            //Set call status to "ivrconnectRequested"
            thisCall.setCallStatus(AcdCallStatus.CONNECT_REQUESTED);
            thisCall.setUser(agent);
            thisCall.save(flush: true);

            //Now we just have to wait for the IVR to respond that it was connected
        }
        else
        {
            log.debug("No agents available to handle call");
        }
    }
}
