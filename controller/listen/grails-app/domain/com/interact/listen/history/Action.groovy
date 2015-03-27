package com.interact.listen.history

enum Action {
    CANCELLED_CONFERENCE_INVITATION,
    CHANGED_ACCOUNT_EMAIL_ADDRESS,
    CHANGED_ACCOUNT_NAME,
    CHANGED_ACCOUNT_PASSWORD,
    CHANGED_ACCOUNT_USERNAME,
    CHANGED_AFTER_HOURS_ALTERNATE_NUMBER,
    CHANGED_AFTER_HOURS_MOBILE_NUMBER,
    CHANGED_CONFERENCE_INVITATION,
    CHANGED_EXTENSION_IP_ADDRESS,
    CHANGED_FIND_ME_EXPIRATION,
    CHANGED_FIND_ME_EXPIRATION_REMINDER_SMS_NUMBER,
    CHANGED_FIND_ME_NUMBERS,
    CHANGED_MOBILE_PHONE_VISIBILITY,
    CHANGED_NEW_CONFERENCE_PIN_LENGTH,
    CHANGED_NEW_VOICEMAIL_EMAIL_ADDRESS,
    CHANGED_NEW_VOICEMAIL_SMS_NUMBER,
    CHANGED_OTHER_PHONE_VISIBILITY,
    CHANGED_PAGER_ALTERNATE_NUMBER,
    CHANGED_REALIZE_CONFIGURATION,
    CHANGED_VOICEMAIL_PIN,
    CREATED_ATTENDANT_HOLIDAY,
    CREATED_CONFERENCE_INVITATION,
    CREATED_DIRECT_INWARD_DIAL_NUMBER,
    CREATED_DIRECT_MESSAGE_NUMBER,
    CREATED_EXTENSION,
    CREATED_EXTERNAL_ROUTE,
    CREATED_INTERNAL_ROUTE,
    CREATED_MOBILE_PHONE,
    CREATED_OTHER_PHONE,
    CREATED_OUTDIAL_RESTRICTION,
    CREATED_OUTDIAL_RESTRICTION_EXCEPTION,
    CREATED_USER,
    DELETED_ATTENDANT_HOLIDAY,
    DELETED_CONFERENCE_RECORDING,
    DELETED_DIRECT_INWARD_DIAL_NUMBER,
    DELETED_DIRECT_MESSAGE_NUMBER,
    DELETED_EXTENSION,
    DELETED_EXTERNAL_ROUTE,
    DELETED_FAX,
    DELETED_INTERNAL_ROUTE,
    DELETED_MOBILE_PHONE,
    DELETED_OTHER_PHONE,
    DELETED_OUTDIAL_RESTRICTION,
    DELETED_OUTDIAL_RESTRICTION_EXCEPTION,
    DELETED_USER,
    DELETED_VOICEMAIL,
    DISABLED_ANDROID_CLOUD_TO_DEVICE,
    DISABLED_FIND_ME_EXPIRATION_REMINDER_SMS,
    DISABLED_RECURRING_VOICEMAIL_SMS,
    DISABLED_NEW_VOICEMAIL_EMAIL,
    DISABLED_NEW_VOICEMAIL_SMS,
    DISABLED_TRANSCRIPTION,
    DISABLED_USER,
    DOWNLOADED_CONFERENCE_RECORDING,
    DOWNLOADED_FAX,
    DOWNLOADED_VOICEMAIL,
    DROPPED_CONFERENCE_CALLER,
    ENABLED_ANDROID_CLOUD_TO_DEVICE,
    ENABLED_FIND_ME_EXPIRATION_REMINDER_SMS,
    ENABLED_RECURRING_VOICEMAIL_SMS,
    ENABLED_NEW_VOICEMAIL_EMAIL,
    ENABLED_NEW_VOICEMAIL_SMS,
    ENABLED_TRANSCRIPTION,
    ENABLED_USER,
    FORWARDED_EXTENSION,
    FORWARDED_VOICEMAIL,
    JOINED_CONFERENCE,
    LEFT_FAX,
    LEFT_VOICEMAIL,
    LISTENED_TO_VOICEMAIL,
    LOGGED_IN,
    LOGGED_OUT,
    MARKED_FAX_NEW,
    MARKED_FAX_OLD,
    MARKED_VOICEMAIL_NEW,
    MARKED_VOICEMAIL_OLD,
    MUTED_CONFERENCE_CALLER,
    SENT_FAX,
    SENT_NEW_FAX_EMAIL,
    SENT_NEW_FAX_SMS,
    SENT_NEW_VOICEMAIL_EMAIL,
    SENT_NEW_VOICEMAIL_SMS,
    SENT_VOICEMAIL_ALTERNATE_NUMBER_PAGE,
    STARTED_CONFERENCE,
    STARTED_RECORDING_CONFERENCE,
    STOPPED_CONFERENCE,
    STOPPED_RECORDING_CONFERENCE,
    UNFORWARDED_EXTENSION,
    UNMUTED_CONFERENCE_CALLER,
    CREATED_ACD_SKILL,
    UPDATED_ACD_SKILL,
    DELETED_ACD_SKILL,
    ADDED_ACD_SKILL_USER,
    DELETED_ACD_SKILL_USER,
    UPDATED_ACD_STATUS,
    UPDATED_ACD_CONTACTNUMBER,
    CHANGED_ORGANIZATION_EXT_LENGTH,
    UPDATED_ATTENDANT_MENU,
    UPDATED_ACD_SKILL_PRIORITY_USER;

    String toString() {
        def name = name().toLowerCase().replaceAll(/_/, ' ')
        return name[0].toUpperCase() + name[1..-1]
    }
}
