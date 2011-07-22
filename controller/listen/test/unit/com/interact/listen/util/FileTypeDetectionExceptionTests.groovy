package com.interact.listen.util

class FileTypeDetectionExceptionTests extends GroovyTestCase {

    // exception constructed with cause sets superclass cause
    void testConstruct0() {
        final def cause = new Throwable()
        assertEquals cause, new FileTypeDetectionException(cause).cause
    }
}
