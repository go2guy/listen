package com.interact.listen.spot

class HrefParserServiceTests extends GroovyTestCase {
    def service = new HrefParserService()

    // returns null if href is null or if href is formatted badly
    void testIdFromHref0() {
        assertNull service.idFromHref(null)
        assertNull service.idFromHref('not/an/href')
    }

    // returns an id from a valid href
    void testIdFromHref1() {
        assertEquals 5, service.idFromHref('/resource/5')
    }
}
