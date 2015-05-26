package com.interact.listen.conferencing

import com.interact.listen.User
import org.joda.time.DateTime

class Conference {
    String arcadeId
    String description
    boolean isRecording = false
    boolean isStarted = false
    String recordingSessionId
    DateTime startTime
    User owner

    static hasMany = [participants: Participant, pins: Pin]

    static constraints = {
        arcadeId nullable: true
        description blank: false, maxSize: 100
        isRecording validator: { val, obj ->
            return (!obj.isStarted && val ? ['recording.while.not.started'] : true)
        }
        recordingSessionId nullable: true
        startTime nullable: true
    }

    def findAdmins() {
        def admins = Participant.findAllByConferenceAndIsAdmin(this, true)
        return admins
    }

    def firstAdminSessionId() {
        def sessionId = Participant.findByConferenceAndIsAdmin(this, true)?.sessionId
        if(!sessionId) {
            throw new IllegalStateException('Could not find admin participant for conference')
        }
        return sessionId
    }
}
