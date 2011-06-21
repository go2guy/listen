package com.interact.listen.license

import com.interact.license.client.UnleashLicense
import com.interact.license.client.validate.CryptoDsigSignatureValidator
import org.springframework.beans.factory.InitializingBean

class LicenseService implements InitializingBean {
    static transactional = false

    def springSecurityService

    @SuppressWarnings('GrailsStatelessService')
    private def license // set by InitializingBean#afterPropertiesSet()

    boolean isLicensed(def feature) {
        return license.isFeatureLicensed(feature)
    }
    
    List licensableFeatures() {
        Arrays.asList(ListenFeature.values())
    }

    List enableableFeatures() {
        ListenFeature.values().findAll { isLicensed(it) } as List
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

    void afterPropertiesSet() {
        def file = new File('/interact/master/.iiXmlLicense')
        def validator = new CryptoDsigSignatureValidator()
        license = new UnleashLicense(file, validator)
    }
}
