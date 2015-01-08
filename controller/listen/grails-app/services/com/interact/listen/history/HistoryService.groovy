package com.interact.listen.history

import com.interact.listen.*
import com.interact.listen.attendant.*
import com.interact.listen.conferencing.*
import com.interact.listen.fax.*
import com.interact.listen.acd.*
import com.interact.listen.pbx.*
import com.interact.listen.pbx.findme.*
import com.interact.listen.voicemail.*
import com.interact.listen.voicemail.afterhours.*
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.springframework.web.context.request.RequestContextHolder as RCH

class HistoryService {
    def springSecurityService
    // TODO handle service (if necessary)

    void cancelledConferenceInvitation(ScheduledConference invitation) {
        write(action: Action.CANCELLED_CONFERENCE_INVITATION,
              description: "Cancelled conference invitation for [${invitation.forConference.description}] starting [${formatDateTime(invitation.startsAt())}]",
              onUser: invitation.scheduledBy)
    }

    void changedAccountEmailAddress(User onUser, def from) {
        write(action: Action.CHANGED_ACCOUNT_EMAIL_ADDRESS,
              description: "Changed account email address from [${from}] to [${onUser.emailAddress}]",
              onUser: onUser)
    }

    void changedAccountName(User onUser, def from) {
        write(action: Action.CHANGED_ACCOUNT_NAME,
              description: "Changed account name from [${from}] to [${onUser.realName}]",
              onUser: onUser)
    }

    void changedAccountPassword(User onUser) {
        write(action: Action.CHANGED_ACCOUNT_PASSWORD,
              description: "Changed account password",
              onUser: onUser)
    }

    void changedAccountUsername(User onUser, def from) {
        write(action: Action.CHANGED_ACCOUNT_USERNAME,
              description: "Changed account username from [${from}] to [${onUser.username}]",
              onUser: onUser)
    }

    void changedAfterHoursAlternateNumber(AfterHoursConfiguration configuration, def from) {
        write(action: Action.CHANGED_AFTER_HOURS_ALTERNATE_NUMBER,
              description: "Changed after hours alternate number from [${from}] to [${configuration.alternateNumber}]")
    }

    void changedAfterHoursMobileNumber(AfterHoursConfiguration configuration, MobilePhone from) {
        write(action: Action.CHANGED_AFTER_HOURS_MOBILE_NUMBER,
              description: "Changed after hours mobile number from [${from?.asSmsEmail() ?: 'None'}] to [${configuration.mobilePhone?.asSmsEmail() ?: 'None'}]")
    }

    void changedConferenceInvitation(ScheduledConference invitation) {
        write(action: Action.CHANGED_CONFERENCE_INVITATION,
              description: "Changed conference invitation for [${invitation.forConference.description}] starting [${formatDateTime(invitation.startsAt())}]",
              onUser: invitation.scheduledBy)
    }

    void changedFindMeExpiration(FindMePreferences preferences) {
        write(action: Action.CHANGED_FIND_ME_EXPIRATION,
              description: "Changed Find Me expiration to [${formatDateTime(preferences.expires)}]",
              onUser: preferences.user)
    }

    void changedFindMeExpirationReminderSmsNumber(FindMePreferences preferences, def from) {
        write(action: Action.CHANGED_FIND_ME_EXPIRATION_REMINDER_SMS_NUMBER,
              description: "Changed Find Me expiration reminder SMS number from [${from}] to [${preferences.reminderNumber}]",
              onUser: preferences.user)
    }

    void changedFindMeNumbers(User user) {
        def numbers = FindMeNumber.findAllByUserForHistory(user)
        write(action: Action.CHANGED_FIND_ME_NUMBERS,
              description: "Changed Find Me numbers to ${numbers}",
              onUser: user)
    }

    void changedMobilePhoneVisibility(MobilePhone phone) {
        write(action: Action.CHANGED_MOBILE_PHONE_VISIBILITY,
              description: "Changed mobile phone [${phone.asSmsEmail()}] from [${phone.isPublic ? 'private' : 'public'}] to [${phone.isPublic ? 'public' : 'private'}]",
              onUser: phone.owner)
    }

