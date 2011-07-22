package com.interact.listen.conferencing

import org.joda.time.LocalDate
import org.joda.time.LocalTime

class ScheduledConferenceTests extends GroovyTestCase {

    // startsAt() is a combination of date and start time
    void testStartsAt0() {
        final def date = new LocalDate()
        final def starts = new LocalTime()
        final def invite = new ScheduledConference(date: date, starts: starts)
        assertEquals date.toLocalDateTime(starts), invite.startsAt()
    }

    // endsAt() is a combination of date and end time
    void testEndsAt0() {
        final def date = new LocalDate()
        final def ends = new LocalTime()
        final def invite = new ScheduledConference(date: date, ends: ends)
        assertEquals date.toLocalDateTime(ends), invite.endsAt()
    }
}
