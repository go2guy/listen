package com.interact.listen.resource;

import com.interact.listen.util.ComparisonUtil;

import java.io.Serializable;

import javax.persistence.*;

import org.hibernate.annotations.Type;
import org.joda.time.Duration;

@Entity
public class CallDetailRecord extends History implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Column(name = "DURATION", nullable = true)
    @Type(type = "org.joda.time.contrib.hibernate.PersistentDuration")
    private Duration duration;

    @Column(name = "ANI", nullable = true)
    private String ani;

    @Column(name = "DNIS", nullable = true)
    private String dnis;

    @Column(name = "DIRECTION", nullable = true)
    @Enumerated(EnumType.STRING)
    private CallDirection direction = CallDirection.INBOUND;

    public static enum CallDirection
    {
        INBOUND, OUTBOUND;
    }

    public Duration getDuration()
    {
        return duration;
    }

    public void setDuration(Duration duration)
    {
        this.duration = duration;
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

    public CallDirection getDirection()
    {
        return direction;
    }

    public void setDirection(CallDirection direction)
    {
        this.direction = direction;
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
            copy.setId(getId());
            copy.setVersion(getVersion());
        }
        copy.setAni(ani);
        copy.setDate(getDate());
        copy.setDnis(dnis);
        copy.setDuration(duration);
        copy.setService(getService());
        copy.setSubscriber(getSubscriber());
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

        if(!ComparisonUtil.isEqual(callDetailRecord.getDate(), getDate()))
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
        hash *= prime + (getDate() == null ? 0 : getDate().hashCode());
        return hash;
    }
}
