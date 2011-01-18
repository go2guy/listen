package com.interact.listen.resource;

import java.io.Serializable;

import javax.persistence.*;

import org.hibernate.annotations.Type;
import org.joda.time.LocalTime;

public class TimeRestriction extends Resource implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    @Column(name = "VERSION")
    @Version
    private Integer version = Integer.valueOf(0);

    @Column(name = "START_ENTRY")
    private String startEntry;
    
    @Column(name = "END_ENTRY")
    private String endEntry;
    
    @Column(name = "START_TIME", nullable = false)
    @Type(type = "org.joda.time.contrib.hibernate.PersistentLocalTimeAsTime")
    private LocalTime startTime;
    
    @Column(name = "END_TIME", nullable = false)
    @Type(type = "org.joda.time.contrib.hibernate.PersistentLocalTimeAsTime")
    private LocalTime endTime;
    
    @Column(name = "ACTION")
    @Enumerated(EnumType.STRING)
    private Action action;
    
    @JoinColumn(name = "SUBSCRIBER_ID")
    @ManyToOne
    private Subscriber subscriber;
    
    public static enum Action
    {
        NEW_VOICEMAIL_EMAIL, NEW_VOICEMAIL_SMS;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Integer getVersion()
    {
        return version;
    }

    public void setVersion(Integer version)
    {
        this.version = version;
    }

    public String getStartEntry()
    {
        return startEntry;
    }

    public void setStartEntry(String startEntry)
    {
        this.startEntry = startEntry;
    }

    public String getEndEntry()
    {
        return endEntry;
    }

    public void setEndEntry(String endEntry)
    {
        this.endEntry = endEntry;
    }

    public LocalTime getStartTime()
    {
        return startTime;
    }

    public void setStartTime(LocalTime startTime)
    {
        this.startTime = startTime;
    }

    public LocalTime getEndTime()
    {
        return endTime;
    }

    public void setEndTime(LocalTime endTime)
    {
        this.endTime = endTime;
    }

    public Action getAction()
    {
        return action;
    }

    public void setAction(Action action)
    {
        this.action = action;
    }

    public Subscriber getSubscriber()
    {
        return subscriber;
    }

    public void setSubscriber(Subscriber subscriber)
    {
        this.subscriber = subscriber;
    }
    
    @Override
    public boolean validate()
    {
        if(subscriber == null)
        {
            addToErrors("subscriber cannot be null");
        }

        if(startTime == null)
        {
            addToErrors("startTime cannot be null");
        }
        
        if(endTime == null)
        {
            addToErrors("endTime cannot be null");
        }

        return !hasErrors();
    }
    
    @Override
    public TimeRestriction copy(boolean withIdAndVersion)
    {
        TimeRestriction copy = new TimeRestriction();
        if(withIdAndVersion)
        {
            copy.setId(getId());
            copy.setVersion(getVersion());
        }

        copy.setStartEntry(getStartEntry());
        copy.setEndEntry(getEndEntry());
        copy.setStartTime(getStartTime());
        copy.setEndTime(getEndTime());
        copy.setAction(getAction());
        copy.setSubscriber(subscriber);
        return copy;
    }
}
