package com.interact.listen

import org.joda.time.LocalDateTime

class CallData {
    String ani
    String dnis
    LocalDateTime started = new LocalDateTime()
    LocalDateTime ended = null
    String sessionId

    static constraints = {
        ended nullable: true
        sessionId blank: false, unique: true
    }
}