    void changedNewConferencePinLength(ConferencingConfiguration configuration, def from) {
        write(action: Action.CHANGED_NEW_CONFERENCE_PIN_LENGTH,
              description: "Changed new conference PIN length from [${from}] to [${configuration.pinLength}]")
    }

    void changedOrganizationExtLength(Organization organization, def from) {
        write(action: Action.CHANGED_ORGANIZATION_EXT_LENGTH,
                description: "Changed organization extension length from [${from}] to [${organization.extLength}]",
                onOrganization: organization)
    }

    void changedNewVoicemailEmailAddress(VoicemailPreferences preferences, def from) {
        write(action: Action.CHANGED_NEW_VOICEMAIL_EMAIL_ADDRESS,
              description: "Changed new voicemail email address from [${from}] to [${preferences.emailNotificationAddress}]",
              onUser: preferences.user)
    }

    void changedNewVoicemailSmsNumber(VoicemailPreferences preferences, def from) {
        write(action: Action.CHANGED_NEW_VOICEMAIL_SMS_NUMBER,
              description: "Changed new voicemail SMS number from [${from}] to [${preferences.smsNotificationAddress}]",
              onUser: preferences.user)
    }

    void changedOtherPhoneVisibility(OtherPhone phone) {
        write(action: Action.CHANGED_OTHER_PHONE_VISIBILITY,
              description: "Changed other phone [${phone.number}] from [${phone.isPublic ? 'private' : 'public'}] to [${phone.isPublic ? 'public' : 'private'}]",
              onUser: phone.owner)
    }

    void changedRealizeConfiguration(AfterHoursConfiguration configuration) {
        write(action: Action.CHANGED_REALIZE_CONFIGURATION,
              description: "Changed Realize configuration to URL [${configuration.realizeUrl}], alert name [${configuration.realizeAlertName}]")
    }

    void changedVoicemailPin(User onUser, def oldPin, def newPin) {
        write(action: Action.CHANGED_VOICEMAIL_PIN,
//              description: "Changed [${onUser.username}]'s voicemail PIN from [${oldPin}] to [${newPin}]",
              description: 'Changed voicemail PIN',
              onUser: onUser)
    }

    void createdAttendantHoliday(PromptOverride promptOverride)
    {
        write(action: Action.CREATED_ATTENDANT_HOLIDAY,
              description: "Created attendant holiday starting [${formatDateTime(promptOverride.startDate)}], ending[${formatDateTime(promptOverride.endDate)} with prompt [${promptOverride.optionsPrompt}]")

    }

    void createdConferenceInvitation(ScheduledConference invitation) {
        write(action: Action.CREATED_CONFERENCE_INVITATION,
              description: "Created conference invitation for [${invitation.forConference.description}] starting [${formatDateTime(invitation.startsAt())}]",
              onUser: invitation.scheduledBy)
    }

    void createdDirectInwardDialNumber(DirectInwardDialNumber number) {
        write(action: Action.CREATED_DIRECT_INWARD_DIAL_NUMBER,
              description: "Created direct inward dial number [${number.number}]",
              onUser: number.owner)
    }
    
    void createdDirectMessageNumber(DirectMessageNumber number) {
        write(action: Action.CREATED_DIRECT_MESSAGE_NUMBER,
              description: "Created direct message number [${number.number}]",
              onUser: number.owner)
    }

    void createdExtension(Extension extension) {
        write(action: Action.CREATED_EXTENSION,
              description: "Created extension [${extension.number}] with user name [${extension.sipPhone.username}]",
              onUser: extension.owner)
    }

    void createdMobilePhone(MobilePhone phone) {
        write(action: Action.CREATED_MOBILE_PHONE,
              description: "Created [${phone.isPublic ? 'public' : 'private'}] mobile phone [${phone.asSmsEmail()}]",
              onUser: phone.owner)
    }

    void createdOtherPhone(OtherPhone phone) {
        write(action: Action.CREATED_OTHER_PHONE,
              description: "Created [${phone.isPublic ? 'public' : 'private'}] other phone [${phone.number}]",
              onUser: phone.owner)
    }

