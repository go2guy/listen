package com.interact.listen.conferencing

import com.interact.listen.User
import org.joda.time.*

class ScheduledConference {
    String activeCallerAddresses
    Conference forConference
    LocalDate date
    DateTime dateCreated
    String emailBody
    String emailSubject
    LocalTime ends
    String passiveCallerAddresses
    User scheduledBy
    LocalTime starts
    String uid
    int sequence = 0

    static constraints = {
        activeCallerAddresses nullable: true, blank: true, maxSize: 2048
        emailBody maxSize: 2048
        emailSubject blank: false
        ends validator: { val, obj ->
            if(obj.starts && (val.isAfter(obj.starts) || val == obj.starts)) {
                return true
            }
            return 'before.starts'
        }
        passiveCallerAddresses nullable: true, blank: true, maxSize: 2048, validator: {val, obj ->
            if(obj.activeCallerAddresses == null && val == null){
                return ['notAllNulls']
            }
        }

        uid nullable: true
    }

    LocalDateTime startsAt() {
        date.toLocalDateTime(starts)
    }

    LocalDateTime endsAt() {
        date.toLocalDateTime(ends)
    }
  
    Duration duration() {
        new Period(starts, ends).toStandardDuration()
    }

    int invitedCount() {
        activeCallers().size() + passiveCallers().size()
    }

    Set activeCallers(boolean includeOrganizer = true) {
        stringToSet(activeCallerAddresses, includeOrganizer)
    }

    Set passiveCallers(boolean includeOrganizer = true) { 
        stringToSet(passiveCallerAddresses, includeOrganizer)
    }

    def isPast() {
        startsAt().isBefore(new LocalDateTime())
    }

    private Set stringToSet(def commaDelimited, boolean includeOrganizer = true) {
        def s = [] as Set
        commaDelimited?.split(/[,\s]/).each {
            def v = it.trim()
            if(v.length() > 0) {
                if(includeOrganizer || v != scheduledBy.emailAddress) {
                    s << v
                }
            }
        }
        return s
    }
}
