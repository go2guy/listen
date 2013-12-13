package com.interact.listen.acd

import com.interact.listen.User
import org.joda.time.DateTime
import org.joda.time.LocalDateTime

/**
 * Created with IntelliJ IDEA.
 * User: knovak
 * Date: 12/11/13
 * Time: 3:48 PM
 * To change this template use File | Settings | File Templates.
 */
class AcdCallProcessorJob
{
    static triggers =
    {
        simple startDelay: 1000, repeatInterval: 20000
    }

    def group = "acd"

    def acdService

    def execute() {
        log.info("Beginning AcdCallProcessorJob")

        //First get all waiting acd calls, ordered by oldest enqueue time
        def waitingCalls = acdService.listWaitingCalls();

        log.info("Number of waiting calls: " + waitingCalls.size());
        for(AcdCall thisCall : waitingCalls)
        {
            acdService.processWaitingCall(thisCall);
        }
    }
}