    void createdOutdialRestriction(OutdialRestriction restriction) {
        write(action: Action.CREATED_OUTDIAL_RESTRICTION,
              description: "Restricted outdialing [${restriction.pattern}] for [${restriction.target?.realName ?: 'Everyone'}]",
              onUser: restriction.target)
    }

    void createdOutdialRestrictionException(OutdialRestrictionException exception) {
        write(action: Action.CREATED_OUTDIAL_RESTRICTION_EXCEPTION,
              description: "Created outdialing restriction exception to pattern [${exception.restriction.pattern}] for [${exception.target.realName}]",
              onUser: exception.target)
    }

    void createdRoute(NumberRoute route) {
        boolean internal = route.type == NumberRoute.Type.INTERNAL
        write(action: internal ? Action.CREATED_INTERNAL_ROUTE : Action.CREATED_EXTERNAL_ROUTE,
              description: "Routed (${internal ? 'internal' : 'external'}) [${route.pattern}] to [${route.destination}]")
    }

    void createdUser(User user) {
        write(action: Action.CREATED_USER,
              description: "Created user [${user.username}]",
              onUser: user)
    }

    void deletedAttendantHoliday(PromptOverride promptOverride)
    {
        write(action: Action.DELETED_ATTENDANT_HOLIDAY,
              description: "Deleted attendant holiday starting [${formatDateTime(promptOverride.startDate)}], ending [${formatDateTime(promptOverride.endDate)}] with prompt [${promptOverride.optionsPrompt}]")
    }

    void deletedConferenceRecording(Recording recording) {
        write(action: Action.DELETED_CONFERENCE_RECORDING,
              description: "Deleted conference recording created on [${formatDateTime(recording.audio.dateCreated)}] for conference [${recording.conference?.description}]",
              onUser: recording.conference.owner)
    }

    void deletedDirectInwardDialNumber(def number) {
        write(action: Action.DELETED_DIRECT_INWARD_DIAL_NUMBER,
              description: "Deleted direct inward dial number [${number.number}]",
              onUser: number.owner)
    }
    
    void deletedDirectMessageNumber(def number) {
        write(action: Action.DELETED_DIRECT_MESSAGE_NUMBER,
              description: "Deleted direct message number [${number.number}]",
              onUser: number.owner)
    }

    void deletedExtension(def extension) {
        write(action: Action.DELETED_EXTENSION,
              description: "Deleted extension [${extension.number}]",
              onUser: extension.owner)
    }

    void deletedFax(Fax fax) {
        write(action: Action.DELETED_FAX,
              description: "Deleted ${friendlyMessageSnippet(fax)}",
              onUser: fax.owner)
    }

    void deletedMobilePhone(def phone) {
        write(action: Action.DELETED_MOBILE_PHONE,
              description: "Deleted mobile phone [${phone.asSmsEmail()}]",
              onUser: phone.owner)
    }

    void deletedOtherPhone(def phone) {
        write(action: Action.DELETED_OTHER_PHONE,
              description: "Deleted other phone [${phone.number}]",
              onUser: phone.owner)
    }

    void deletedOutdialRestriction(def restriction) {
        write(action: Action.DELETED_OUTDIAL_RESTRICTION,
              description: "Deleted outdialing restriction [${restriction.pattern}] for [${restriction.target?.realName ?: 'Everyone'}]",
              onUser: restriction.target)
    }

    void deletedOutdialRestrictionException(def exception) {
        write(action: Action.DELETED_OUTDIAL_RESTRICTION_EXCEPTION,
              description: "Deleted outdialing restriction exception to pattern [${exception.restriction.pattern}] for [${exception.target.realName}]",
              onUser: exception.target)
    }

    void deletedRoute(def route) {
        boolean internal = route.type == NumberRoute.Type.INTERNAL
        write(action: internal ? Action.DELETED_INTERNAL_ROUTE : Action.DELETED_EXTERNAL_ROUTE,
              description: "Deleted (${internal ? 'internal' : 'external'}) route from [${route.pattern}] to [${route.destination}]")
    }

    void deletedVoicemail(Voicemail voicemail) {
        write(action: Action.DELETED_VOICEMAIL,
              description: "Deleted ${friendlyMessageSnippet(voicemail)}",
              onUser: voicemail.owner)
    }

