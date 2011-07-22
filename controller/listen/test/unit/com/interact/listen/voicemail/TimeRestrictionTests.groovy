package com.interact.listen.voicemail

import org.joda.time.DateTimeConstants
import org.joda.time.LocalTime

class TimeRestrictionTests extends GroovyTestCase {

    // restriction applying to monday returns true for monday query
    void testAppliesToJodaDayOfWeek0() {
        final def restriction = new TimeRestriction(monday: true)
        assertTrue restriction.appliesToJodaDayOfWeek(DateTimeConstants.MONDAY)
    }

    // restriction not applying to monday returns false for monday query
    void testAppliesToJodaDayOfWeek1() {
        final def restriction = new TimeRestriction(monday: false)
        assertFalse restriction.appliesToJodaDayOfWeek(DateTimeConstants.MONDAY)
    }

    // restriction with invalid constant returns false
    void testAppliesToJodaDayOfWeek2() {
        final def restriction = new TimeRestriction()
        final def invalidConstant = 1234
        assertFalse restriction.appliesToJodaDayOfWeek(invalidConstant)
    }

    // restriction for the current time and all days returns true
    void testAppliesToNow0() {
        final def thirtyMinutesAgo = new LocalTime().minusMinutes(30)
        final def thirtyMinutesFromNow = new LocalTime().plusMinutes(30)

        final def restriction = new TimeRestriction(monday: true,
                                                    tuesday: true,
                                                    wednesday: true,
                                                    thursday: true,
                                                    friday: true,
                                                    saturday: true,
                                                    sunday: true,
                                                    startTime: thirtyMinutesAgo,
                                                    endTime: thirtyMinutesFromNow)
        assertTrue restriction.appliesToNow()
    }
}
