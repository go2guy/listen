import com.interact.listen.acd.AcdCallProcessorJob
import com.interact.listen.acd.AcdCleanupJob
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

        mailService.mailMessageBuilderFactory = customMailMessageBuilderFactory
    }
    def destroy = {
    }
}
