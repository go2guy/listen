package com.interact.listen.resource;

import com.interact.listen.util.ComparisonUtil;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name = "CALL_DETAIL_RECORD")
public class CallDetailRecord extends Resource implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static enum CallDirection
    {
        INBOUND, OUTBOUND;
    }

    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    @Column(name = "VERSION")
    @Version
    private Integer version = Integer.valueOf(0);

    @Column(name = "DATE_STARTED")
    private Date dateStarted = new Date();

    @Column(name = "DURATION")
    private Long duration;

    @Column(name = "SERVICE")
    private String service;

    @JoinColumn(name = "SUBSCRIBER_ID")
    @OneToOne
    private Subscriber subscriber;

    @Column(name = "ANI")
    private String ani;

    @Column(name = "DNIS")
    private String dnis;

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

    public Date getDateStarted()
    {
        return dateStarted == null ? dateStarted : new Date(dateStarted.getTime());
    }

    public void setDateStarted(Date dateStarted)
    {
        this.dateStarted = dateStarted == null ? null : new Date(dateStarted.getTime());
    }

    public Long getDuration()
    {
        return duration;
    }

    public void setDuration(Long duration)
    {
        this.duration = duration;
    }

    public String getService()
    {
        return service;
    }

    public void setService(String service)
    {
        this.service = service;
    }

    public Subscriber getSubscriber()
    {
        return subscriber;
    }

    public void setSubscriber(Subscriber subscriber)
    {
        this.subscriber = subscriber;
    }

    public String getAni()
    {
        return ani;
    }

    public void setAni(String ani)
    {
        this.ani = ani;
    }

    public String getDnis()
    {
        return dnis;
    }

    public void setDnis(String dnis)
    {
        this.dnis = dnis;
    }

    @Override
    public boolean validate()
    {
        return !this.hasErrors();
    }

    @Override
    public CallDetailRecord copy(boolean withIdAndVersion)
    {
        CallDetailRecord copy = new CallDetailRecord();
        if(withIdAndVersion)
        {
            copy.setId(id);
            copy.setVersion(version);
        }
        copy.setAni(ani);
        copy.setDateStarted(dateStarted == null ? null : new Date(dateStarted.getTime()));
        copy.setDnis(dnis);
        copy.setDuration(duration);
        copy.setService(service);
        copy.setSubscriber(subscriber);
        return copy;
    }

    @Override
    public boolean equals(Object that)
    {
        if(this == that)
        {
            return true;
        }

        if(that == null)
        {
            return false;
        }

        if(!(that instanceof CallDetailRecord))
        {
            return false;
        }

        final CallDetailRecord callDetailRecord = (CallDetailRecord)that;

        if(!ComparisonUtil.isEqual(callDetailRecord.getAni(), getAni()))
        {
            return false;
        }

        if(!ComparisonUtil.isEqual(callDetailRecord.getDnis(), getDnis()))
        {
            return false;
        }

        if(!ComparisonUtil.isEqual(callDetailRecord.getDateStarted(), getDateStarted()))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int hash = 1;
        hash *= prime + (getAni() == null ? 0 : getAni().hashCode());
        hash *= prime + (getDnis() == null ? 0 : getDnis().hashCode());
        hash *= prime + (getDateStarted() == null ? 0 : getDateStarted().hashCode());
        return hash;
    }
}
