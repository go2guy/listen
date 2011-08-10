package com.interact.listen

import grails.test.TagLibUnitTestCase
import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException
import org.joda.time.Duration

class DateTimeTagLibTests extends TagLibUnitTestCase {
    DateTimeTagLibTests() {
        super(DateTimeTagLib)
    }

    // null duration throws error
    void testPrettyduration0() {
        try {
            tagLib.prettyduration(duration: null)
        } catch(GrailsTagException e) {
            assertEquals 'Tag [prettyduration] is missing required attribute [duration]', e.message
        }
    }

    // duration with hours
    void testPrettyduration1() {
        final def duration = new Duration((1000 * 60 * 60 * 5) + (1000 * 60 * 6) + (1000 * 7))
        tagLib.prettyduration(duration: duration)
        assertEquals '5h 6m 7s', tagLib.out.toString()
    }

    // duration with minutes
    void testPrettyduration2() {
        final def duration = new Duration(1000 * 60 * 34)
        tagLib.prettyduration(duration: duration)
        // TODO fix the trailing space
        assertEquals '34m ', tagLib.out.toString()
    }

    // duration with seconds
    void testPrettyduration3() {
        final def duration = new Duration(1000 * 22)
        tagLib.prettyduration(duration: duration)
        assertEquals '22s', tagLib.out.toString()
    }

    // duration with 0 seconds
    void testPrettyduration4() {
        final def duration = new Duration(0)
        tagLib.prettyduration(duration: duration)
        assertEquals '0s', tagLib.out.toString()
    }

    // null duration throws error
    void testFormatduration0() {
        try {
            tagLib.formatduration(duration: null)
        } catch(GrailsTagException e) {
            assertEquals 'Tag [formatduration] is missing required attribute [duration]', e.message
        }
    }
    
    // duration with hours, millis
    void testFormatduration1() {
        final def duration = new Duration((1000 * 60 * 60 * 5) + (1000 * 60 * 6) + (1000 * 7) + 432)
        tagLib.formatduration(duration: duration, millis: true)
        assertEquals '5:06:07.432', tagLib.out.toString()
    }

    // duration with minutes, millis
    void testFormatduration2() {
        final def duration = new Duration(1000 * 60 * 34)
        tagLib.formatduration(duration: duration, millis: true)
        assertEquals '34:00.000', tagLib.out.toString()
    }

    // duration with < 60 seconds, millis
    void testFormatduration3() {
        final def duration = new Duration(1000 * 32)
        tagLib.formatduration(duration: duration, millis: true)
        assertEquals '0:32.000', tagLib.out.toString()
    }

    // duration with < 10 seconds, no millis
    void testFormatduration4() {
        final def duration = new Duration(1000 * 9)
        tagLib.formatduration(duration: duration, millis: false)
        assertEquals '0:09', tagLib.out.toString()
    }

    // duration > 100 hours, millis
    void testFormatduration5() {
        final def duration = new Duration((1000 * 60 * 60 * 123) + (1000 * 60 * 55) + 321)
        tagLib.formatduration(duration: duration, millis: true)
        assertEquals '123:55:00.321', tagLib.out.toString()
    }
}
