package com.interact.listen.conferencing

import org.joda.time.DateTime
import org.joda.time.Period

class ConferenceService {
    static scope = 'singleton'
    static transactional = true

    def historyService
    def springSecurityService
    def statWriterService

    // TODO remove this class, and just put the logic into the SpotApiController
    // - or, move ALL logic from the API controller into here. just dont separate it out,
    //   its too much work to maintain
    // - ideally, this would be easier to do after changing the SPOT API to have more
    //   specific API calls (e.g. a 'stop conference' 

    boolean dropCaller(Participant participant) {
/*        def user = springSecurityService.getCurrentUser()
        if(participant.conference.owner != user) {
            log.warn "User [${user}] illegally tried to drop caller from conference [${participant.conference}]"
            return false
        }*/

        if(participant.isAdmin) {
//            log.warn "User [${user}] illegally tried to drop admin caller [${participant}]"
            return false
        }

        historyService.droppedConferenceCaller(participant)
        participant.delete()
        return true
    }

    boolean muteCaller(Participant participant) {
/*        def user = springSecurityService.getCurrentUser()
        if(participant.conference.owner != user) {
            log.warn "User [${user}] illegally tried to mute caller from conference [${participant.conference}]"
            return false
        }*/

        if(participant.isAdmin) {
//            log.warn "User [${user}] illegally tried to mute admin caller [${participant}]"
            return false
        }

        if(participant.isPassive) {
//            log.warn "User [${user}] illegally tried to mute passive caller [${participant}]"
            return false
        }

        historyService.mutedConferenceCaller(participant)
        participant.isAdminMuted = true
        return participant.validate() && participant.save()
    }

    boolean startConference(Conference conference) {
        // TODO user validation?
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
/*        def user = springSecurityService.getCurrentUser()
        if(conference.owner != user) {
            log.warn "User [${user}] illegally tried to start recording conference [${conference}]"
            return false
        }*/

        statWriterService.send(Stat.CONFERENCE_RECORDING_START)

        conference.isRecording = true
        boolean success = conference.validate() && conference.save()
        historyService.startedRecordingConference(conference)
        return success
    }

    boolean stopConference(Conference conference) {
        // TODO user validation?
        
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
/*        def user = springSecurityService.getCurrentUser()
        if(conference.owner != user) {
            log.warn "User [${user}] illegally tried to stop recording conference [${conference}]"
            return false
        }*/

        statWriterService.send(Stat.CONFERENCE_RECORDING_STOP)

        conference.isRecording = false
        conference.recordingSessionId = null

        boolean success = conference.validate() && conference.save()
        historyService.stoppedRecordingConference(conference)
        return success
    }

    boolean unmuteCaller(Participant participant) {
/*        def user = springSecurityService.getCurrentUser()
        if(participant.conference.owner != user) {
            log.warn "User [${user}] illegally tried to unmute caller from conference [${participant.conference}]"
            return false
        }*/

        if(participant.isAdmin) {
//            log.warn "User [${user}] illegally tried to unmute admin caller [${participant}]"
            return false
        }

        if(participant.isPassive) {
//            log.warn "User [${user}] illegally tried to unmute passive caller [${participant}]"
            return false
        }

        historyService.unmutedConferenceCaller(participant)
        participant.isAdminMuted = false
        return participant.validate() && participant.save()
    }
}
