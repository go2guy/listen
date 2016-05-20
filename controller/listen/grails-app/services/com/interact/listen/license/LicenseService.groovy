package com.interact.listen.license

import com.interact.license.client.UnleashLicense
import com.interact.license.client.validate.CryptoDsigSignatureValidator
import grails.util.Environment
import org.springframework.beans.factory.InitializingBean

class LicenseService implements InitializingBean {
    static transactional = false

    def springSecurityService

    @SuppressWarnings('GrailsStatelessService')
    private def license // set by InitializingBean#afterPropertiesSet()

    boolean isLicensed(def feature)
    {
        boolean returnVal = false;

        switch(Environment.current)
        {
            case Environment.DEVELOPMENT:
            case Environment.TEST:
                returnVal = true;
                break;
            case Environment.PRODUCTION:
                returnVal = license.isFeatureLicensed(feature);
                break;
            default:
                returnVal = true;
                break;
        }

        return returnVal;
    }
    
    List licensableFeatures() {
        Arrays.asList(ListenFeature.values()).findAll { it.isPerOrganization }
    }

    List enableableFeatures() {
        licensableFeatures().findAll { isLicensed(it) }
    }

    boolean canAccess(def feature, def organization = null) {
        if(!isLicensed(feature)) {
            return false
        }

        def org = organization ?: springSecurityService.getCurrentUser()?.organization
        if(!org) {
            return true
        }

        return org.enabledFeatures.contains(feature)
    }

    void afterPropertiesSet()
    {
        if(license == null)
        {
            File file = new File('/interact/master/.iiXmlLicense');
            def validator = new CryptoDsigSignatureValidator();
            license = new UnleashLicense(file, validator)
        }
    }
}
