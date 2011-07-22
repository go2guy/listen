package com.interact.listen

class AudioTests extends GroovyTestCase {

    // file with .wav extension is returned with a .mp3 extension instead
    void testMp3File0() {
        final def uriString = 'file:/tmp/foo.wav'
        final def file = new File(new URI(uriString))
        final def audio = new Audio(file: file)

        assertEquals 'file:/tmp/foo.mp3', audio.mp3File().toURI().toString()
    }
}
