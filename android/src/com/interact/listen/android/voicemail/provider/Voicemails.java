package com.interact.listen.android.voicemail.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public final class Voicemails implements BaseColumns
{
    public static final Uri CONTENT_URI = Uri.parse("content://" + VoicemailProvider.AUTHORITY + "/voicemails");

    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.interact.listen.voicemails";
    
    public static final String USER_NAME = "user_name";
    public static final String VOICEMAIL_ID = "voicemail_id";
    public static final String DATE_CREATED = "date_created";
    public static final String IS_NEW = "is_new";
    public static final String HAS_NOTIFIED = "has_notified";
    public static final String LEFT_BY = "left_by";
    public static final String LEFT_BY_NAME = "left_by_name";
    public static final String DESCRIPTION = "description";
    public static final String DURATION = "duration";
    public static final String TRANSCRIPT = "transcript";
    public static final String LABEL = "label";
    public static final String STATE = "state";
    public static final String AUDIO_STATE = "audio_state";
    public static final String AUDIO_DATE = "audio_date";

    public static final String DATA = "_data";
    
    private Voicemails()
    {
    }
    
}
