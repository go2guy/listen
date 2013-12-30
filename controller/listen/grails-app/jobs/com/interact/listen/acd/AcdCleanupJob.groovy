package com.interact.listen.acd

import com.interact.listen.exceptions.ListenAcdException
import org.joda.time.DateTime
import org.joda.time.Seconds

/**
 * Created with IntelliJ IDEA.
 * User: knovak
 * Date: 12/13/13
 * Time: 10:08 AM
 * To change this template use File | Settings | File Templates.
 */
class AcdCleanupJob
{
    def acdService;

    static triggers =
    {
        simple startDelay: 1000, repeatInterval: 30000
    }

    def group = "acd"

    def execute()
    {
        log.info("Executing AcdCleanupJob");

        int waitingMax = acdService.getWaitingMax();
        int connectMax = acdService.getConnectMax();

        //Get all calls enqueued too long

        //Get all calls where there are no agents with their skill

        //Get all agents who show on a call but who aren't

        def calls = acdService.listAllCalls();

        log.info("Number of waiting calls: " + calls.size());
        for(AcdCall thisCall : calls)
        {
            try
            {
                switch(thisCall.callStatus)
                {
                    case AcdCallStatus.WAITING:
                        //Verify they haven't been waiting too long
                        int waitingTime = Seconds.secondsBetween(thisCall.enqueueTime, DateTime.now()).getSeconds();
                        if(waitingTime > waitingMax)
                        {
                            //Just log for now
                            log.error("AcdCall[" + thisCall.id + "] waiting beyond the maximum allowed time!");
                        }
                        break;
                   case AcdCallStatus.CONNECT_REQUESTED:
                       //Verify they haven't been in this state too long
                       int requestTime = Seconds.secondsBetween(thisCall.lastModified, DateTime.now()).getSeconds();
                       if(requestTime > connectMax)
                       {
                           //Set back to waiting
                           log.error("AcdCall[" + thisCall.id + "] connect_requested beyond the maximum allowed time!");
//                           acdService.acdCallStatusUpdate(thisCall.sessionId, AcdCallStatus.WAITING);
                       }
                       break;
                }
            }
            catch(ListenAcdException ace)
            {
                log.error("Exception processing cleanup job: " + ace, ace);
            }
        }
    }
}
