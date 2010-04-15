package com.interact.listen.resource;

import java.io.Serializable;
import java.util.*;

import javax.persistence.*;

import org.hibernate.annotations.CollectionOfElements;

@Entity
public class Conference extends Resource implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Version
    private Integer version = Integer.valueOf(0);

    @Column(nullable = false)
    private Boolean isStarted;

    @Column(nullable = false)
    private String adminPin;

    @Column(nullable = false)
    private String number;

    @CollectionOfElements
    private List<Participant> participants = new ArrayList<Participant>();

    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private Set<ConferenceHistory> conferenceHistorys = new HashSet<ConferenceHistory>();

    public Boolean getIsStarted()
    {
        return isStarted;
    }

    public void setIsStarted(Boolean isStarted)
    {
        this.isStarted = isStarted;
    }

    public String getAdminPin()
    {
        return adminPin;
    }

    public void setAdminPin(String adminPin)
    {
        this.adminPin = adminPin;
    }

    public String getNumber()
    {
        return number;
    }

    public void setNumber(String number)
    {
        this.number = number;
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

    public List<Participant> getParticipants()
    {
        return participants;
    }

    public void setParticipants(List<Participant> participants)
    {
        this.participants = participants;
    }

    public Set<ConferenceHistory> getConferenceHistorys()
    {
        return conferenceHistorys;
    }

    public void setConferenceHistorys(Set<ConferenceHistory> conferenceHistorys)
    {
        this.conferenceHistorys = conferenceHistorys;
    }

    @Override
    public boolean validate()
    {
        boolean isValid = true;

        if(isStarted == null)
        {
            addToErrors("isStarted cannot be null");
            isValid = false;
        }

        if(adminPin == null || adminPin.trim().equals(""))
        {
            addToErrors("adminPin is required");
            isValid = false;
        }

        if(number == null || number.trim().equals(""))
        {
            addToErrors("number is required");
            isValid = false;
        }

        return isValid;
    }
}
