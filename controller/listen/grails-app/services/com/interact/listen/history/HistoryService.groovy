package com.interact.listen.history

import com.interact.listen.*
import com.interact.listen.conferencing.*
import com.interact.listen.voicemail.*
import com.interact.listen.pbx.findme.*
import java.text.SimpleDateFormat
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.springframework.web.context.request.RequestContextHolder as RCH

class HistoryService {
    static scope = 'singleton'
    static transactional = true

    def springSecurityService // injected
    // TODO handle service (if necessary)

    def changedAlternatePagerNumber(def newNumber) {
        // TODO later
    }

    void changedVoicemailPin(User onUser, def oldPin, def newPin) {
        def properties = [
            action: Action.CHANGED_VOICEMAIL_PIN,
            description: "Changed [${onUser.username}]'s voicemail PIN from [${oldPin}] to [${newPin}]",
            onUser: onUser
        ]
        write(new ActionHistory(properties))
     }

    void createdUser(User user) {
        def properties = [
            action: Action.CREATED_USER,
            description: "Created user [${user.username}]",
            onUser: user
        ]
        write(new ActionHistory(properties))
    }

    void deletedFindMeNumber(FindMeNumber findMeNumber) {
        def properties = [
            action: Action.DELETED_FINDMENUMBER,
            description: "Deleted findme number [${findMeNumber.number}]",
            onUser: findMeNumber.user
        ]
        write(new ActionHistory(properties))
    }

    def deletedUser(User user) {
        // TODO?
    }

    def deletedVoicemail(Voicemail voicemail) {
        def properties = [
            action: Action.DELETED_VOICEMAIL,
            description: "Deleted ${friendlyVoicemailSnippet(voicemail)}",
            onUser: voicemail.owner
        ]
        write(new ActionHistory(properties))
    }

    def downloadedVoicemail(Voicemail voicemail) {
        def properties = [
            action: Action.DOWNLOADED_VOICEMAIL,
            description: "Downloaded ${friendlyVoicemailSnippet(voicemail)}",
            onUser: voicemail.owner
        ]
        write(new ActionHistory(properties))
    }

    def droppedConferenceCaller(Participant participant) {
        def properties = [
            action: Action.DROPPED_CONFERENCE_CALLER,
            description: "Dropped caller [${participant.displayName()}] from conference [${participant.conference.description}]"
        ]
        write(new ActionHistory(properties))
    }

    def forwardedVoicemail(Voicemail voicemail) {
        def properties = [
            action: Action.FORWARDED_VOICEMAIL,
            description: "Forwarded ${friendlyVoicemailSnippet(voicemail)}",
            onUser: voicemail.owner
        ]
        write(new ActionHistory(properties))
    }

    def leftVoicemail(Voicemail voicemail) {
        def properties = [
            action: Action.LEFT_VOICEMAIL,
            description: "${voicemail.from()} left voicemail for [${voicemail.owner.username}]",
            onUser: voicemail.owner
        ]
        write(new ActionHistory(properties))
    }

    def listenedToVoicemail(Voicemail voicemail) {
        // TODO?
    }

    def loggedIn(User user) {
        def properties = [
            action: Action.LOGGED_IN,
            description: "Logged into GUI (Local Account)",
            onUser: user
        ]
        write(new ActionHistory(properties))
    }

    def loggedOut(User user) {
        def properties = [
            action: Action.LOGGED_OUT,
            description: "Logged out of GUI",
            onUser: user
        ]
        write(new ActionHistory(properties))
    }

    def mutedConferenceCaller(Participant participant) {
        def properties = [
            action: Action.MUTED_CONFERENCE_CALLER,
            description: "Muted caller [${participant.displayName()}] in conference [${participant.conference.description}]"
        ]
        write(new ActionHistory(properties))
    }

    def sentVoicemailAlternatePage(Voicemail voicemail) {
        // TODO later
    }

