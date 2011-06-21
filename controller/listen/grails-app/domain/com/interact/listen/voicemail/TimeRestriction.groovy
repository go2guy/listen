package com.interact.listen.voicemail

import org.joda.time.DateTimeConstants
import org.joda.time.LocalDateTime
import org.joda.time.LocalTime

// TODO this class isnt great. an ideal solution would be to use
// some sort of temporal expression engine to represent times and recurrence.
// this is a short-term hacky solution, and should be refactored when the
// opportunity arises.

// most of the code here was copied straight from the original Listen source.
class TimeRestriction {
    LocalTime endTime
    LocalTime startTime

    boolean monday = false
    boolean tuesday = false
    boolean wednesday = false
    boolean thursday = false
    boolean friday = false
    boolean saturday = false
    boolean sunday = false

    static constraints = {
        startTime validator: { val, obj ->
            return val.isBefore(obj.endTime)
        }
    }

    static belongsTo = [VoicemailPreferences]

    public enum DayOfWeek {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }

    private enum Meridiem {
        AM, PM, NONE;
    }

    public boolean appliesToNow() {
        def now = new LocalDateTime();
        if(!appliesToJodaDayOfWeek(now.dayOfWeek)) {
            return false
        }

        def start = new LocalTime(startTime.hourOfDay, startTime.minuteOfHour, 0, 0)
        def end = new LocalTime(endTime.hourOfDay, endTime.minuteOfHour, 59, 999)
        def time = now.toLocalTime()

        if(time == start || time == end || (time.isAfter(start) && time.isBefore(end))) {
            return true
        }

        return false
    }

    public boolean appliesToJodaDayOfWeek(int jodaDayOfWeek)
    {
        switch(jodaDayOfWeek)
        {
            case DateTimeConstants.MONDAY:
                return monday;
            case DateTimeConstants.TUESDAY:
                return tuesday;
            case DateTimeConstants.WEDNESDAY:
                return wednesday;
            case DateTimeConstants.THURSDAY:
                return thursday;
            case DateTimeConstants.FRIDAY:
                return friday;
            case DateTimeConstants.SATURDAY:
                return saturday;
            case DateTimeConstants.SUNDAY:
                return sunday;
            default:
                return false;
        }
    }
}