    void addedUserSkill(UserSkill userSkill) {
        write(action: Action.ADDED_ACD_SKILL_USER,
              description: "Add ACD skill [${userSkill.skill.skillname}]",
              onUser: userSkill.user )
    }
    
    void deletedUserSkill(UserSkill userSkill) {
        write(action: Action.DELETED_ACD_SKILL_USER,
              description: "Removed ACD skill [${userSkill.skill.skillname}]",
              onUser: userSkill.user )
    }
    
    void createdSkill(Skill skill) {
        write(action: Action.CREATED_ACD_SKILL,
              description: "Created ACD skill [${skill.skillname}] for org [${skill.organization}]")
    }

    void updatedSkill(Skill skill) {
        write(action: Action.UPDATED_ACD_SKILL,
              description: "Updated ACD skill [${skill.skillname}] for org [${skill.organization}]")
    }
        
    void deletedSkill(Skill skill) {
        write(action: Action.DELETED_ACD_SKILL,
              description: "Deleted ACD skill [${skill.skillname}] for org [${skill.organization}]")
    }
    
    void toggleACDStatus(AcdUserStatus acdUserStatus) {
        write(action: Action.UPDATED_ACD_STATUS,
              description: "Updated ACD status to [${acdUserStatus.acdQueueStatus.toString()}]",
              onUser: acdUserStatus.owner )
    }
        
    void updatedACDContactNumber(AcdUserStatus acdUserStatus) {
        write(action: Action.UPDATED_ACD_CONTACTNUMBER,
              description: "Updated ACD contact number to [${acdUserStatus.contactNumber.number}]",
              onUser: acdUserStatus.owner )
    }
    
    void disabledAndroidCloudToDevice() {
        write(action: Action.DISABLED_ANDROID_CLOUD_TO_DEVICE,
              description: "Disabled Android Cloud-to-Device")
    }

    void disabledFindMeExpirationReminderSms(FindMePreferences preferences) {
        write(action: Action.DISABLED_FIND_ME_EXPIRATION_REMINDER_SMS,
              description: "Disabled Find Me expiration reminder SMS",
              onUser: preferences.user)
    }

    void disabledRecurringVoicemailSms(VoicemailPreferences preferences) {
        write(action: Action.DISABLED_RECURRING_VOICEMAIL_SMS,
              description: "Disabled recurring voicemail SMS",
              onUser: preferences.user)
    }

    void disabledNewVoicemailEmail(VoicemailPreferences preferences) {
        write(action: Action.DISABLED_NEW_VOICEMAIL_EMAIL,
              description: "Disabled new voicemail email",
              onUser: preferences.user)
    }

    void disabledNewVoicemailSms(VoicemailPreferences preferences) {
        write(action: Action.DISABLED_NEW_VOICEMAIL_SMS,
              description: "Disabled new voicemail SMS",
              onUser: preferences.user)
    }

    void disabledTranscription() {
        write(action: Action.DISABLED_TRANSCRIPTION,
              description: "Disabled transcription")
    }

    void disabledUser(User user) {
        write(action: Action.DISABLED_USER,
              description: "Disabled user [${user.username}]",
              onUser: user)
    }

    void downloadedConferenceRecording(Recording recording) {
        write(action: Action.DOWNLOADED_CONFERENCE_RECORDING,
              description: "Downloaded conference recording created on [${formatDateTime(recording.audio.dateCreated)}] for conference [${recording.conference?.description}]",
              onUser: recording.conference.owner)
    }

    void downloadedFax(Fax fax) {
        write(action: Action.DOWNLOADED_FAX,
              description: "Downloaded ${friendlyMessageSnippet(fax)}",
              onUser: fax.owner)
    }

    void downloadedVoicemail(Voicemail voicemail) {
        write(action: Action.DOWNLOADED_VOICEMAIL,
              description: "Downloaded ${friendlyMessageSnippet(voicemail)}",
              onUser: voicemail.owner)
    }

    void droppedConferenceCaller(Participant participant) {
        write(action: Action.DROPPED_CONFERENCE_CALLER,
              description: "Dropped caller [${participant.displayName()}] from conference [${participant.conference.description}]")
    }

