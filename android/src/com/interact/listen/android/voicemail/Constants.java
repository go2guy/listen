package com.interact.listen.android.voicemail;

public final class Constants
{
    public static final String TAG = "Listen";

    public static final String ACCOUNT_TYPE   = "com.interact.listen.voicemail";
    public static final String AUTHTOKEN_TYPE = "com.interact.listen.voicemail";

    public static final String ACTION_LISTALL_VOICEMAIL     = "com.interact.listen.voicemail.LIST";
    public static final String ACTION_VIEW_VOICEMAIL        = "com.interact.listen.voicemail.VIEW";
    public static final String ACTION_APP_SETTINGS          = "com.interact.listen.voicemail.SETTINGS";
    public static final String ACTION_AUTHORIZE_SETTINGS    = "com.interact.listen.voicemail.AUTHORIZE";
    public static final String ACTION_NOTIFY_NEW_VOICEMAILS = "com.interact.listen.voicemail.NEW_VOICEMAILS";
    public static final String ACTION_NOTIFY_ERROR          = "com.interact.listen.voicemail.NOTIFY_ERROR";
    public static final String ACTION_MARK_NOTIFIED         = "com.interact.listen.voicemail.MARK_NOTIFIED";
    public static final String ACTION_MARK_READ             = "com.interact.listen.voicemail.MARK_READ";
    
    public static final String EXTRA_ID                = "id";         // provider voicemail id
    public static final String EXTRA_VOICEMAIL_UPDATED = "updated";    // flag indicating a voicemail update
    public static final String EXTRA_ACCOUNT_NAME      = "username";   // account name
    public static final String EXTRA_IDS               = "ids";        // int array of provider voicemail id's
    public static final String EXTRA_COUNT             = "count";      // indicate number of voicemails
    public static final String EXTRA_NOTIFY_ERROR      = "noterrmess"; // error message associated to a notification
    public static final String EXTRA_IS_READ           = "isRead";
    private Constants()
    {
    }
}
