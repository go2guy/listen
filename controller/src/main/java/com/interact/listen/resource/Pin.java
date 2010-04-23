package com.interact.listen.resource;

import java.io.Serializable;

import javax.persistence.*;

@Entity
public class Pin extends Resource implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Version
    private Integer version = Integer.valueOf(0);

    @Column(nullable = false, unique = true)
    private String number;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PinType type = PinType.ACTIVE;

    public static enum PinType
    {
        ACTIVE, ADMIN, PASSIVE;
    }

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
}
