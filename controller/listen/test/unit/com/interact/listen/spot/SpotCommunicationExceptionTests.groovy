package com.interact.listen.spot

class SpotCommunicationExceptionTests extends GroovyTestCase {

    // exception constructed with message sets the superclass message
    void testConstruct0() {
        final def message = 'Oh no, an error!'
        assertEquals message, new SpotCommunicationException(message).message
    }
}
