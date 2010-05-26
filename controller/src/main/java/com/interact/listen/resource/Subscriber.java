package com.interact.listen.resource;

import com.interact.listen.util.ComparisonUtil;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

@Entity
@Table(name = "SUBSCRIBER")
public class Subscriber extends Resource implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    @Column(name = "VERSION")
    @Version
    private Integer version = Integer.valueOf(0);

    @Column(name = "NUMBER", unique = true, nullable = false)
    private String number;

    @Column(name = "VOICEMAIL_GREETING_LOCATION")
    private String voicemailGreetingLocation;

    @Column(name = "VOICEMAIL_PIN")
    private String voicemailPin;

    @JoinTable(name = "SUBSCRIBER_VOICEMAIL",
               joinColumns = @JoinColumn(name = "SUBSCRIBER_ID", unique = true),
               inverseJoinColumns = @JoinColumn(name = "VOICEMAIL_ID"))
    @OneToMany(cascade = CascadeType.ALL)
    private Set<Voicemail> voicemails = new HashSet<Voicemail>();

    public String getNumber()
    {
        return number;
    }

    public void setNumber(String number)
    {
        this.number = number;
    }

    @Override
    public Long getId()
    {
        return id;
    }

    @Override
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

    public String getVoicemailGreetingLocation()
    {
        return voicemailGreetingLocation;
    }

    public void setVoicemailGreetingLocation(String voicemailGreetingLocation)
    {
        this.voicemailGreetingLocation = voicemailGreetingLocation;
    }

    public String getVoicemailPin()
    {
        return voicemailPin;
    }

    public void setVoicemailPin(String voicemailPin)
    {
        this.voicemailPin = voicemailPin;
    }

    public Set<Voicemail> getVoicemails()
    {
        return voicemails;
    }

    public void setVoicemails(Set<Voicemail> voicemails)
    {
        this.voicemails = voicemails;
    }

    @Override
    public boolean validate()
    {
        boolean isValid = true;
        if(number == null || number.trim().equals(""))
        {
            addToErrors("Subscriber must have a number");
            isValid = false;
        }
        return isValid;
    }

    @Override
    public Subscriber copy(boolean withIdAndVersion)
    {
        Subscriber copy = new Subscriber();
        if(withIdAndVersion)
        {
            copy.setId(id);
            copy.setVersion(version);
        }

        copy.setNumber(number);
        copy.setVoicemailGreetingLocation(voicemailGreetingLocation);
        copy.setVoicemailPin(voicemailPin);
        copy.setVoicemails(voicemails);
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

        if(!(that instanceof Subscriber))
        {
            return false;
        }

        Subscriber subscriber = (Subscriber)that;

        if(!ComparisonUtil.isEqual(subscriber.getNumber(), getNumber()))
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
