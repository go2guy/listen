package com.interact.listen.voicemail

class PlaybackOrderTests extends GroovyTestCase {

    // toString() returns nice text
    void testToString0() {
        assertEquals 'Newest to oldest', PlaybackOrder.NEWEST_TO_OLDEST.toString()
        assertEquals 'Oldest to newest', PlaybackOrder.OLDEST_TO_NEWEST.toString()
    }

    // key is the name (for persistence)
    void testGetKey0() {
        assertEquals 'NEWEST_TO_OLDEST', PlaybackOrder.NEWEST_TO_OLDEST.key
        assertEquals 'OLDEST_TO_NEWEST', PlaybackOrder.OLDEST_TO_NEWEST.key
    }
}
