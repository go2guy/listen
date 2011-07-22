package com.interact.listen

class FilterUtilTests extends GroovyTestCase {

    // all controllers and actions that should not be logged in filters
    void testShouldLog0() {
        assertFalse FilterUtil.shouldLog('messages', 'pollingList')
        assertFalse FilterUtil.shouldLog('messages', 'newCount')
        assertFalse FilterUtil.shouldLog('login', 'authAjax')
        assertFalse FilterUtil.shouldLog('conferencing', 'polledConference')
        assertFalse FilterUtil.shouldLog('conferencing', 'ajaxPagination')
        assertFalse FilterUtil.shouldLog('fax', 'prepareStatus')

        assertTrue FilterUtil.shouldLog('messages', 'inbox')
    }

    // all controllers and actions that do not get the organization extracted
    void testShouldExtractOrganization0() {
        assertFalse FilterUtil.shouldExtractOrganization('spotApi', 'foo')
        assertFalse FilterUtil.shouldExtractOrganization('callRouting', 'foo')
        assertFalse FilterUtil.shouldExtractOrganization('faxApi', 'foo')

        assertTrue FilterUtil.shouldExtractOrganization('messages', 'inbox')
    }
}
