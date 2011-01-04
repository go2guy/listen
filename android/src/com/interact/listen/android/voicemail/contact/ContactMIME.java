package com.interact.listen.android.voicemail.contact;

import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;

public enum ContactMIME
{
    PHONE(Phone.CONTENT_ITEM_TYPE),
    EMAIL(Email.CONTENT_ITEM_TYPE);
    
    private String mime;
    
    private ContactMIME(String mime)
    {
        this.mime = mime;
    }
    
    public String getMIME()
    {
        return mime;
    }
    
    public static ContactMIME getMIME(String mime)
    {
        ContactMIME[] mimes = values();
        for(ContactMIME cm : mimes)
        {
            if(cm.getMIME().equals(mime))
            {
                return cm;
            }
        }
        return null;
    }
}
