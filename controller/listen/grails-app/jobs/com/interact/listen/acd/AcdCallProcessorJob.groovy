package com.interact.listen.acd

import com.interact.listen.User
import com.interact.listen.exceptions.ListenAcdException
import grails.util.Holders
import org.joda.time.DateTime
import org.joda.time.LocalDateTime

/**
 * User: knovak
 * Date: 12/11/13
 * Time: 3:48 PM
 */
class AcdCallProcessorJob
{
    def acdService

    def group = "acd"

    def concurrent = false;

    def execute()
    {
        log.info("Beginning AcdCallProcessorJob")

        //First get all waiting acd calls, ordered by oldest enqueue time
        def waitingCalls = acdService.listWaitingCalls();

        log.info("Number of waiting calls: " + waitingCalls.size());
        for(AcdCall thisCall : waitingCalls)
        {
            try
            {
                acdService.processWaitingCall(thisCall);
            }
            catch(ListenAcdException lae)
            {
                log.warn("Unable to process call: " + thisCall.getSessionId());
            }
        }
    }
}