    void enabledAndroidCloudToDevice() {
        write(action: Action.ENABLED_ANDROID_CLOUD_TO_DEVICE,
              description: "Enabled Android Cloud-to-Device")
    }

    void enabledFindMeExpirationReminderSms(FindMePreferences preferences) {
        write(action: Action.ENABLED_FIND_ME_EXPIRATION_REMINDER_SMS,
              description: "Enabled Find Me expiration reminder SMS to number [${preferences.reminderNumber}]",
              onUser: preferences.user)
    }

    void enabledRecurringVoicemailSms(VoicemailPreferences preferences) {
        write(action: Action.ENABLED_RECURRING_VOICEMAIL_SMS,
              description: "Enabled recurring voicemail SMS",
              onUser: preferences.user)
    }

    void enabledNewVoicemailEmail(VoicemailPreferences preferences) {
        write(action: Action.ENABLED_NEW_VOICEMAIL_EMAIL,
              description: "Enabled new voicemail email to address [${preferences.emailNotificationAddress}]",
              onUser: preferences.user)
    }

    void enabledNewVoicemailSms(VoicemailPreferences preferences) {
        write(action: Action.ENABLED_NEW_VOICEMAIL_SMS,
              description: "Enabled new voicemail SMS to number [${preferences.smsNotificationAddress}]",
              onUser: preferences.user)
    }

    void enabledTranscription(TranscriptionConfiguration configuration) {
        write(action: Action.ENABLED_TRANSCRIPTION,
              description: "Enabled transcription using phone number [${configuration.phoneNumber}]")
    }

    void enabledUser(User user) {
        write(action: Action.ENABLED_USER,
              description: "Enabled user [${user.username}]",
              onUser: user)
    }

    void forwardedExtension(Extension extension) {
        write(action: Action.FORWARDED_EXTENSION,
              description: "Forwarded extension [${extension.number}] to [${extension.forwardedTo}]",
              onUsr: extension.owner)
    }

    void forwardedVoicemail(Voicemail voicemail) {
        write(action: Action.FORWARDED_VOICEMAIL,
              description: "Forwarded ${friendlyMessageSnippet(voicemail)}",
              onUser: voicemail.owner)
    }

    void joinedConference(Participant participant) {
        write(action: Action.JOINED_CONFERENCE,
              description: "[${participant.displayName()}] joined conference [${participant.conference.description}]")
    }

    void leftFax(Fax fax) {
        write(action: Action.LEFT_FAX,
              description: "${fax.from()} left fax for [${fax.owner.username}]",
              onUser: fax.owner)
    }

    void leftVoicemail(Voicemail voicemail) {
        write(action: Action.LEFT_VOICEMAIL,
              description: "${voicemail.from()} left voicemail for [${voicemail.owner.username}]",
              byUser: voicemail.leftBy,
              onUser: voicemail.owner)
    }

    void loggedIn(User user) {
        write(action: Action.LOGGED_IN,
              description: "Logged into GUI (Local Account)",
              onUser: user)
    }

    void loggedOut(User user) {
        write(action: Action.LOGGED_OUT,
              description: "Logged out of GUI",
              onUser: user)
    }

    void markedFaxNew(Fax fax) {
        write(action: Action.MARKED_FAX_NEW,
              description: "Marked ${friendlyMessageSnippet(fax)} as New",
              onUser: fax.owner)
    }

    void markedFaxOld(Fax fax) {
        write(action: Action.MARKED_FAX_OLD,
              description: "Marked ${friendlyMessageSnippet(fax)} as Old",
              onUser: fax.owner)
    }

    void markedVoicemailNew(Voicemail voicemail) {
        write(action: Action.MARKED_VOICEMAIL_NEW,
              description: "Marked ${friendlyMessageSnippet(voicemail)} as New",
              onUser: voicemail.owner)
    }

    void markedVoicemailOld(Voicemail voicemail) {
        write(action: Action.MARKED_VOICEMAIL_OLD,
              description: "Marked ${friendlyMessageSnippet(voicemail)} as Old",
              onUser: voicemail.owner)
    }

