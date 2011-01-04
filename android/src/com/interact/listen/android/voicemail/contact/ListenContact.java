package com.interact.listen.android.voicemail.contact;

import java.util.SortedSet;
import java.util.TreeSet;


public class ListenContact implements Comparable<ListenContact>
{
    private long rawID;
    private boolean deleted;
    private long nameDataID;
    
    private long subscriberId;
    private String name;
    private SortedSet<ContactAddress> addresses;

    public ListenContact(long subscriberId, String name)
    {
        this.rawID = 0;
        this.deleted = false;
        this.nameDataID = 0;
        this.subscriberId = subscriberId;
        this.name = name;
        this.addresses = new TreeSet<ContactAddress>();
    }

    public void setRawID(Long rawID)
    {
        this.rawID = rawID;
    }
    
    public long getRawID()
    {
        return rawID;
    }

    public void setDeleted(boolean deleted)
    {
        this.deleted = deleted;
    }
    
    public boolean isDeleted()
    {
        return deleted;
    }
    
    public long getSubscriberId()
    {
        return subscriberId;
    }
    
    public String getName()
    {
        return name;
    }

    public long getNameDataID()
    {
        return nameDataID;
    }
    
    void setName(long id, String n)
    {
        this.nameDataID = id;
        this.name = n;
    }
    
    public SortedSet<ContactAddress> getAddresses()
    {
        return addresses;
    }
    
    public boolean addAddress(ContactAddress address)
    {
        return addresses.add(address);
    }
    
    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof ListenContact))
        {
            return false;
        }
        ListenContact c = (ListenContact)o;
        return getSubscriberId() == c.getSubscriberId() && getName().equals(c.getName());
    }
    
    @Override
    public int hashCode()
    {
        return (int)getSubscriberId();
    }

    @Override
    public int compareTo(ListenContact another)
    {
        if(another == null || subscriberId > another.subscriberId)
        {
            return 1;
        }
        return subscriberId == another.subscriberId ? 0 : -1;
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[ListenContact ").append(subscriberId).append(" '").append(name);
        sb.append("' size=").append(addresses.size()).append(" rawID=").append(rawID);
        sb.append(" delete=").append(deleted).append(' ').append(addresses).append(']');
        return sb.toString();
    }
}
