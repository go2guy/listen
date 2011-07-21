package com.interact.listen.conferencing

import com.interact.listen.stats.Stat
import org.joda.time.DateTime
import org.joda.time.Period

class ConferenceService {
    def historyService
    def springSecurityService
    def statWriterService

    boolean dropCaller(Participant participant) {
        if(participant.isAdmin) {
            log.warn "Cannot drop admin caller ${participant}"
            return false
        }

        participant.delete()
        historyService.droppedConferenceCaller(participant)
        return true
    }

    boolean muteCaller(Participant participant) {
        if(participant.isAdmin) {
            log.warn "Cannot mute admin caller ${participant}"
            return false
        }

        if(participant.isPassive) {
            log.warn "Cannot mute passive caller ${participant}"
            return false
        }

        participant.isAdminMuted = true
        boolean success = participant.validate() && participant.save()
        if(success) {
            historyService.mutedConferenceCaller(participant)
        }
        return success
    }

    boolean startConference(Conference conference) {
        statWriterService.send(Stat.CONFERENCE_START)
        
        if(conference.isStarted) {
            // TODO warn log
            return true
        }

        conference.isStarted = true
        conference.startTime = new DateTime()

        boolean success = conference.validate() && conference.save()
        historyService.startedConference(conference)
        return success
    }

    boolean startRecordingConference(Conference conference) {
        statWriterService.send(Stat.CONFERENCE_RECORDING_START)

        conference.isRecording = true
        boolean success = conference.validate() && conference.save()
        historyService.startedRecordingConference(conference)
        return success
    }

    boolean stopConference(Conference conference) {
        def seconds = new Period(conference.startTime, new DateTime()).toStandardSeconds().seconds
        statWriterService.send(Stat.CONFERENCE_LENGTH, seconds)

        conference.isStarted = false

        if(conference.isRecording) {
            statWriterService.send(Stat.CONFERENCE_RECORDING_STOP)
            conference.isRecording = false
            conference.recordingSessionId = null
            historyService.stoppedRecordingConference(conference)
        }

        boolean success = conference.validate() && conference.save()
        historyService.stoppedConference(conference)
        return success
    }

    boolean stopRecordingConference(Conference conference) {
        statWriterService.send(Stat.CONFERENCE_RECORDING_STOP)

        conference.isRecording = false
        conference.recordingSessionId = null

        boolean success = conference.validate() && conference.save()
        historyService.stoppedRecordingConference(conference)
        return success
    }

    boolean unmuteCaller(Participant participant) {
        if(participant.isAdmin) {
            log.warn "Cannot unmute admin caller ${participant}"
            return false
        }

        if(participant.isPassive) {
            log.warn "Cannot unmute passive caller ${participant}"
            return false
        }

        participant.isAdminMuted = false
        boolean success = participant.validate() && participant.save()
        if(success) {
            historyService.unmutedConferenceCaller(participant)
        }
        return success
    }
}
