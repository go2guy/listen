package com.interact.listen.android.voicemail.contact;

public class ContactAddress implements Comparable<ContactAddress>
{
    private long dataID;
    
    private ContactMIME mime;
    private String value;
    private ContactType type;
    
    public ContactAddress(ContactMIME mime, String value, ContactType type)
    {
        if(mime == null || value == null || type == null)
        {
            throw new NullPointerException("MIME, value, and type required for ContactAddress");
        }
        this.mime = mime;
        this.value = value;
        this.type = type;
    }

    public void setDataID(long dataID)
    {
        this.dataID = dataID;
    }
    
    public long getDataID()
    {
        return this.dataID;
    }
    
    public ContactMIME getContactMime()
    {
        return mime;
    }

    public String getAddress()
    {
        return value;
    }

    public ContactType getContactType()
    {
        return type;
    }
    
    public int getType()
    {
        return type.getType(mime);
    }
    
    public String getMIME()
    {
        return mime.getMIME();
    }
    
    public boolean isLabel()
    {
        return type.isLabel();
    }
    
    public String getLabel()
    {
        return type.getLabel();
    }
    
    public boolean isOfficeNumber()
    {
        return type == ContactType.EXTENSION;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if(o instanceof ContactAddress)
        {
            ContactAddress c = (ContactAddress)o;
            return this.mime == c.mime && this.value.equals(c.value) && this.type == c.type;
        }
        return false;
    }
    
    @Override
    public int hashCode()
    {
        return (value.hashCode() << 6) & (mime.ordinal() << 5) & (type.ordinal());
    }

    @Override
    public int compareTo(ContactAddress c)
    {
        if(c == null)
        {
            return 1;
        }
        if(this.mime != c.mime)
        {
            return this.mime.ordinal() - c.mime.ordinal();
        }
        return this.value.compareTo(c.value);
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[Contact ").append(dataID).append(' ').append(mime.name()).append('-').append(type.name());
        sb.append(" '").append(value).append("']");
        return sb.toString();
    }
}