    void mutedConferenceCaller(Participant participant) {
        write(action: Action.MUTED_CONFERENCE_CALLER,
              description: "Muted caller [${participant.displayName()}] in conference [${participant.conference.description}]")
    }

    void sentFax(OutgoingFax fax) {
        write(action: Action.SENT_FAX,
              description: "Sent fax to [${fax.dnis}]")
    }

    void sentNewVoicemailEmail(Voicemail voicemail) {
        def preferences = VoicemailPreferences.findByUser(voicemail.owner)
        write(action: Action.SENT_NEW_VOICEMAIL_EMAIL,
              description: "Sent email to [${preferences.emailNotificationAddress}] for ${friendlyMessageSnippet(voicemail)}",
              onUser: voicemail.owner)
    }

    void sentNewVoicemailSms(Voicemail voicemail) {
        def preferences = VoicemailPreferences.findByUser(voicemail.owner)
        write(action: Action.SENT_NEW_VOICEMAIL_SMS,
              description: "Sent SMS to [${preferences.smsNotificationAddress}] for ${friendlyMessageSnippet(voicemail)}",
              onUser: voicemail.owner)
    }

    void sentNewFaxEmail(Fax fax) {
        def preferences = VoicemailPreferences.findByUser(fax.owner)
        write(action: Action.SENT_NEW_FAX_EMAIL,
              description: "Sent email to [${preferences.emailNotificationAddress}] for ${friendlyMessageSnippet(fax)}",
              onUser: fax.owner)
    }

    void sentNewFaxSms(Fax fax) {
        def preferences = VoicemailPreferences.findByUser(fax.owner)
        write(action: Action.SENT_NEW_FAX_SMS,
              description: "Sent SMS to [${preferences.smsNotificationAddress}] for ${friendlyMessageSnippet(fax)}",
              onUser: fax.owner)
    }

    void startedConference(Conference conference) {
        write(action: Action.STARTED_CONFERENCE,
              description: "Started conference [${conference.description}]")
    }

    void startedRecordingConference(Conference conference) {
        write(action: Action.STARTED_RECORDING_CONFERENCE,
              description: "Started recording conference [${conference.description}]")
    }

    void stoppedConference(Conference conference) {
        write(action: Action.STOPPED_CONFERENCE,
              description: "Stopped conference [${conference.description}]")
    }

    void stoppedRecordingConference(Conference conference) {
        write(action: Action.STOPPED_RECORDING_CONFERENCE,
              description: "Stopped recording conference [${conference.description}]")
    }

    void unforwardedExtension(Extension extension) {
        write(action: Action.UNFORWARDED_EXTENSION,
              description: "Removed forwarding for extension [${extension.number}]",
              onUser: extension.owner)
    }

    void unmutedConferenceCaller(Participant participant) {
        write(action: Action.UNMUTED_CONFERENCE_CALLER,
              description: "Unmuted caller [${participant.displayName()}] in conference [${participant.conference.description}]")
    }

    private void write(Map map)
    {
        try
        {
            def history = new ActionHistory(map)
            history.byUser = map.containsKey('byUser') ? map.byUser : currentUser()
            if (map.containsKey('onOrganization')){
                history.organization = map.onOrganization
            } else {
                history.organization = history.byUser?.organization ?: history.onUser?.organization
            }

            history.channel = currentChannel()
            if(!(history.validate() && history.save())) {
                log.error('Unable to save ActionHistory: ' + history.errors.allErrors)
            }
        }
        catch(Exception e)
        {
            log.error("Exception writing history: " + e, e);
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

    private def formatFriendlyDate(def date)
    {
        def formatter = DateTimeFormat.forPattern('MMMM d, yyyy')
        return formatter.print(date)
    }

    private def formatDateTime(def date) {
        def formatter = DateTimeFormat.forPattern('yyyy-MM-dd HH:mm')
        return formatter.print(date)
    }

    private def friendlyMessageSnippet(InboxMessage message) {
        def type = message.instanceOf(Fax) ? 'fax' : 'voicemail'
        return "${type} for [${message.owner.realName}] from [${message.from()}] left on [${formatDateTime(message.dateCreated)}]"
    }
}
