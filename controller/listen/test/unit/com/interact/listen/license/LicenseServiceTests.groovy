package com.interact.listen.license

class LicenseServiceTests extends GroovyTestCase {
    def service = new LicenseService()

    // all licensable features are per-organization features
    void testLicensableFeatures0() {
        service.licensableFeatures().each {
            assertTrue it.isPerOrganization
        }
    }
}
