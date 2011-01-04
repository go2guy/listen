package com.interact.listen.android.voicemail.contact;

import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.TextUtils;

public enum ContactType
{
    EXTENSION(Phone.TYPE_WORK  , 0                , ""),
    VOICEMAIL(Phone.TYPE_CUSTOM, 0                , "voicemail"), // should be localized
    WORK     (Phone.TYPE_WORK  , Email.TYPE_WORK  , ""),
    HOME     (Phone.TYPE_HOME  , Email.TYPE_HOME  , ""),
    MOBILE   (Phone.TYPE_MOBILE, Email.TYPE_MOBILE, ""),
    OTHER    (Phone.TYPE_OTHER , Email.TYPE_OTHER , "");
    
    private int phoneType;
    private int emailType;
    private String label;
    
    private ContactType(int phoneType, int emailType, String label)
    {
        this.phoneType = phoneType;
        this.emailType = emailType;
        this.label = label;
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
        ContactType[] types = values();
        for(ContactType type : types)
        {
            if(type.getType(mime) == dataType && (isExt == (type == EXTENSION)) &&
                TextUtils.equals(type.getLabel(), label))
            {
                return type;
            }
        }
        return null;
    }
}
