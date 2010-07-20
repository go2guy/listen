package com.interact.listen.resource;

import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;
import com.interact.listen.util.ComparisonUtil;

import java.io.Serializable;
import java.security.SecureRandom;

import javax.persistence.*;

@Entity
@Table(name = "PIN")
public class Pin extends Resource implements Serializable
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

    @Column(name = "TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private PinType type = PinType.ACTIVE;

    public static enum PinType
    {
        ACTIVE, ADMIN, PASSIVE;

        public String toFriendlyName()
        {
            String name = this.name();
            return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        }

        public static PinType fromFriendlyName(String friendlyName)
        {
            return PinType.valueOf(friendlyName.toUpperCase());
        }
    }

    @JoinColumn(name = "CONFERENCE_ID")
    @ManyToOne
    private Conference conference;

    // factory creation method
    public static Pin newInstance(String number, PinType type)
    {
        Pin pin = new Pin();
        pin.setNumber(number);
        pin.setType(type);
        return pin;
    }

    // factory creation method
    public static Pin newRandomInstance(PinType type)
    {
        Pin pin = new Pin();
        pin.setNumber(generateRandomPin());
        pin.setType(type);
        return pin;
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

    public String getNumber()
    {
        return number;
    }

    public void setNumber(String number)
    {
        this.number = number;
    }

    public PinType getType()
    {
        return type;
    }

    public void setType(PinType type)
    {
        this.type = type;
    }

    public Conference getConference()
    {
        return conference;
    }

    public void setConference(Conference conference)
    {
        this.conference = conference;
    }

    @Override
    public boolean validate()
    {
        if(number == null || number.trim().equals(""))
        {
            addToErrors("number cannot be null or empty");
        }

        if(type == null)
        {
            addToErrors("type cannot be null");
        }

        if(conference == null)
        {
            addToErrors("conference cannot be null");
        }

        return !hasErrors();
    }

    @Override
    public Pin copy(boolean withIdAndVersion)
    {
        Pin copy = new Pin();
        if(withIdAndVersion)
        {
            copy.setId(id);
            copy.setVersion(version);
        }

        copy.setConference(conference);
        copy.setNumber(number);
        copy.setType(type);
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

        if(!(that instanceof Pin))
        {
            return false;
        }

        Pin pin = (Pin)that;

        if(!ComparisonUtil.isEqual(pin.getNumber(), getNumber()))
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

    public static String generateRandomPin()
    {
        int pinLength = Integer.parseInt(Configuration.get(Property.Key.CONFERENCING_PINLENGTH));
        SecureRandom random = new SecureRandom();
        StringBuilder number = new StringBuilder();
        while(number.length() < pinLength)
        {
            number.append(String.valueOf(random.nextInt(10)));
        }
        return number.toString();
    }
}
