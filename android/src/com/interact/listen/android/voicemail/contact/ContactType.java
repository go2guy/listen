package com.interact.listen.android.voicemail.contact;

import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.TextUtils;

public enum ContactType
{
    // TODO: localize contact type labels
    EXTENSION(Phone.TYPE_WORK  , 0                , "extension", ""         , false),
    VOICEMAIL(Phone.TYPE_CUSTOM, 0                , "voicemail", "voicemail", false),
    WORK     (Phone.TYPE_WORK  , Email.TYPE_WORK  , "work"     , ""         , false),
    HOME     (Phone.TYPE_HOME  , Email.TYPE_HOME  , "home"     , ""         , false),
    MOBILE   (Phone.TYPE_MOBILE, Email.TYPE_MOBILE, "mobile"   , ""         , true),
    OTHER    (Phone.TYPE_OTHER , Email.TYPE_OTHER , ""         , ""         , true);
    
    private int phoneType;
    private int emailType;
    private String viewLabel;
    private String label;
    private boolean text;
    
    private ContactType(int phoneType, int emailType, String viewLabel, String label, boolean text)
    {
        this.phoneType = phoneType;
        this.emailType = emailType;
        this.viewLabel = viewLabel;
        this.label = label;
        this.text = text;
    }
    
    public boolean isTextable()
    {
        return text;
    }
    
    public int getType(ContactMIME mime)
    {
        return mime == ContactMIME.EMAIL ? emailType : phoneType;
    }

    public boolean isLabel()
    {
        return label.length() > 0;
    }
    
    public String getLabel()
    {
        return label;
    }
    
    public static ContactType getContactType(ContactMIME mime, int dataType, boolean isExt, String label)
    {
        if(mime != null)
        {
            final String lb = label == null ? "" : label;
            ContactType[] types = values();
            for(ContactType type : types)
            {
                if(type.getType(mime) == dataType && (isExt == (type == EXTENSION)) &&
                    TextUtils.equals(type.getLabel(), lb))
                {
                    return type;
                }
            }
        }
        return null;
    }
    
    public String getViewLabel()
    {
        return isLabel() ? getLabel() : viewLabel;
    }
}
