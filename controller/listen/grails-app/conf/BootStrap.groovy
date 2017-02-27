import com.interact.listen.acd.AcdCallProcessorJob
import com.interact.listen.acd.AcdCleanupJob
import com.interact.listen.history.CallHistoryPostJob
import com.interact.listen.license.LicenseService
import com.interact.listen.license.ListenFeature
import grails.util.Holders

class BootStrap
{
    def customMailMessageBuilderFactory
    def mailService

    def init = { servletContext ->
        def licenseService = new LicenseService();
        licenseService.afterPropertiesSet();
        if(licenseService.isLicensed(ListenFeature.ACD))
        {
            int callProcessorInterval =
                Integer.parseInt((String)Holders.config.com.interact.listen.acd.callProcessor.repeatInterval);

            if(callProcessorInterval > 0)
            {
                log.debug("Scheduling AcdCallProcessorJob with interval[" + callProcessorInterval + "]");
                AcdCallProcessorJob.schedule(callProcessorInterval);
            }

            int cleanupInterval =
                Integer.parseInt((String)Holders.config.com.interact.listen.acd.cleanup.repeatInterval);

            if(cleanupInterval > 0)
            {
                AcdCleanupJob.schedule(cleanupInterval);
            }
        }
        else
        {
            log.debug("Listen ACD Feature is not licensed.");
        }

        // Start up Call History Post Job
        int callHistoryPostInterval =
                Integer.parseInt((String)Holders.config.com.interact.listen.callHistory.postJob.repeatInterval);

        log.debug("CallHistoryPostJob repeat interval is ${callHistoryPostInterval}");

        if (callHistoryPostInterval > 0)
        {
            log.debug("Scheduling CallHistoryPostJob with interval [${callHistoryPostInterval}]");
            CallHistoryPostJob.schedule(callHistoryPostInterval);
        }

        mailService.mailMessageBuilderFactory = customMailMessageBuilderFactory
    }
    def destroy = {
    }
}
