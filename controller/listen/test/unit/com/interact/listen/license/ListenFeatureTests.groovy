package com.interact.listen.license

class ListenFeatureTests extends GroovyTestCase {

    // which features are per-organization and which are not
    void testGetIsPerOrganization0() {
        assertTrue ListenFeature.ACD.isPerOrganization
        assertFalse ListenFeature.ACTIVE_DIRECTORY.isPerOrganization

        assertTrue ListenFeature.AFTERHOURS.isPerOrganization
        assertTrue ListenFeature.ATTENDANT.isPerOrganization
        assertTrue ListenFeature.BROADCAST.isPerOrganization
        assertTrue ListenFeature.CONFERENCING.isPerOrganization
        assertTrue ListenFeature.CUSTOM_APPLICATIONS.isPerOrganization
        assertTrue ListenFeature.FAX.isPerOrganization
        assertTrue ListenFeature.FINDME.isPerOrganization
        assertTrue ListenFeature.VOICEMAIL.isPerOrganization
        assertTrue ListenFeature.IPPBX.isPerOrganization
    }
}
