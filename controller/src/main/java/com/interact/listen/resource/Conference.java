package com.interact.listen.resource;

import java.io.Serializable;
import java.util.*;

import javax.persistence.*;

import org.hibernate.Session;
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

    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.EAGER)
    private Set<Pin> pins = new HashSet<Pin>();

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

    public Set<Pin> getPins()
    {
        return new HashSet<Pin>(pins);
    }

    public void setPins(Set<Pin> pins)
    {
        this.pins = new HashSet<Pin>(pins);
        for(Pin pin : pins)
        {
            pin.setConference(this);
        }
    }

    public void addToPins(Pin pin)
    {
        pin.setConference(this);
        this.pins.add(pin);
    }

    public void removeFromPins(Pin pin)
    {
        this.pins.remove(pin);
        pin.setConference(null);
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
        if(isStarted == null)
        {
            addToErrors("isStarted cannot be null");
        }

        return !hasErrors();
    }

    public static Conference findByPinNumber(String pinNumber, Session session)
    {
        final String hql = "select c from Conference c join c.pins as p where p.number = ?";

        org.hibernate.Query query = session.createQuery(hql);
        query.setMaxResults(1);
        query.setString(0, pinNumber);

        return (Conference)query.uniqueResult();
    }
}
