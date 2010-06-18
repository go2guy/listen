package com.interact.listen.resource;

import com.interact.listen.util.ComparisonUtil;

import java.io.Serializable;

import javax.persistence.*;

@Entity
@Table(name = "ACCESS_NUMBER")
public class AccessNumber extends Resource implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    @Column(name = "VERSION")
    @Version
    private Integer version = Integer.valueOf(0);

    @Column(name = "NUMBER", nullable = false, unique = true)
    private String number;

    @JoinColumn(name = "SUBSCRIBER_ID", nullable = false)
    @ManyToOne
    private Subscriber subscriber;

    @Column(name = "GREETING_LOCATION")
    private String greetingLocation;

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

    public String getNumber()
    {
        return number;
    }

    public void setNumber(String number)
    {
        this.number = number;
    }

    public Subscriber getSubscriber()
    {
        return subscriber;
    }

    public void setSubscriber(Subscriber subscriber)
    {
        this.subscriber = subscriber;
    }

    public String getGreetingLocation()
    {
        return greetingLocation;
    }

    public void setGreetingLocation(String greetingLocation)
    {
        this.greetingLocation = greetingLocation;
    }

    @Override
    public boolean validate()
    {
        if(number == null || number.trim().equals(""))
        {
            addToErrors("number cannot be null or empty");
        }

        if(subscriber == null)
        {
            addToErrors("subscriber cannot be null");
        }

        return !hasErrors();
    }

    @Override
    public AccessNumber copy(boolean withIdAndVersion)
    {
        AccessNumber copy = new AccessNumber();
        if(withIdAndVersion)
        {
            copy.setId(id);
            copy.setVersion(version);
        }
        copy.setGreetingLocation(greetingLocation);
        copy.setNumber(number);
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

        if(!(that instanceof AccessNumber))
        {
            return false;
        }

        AccessNumber accessNumber = (AccessNumber)that;

        if(!ComparisonUtil.isEqual(accessNumber.getNumber(), getNumber()))
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
        hash *= prime + (getNumber() == null ? 0 : getNumber().hashCode());
        return hash;
    }
}