    def sentNewVoicemailEmail(Voicemail voicemail) {
        def preferences = VoicemailPreferences.findByUser(voicemail.owner)
        def properties = [
            action: Action.SENT_NEW_VOICEMAIL_EMAIL,
            description: "Sent email to [${preferences.emailNotificationAddress}] for ${friendlyVoicemailSnippet(voicemail)}",
            onUser: voicemail.owner
        ]
        write(new ActionHistory(properties))
    }

    def sentNewVoicemailSms(Voicemail voicemail) {
        def preferences = VoicemailPreferences.findByUser(voicemail.owner)
        def properties = [
            action: Action.SENT_NEW_VOICEMAIL_SMS,
            description: "Sent SMS to [${preferences.smsNotificationAddress}] for ${friendlyVoicemailSnippet(voicemail)}",
            onUser: voicemail.owner
        ]
        write(new ActionHistory(properties))
    }

    def startedConference(Conference conference) {
        def properties = [
            action: Action.STARTED_CONFERENCE,
            description: "Started conference [${conference.description}]"
        ]
        write(new ActionHistory(properties))
    }

    def startedRecordingConference(Conference conference) {
        def properties = [
            action: Action.STARTED_RECORDING_CONFERENCE,
            description: "Started recording conference [${conference.description}]"
        ]
        write(new ActionHistory(properties))
    }

    def stoppedConference(Conference conference) {
        def properties = [
            action: Action.STOPPED_CONFERENCE,
            description: "Stopped conference [${conference.description}]"
        ]
        write(new ActionHistory(properties))
    }

    def stoppedRecordingConference(Conference conference) {
        def properties = [
            action: Action.STOPPED_RECORDING_CONFERENCE,
            description: "Stopped recording conference [${conference.description}]"
        ]
        write(new ActionHistory(properties))
    }

    def unmutedConferenceCaller(Participant participant) {
        def properties = [
            action: Action.UNMUTED_CONFERENCE_CALLER,
            description: "Unmuted caller [${participant.displayName()}] in conference [${participant.conference.description}]"
        ]
        write(new ActionHistory(properties))
    }

    void updatedFindMeNumber(FindMeNumber findMeNumber) {
        def properties
        if(findMeNumber.isEnabled) {
            properties = [
                action: Action.UPDATED_FINDMENUMBER,
                description: "Enabled findme number [${findMeNumber.number}]",
                onUser: findMeNumber.user
            ]
        }
        else {
            properties = [
                action: Action.UPDATED_FINDMENUMBER,
                description: "Disabled findme number [${findMeNumber.number}]",
                onUser: findMeNumber.user
            ]
        }
        write(new ActionHistory(properties))
    }

    private void write(ActionHistory history) {
        history.byUser = currentUser()
        history.organization = history.byUser?.organization ?: history.onUser?.organization
        history.channel = currentChannel()
        if(!(history.validate() && history.save())) {
            log.error('Unable to save ActionHistory: ' + history.errors.allErrors)
        }
    }

    private def currentUser() {
        try {
            def user = springSecurityService.getCurrentUser()
            if(user) {
                return user
            }
        } catch(MissingPropertyException e) {
            log.warn 'Error retrieving spring security user for history [MissingPropertyException (principal was probably a String, not a User)]'
            log.debug e
        }

        def request = RCH.requestAttributes?.currentRequest
        if(request) {
            log.debug "Returning request attribute user for history [${request.getAttribute('tui-user')}]"
            return request.getAttribute('tui-user')
        }

        log.debug "No request available, returning null for current user"
        return null
    }

    private def currentChannel() {
        def request = RCH.requestAttributes?.currentRequest
        return request?.getAttribute('tui-channel') ?: Channel.GUI
    }

    private def formatDate(DateTime date) {
        def formatter = DateTimeFormat.forPattern('yyyy-MM-dd HH:mm')
        return formatter.print(date)
    }

    private def friendlyVoicemailSnippet(Voicemail voicemail) {
        return "voicemail for [${voicemail.owner.realName}] from [${voicemail.from()}] left on [${formatDate(voicemail.dateCreated)}]"
    }
}
