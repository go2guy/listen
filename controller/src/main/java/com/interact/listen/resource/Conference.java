package com.interact.listen.resource;

import com.interact.listen.PersistenceService;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;
import com.interact.listen.stats.StatSenderFactory;
import com.interact.listen.util.ComparisonUtil;

import java.io.Serializable;
import java.util.*;

import javax.persistence.*;

import org.hibernate.Session;

@Entity
public class Conference extends Resource implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Version
    private Integer version = Integer.valueOf(0);

    // TODO enforce unique description per User
    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Boolean isStarted;
    
    @Column(nullable = false)
    private Boolean isRecording;
    
    @Column(nullable = false)
    private Date startTime = new Date();

    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.EAGER)
    private Set<Pin> pins = new HashSet<Pin>();

    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.EAGER)
    private List<Participant> participants = new ArrayList<Participant>();

    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private Set<ConferenceHistory> conferenceHistorys = new HashSet<ConferenceHistory>();
    
    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private Set<Audio> recordings = new HashSet<Audio>();

    @ManyToOne
    private User user;

    public Boolean getIsStarted()
    {
        return isStarted;
    }

    public void setIsStarted(Boolean isStarted)
    {
        this.isStarted = isStarted;
    }
    
    public Boolean getIsRecording()
    {
        return isRecording;
    }

    public void setIsRecording(Boolean isRecording)
    {
        this.isRecording = isRecording;
    }

    public Set<Pin> getPins()
    {
        return pins;
    }

    public void setPins(Set<Pin> pins)
    {
        this.pins = pins;
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

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
    
    public Date getStartTime()
    {
        return startTime == null ? null : new Date(startTime.getTime());
    }

    public void setStartTime(Date startTime)
    {
        this.startTime = startTime == null ? null : new Date(startTime.getTime());
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
    
    public Set<Audio> getRecordings()
    {
        return recordings;
    }

    public void setRecordings(Set<Audio> recordings)
    {
        this.recordings = recordings;
    }

    public User getUser()
    {
        return user;
    }

    public void setUser(User user)
    {
        this.user = user;
    }

    @Override
    public boolean validate()
    {
        if(description == null || description.trim().equals(""))
        {
            addToErrors("description cannot be blank or null");
        }

        if(isStarted == null)
        {
            addToErrors("isStarted cannot be null");
        }
        
        if(isRecording == null)
        {
            addToErrors("isRecording cannot be null");
        }
        
        if(startTime == null)
        {
            addToErrors("startTime cannot be null");
        }

        return !hasErrors();
    }

    @Override
    public Conference copy(boolean withIdAndVersion)
    {
        Conference copy = new Conference();
        if(withIdAndVersion)
        {
            copy.setId(id);
            copy.setVersion(version);
        }

        copy.setDescription(description);
        copy.setStartTime(startTime);
        copy.setConferenceHistorys(conferenceHistorys);
        copy.setRecordings(recordings);
        copy.setIsStarted(isStarted);
        copy.setIsRecording(isRecording);
        copy.setParticipants(participants);

        for(Pin pin : pins)
        {
            copy.addToPins(pin.copy(false));
        }

        copy.setUser(user);

        return copy;
    }

    @Override
    public void afterSave(Session session)
    {
        StatSender statSender = StatSenderFactory.getStatSender();
        ConferenceHistory history = new ConferenceHistory();
        history.setConference(this);
        history.setUser("Current User"); // FIXME
        history.setDescription("Conference created");

        PersistenceService persistenceService = new PersistenceService(session);
        persistenceService.save(history);
        
        if(isStarted.booleanValue())
        {
            statSender.send(Stat.CONFERENCE_START);
        }
        
        if(isRecording.booleanValue())
        {
            history = new ConferenceHistory();
            history.setConference(this);
            history.setUser("Current User"); // FIXME
            history.setDescription("Conference recording " + (isRecording ? "started" : "ended"));

            persistenceService = new PersistenceService(session);
            persistenceService.save(history);
            
            statSender.send(Stat.CONFERENCE_RECORDING_START);
        }
    }

    @Override
    public void afterUpdate(Session session, Resource original)
    {
        StatSender statSender = StatSenderFactory.getStatSender();
        Conference originalConference = (Conference)original;
        if(isStarted.booleanValue() != originalConference.getIsStarted().booleanValue())
        {
            ConferenceHistory history = new ConferenceHistory();
            history.setConference(this);
            history.setUser("Current User"); // FIXME
            history.setDescription("Conference " + (isStarted ? "started" : "ended"));

            PersistenceService persistenceService = new PersistenceService(session);
            persistenceService.save(history);
            
            if(isStarted.booleanValue())
            {
                //Conference moved to 'started'
                startTime = new Date();
                statSender.send(Stat.CONFERENCE_START);
            }
            else
            {
                //Conference ended
                Long conferenceLength = System.currentTimeMillis() - startTime.getTime();
                
                //want conference length in seconds
                statSender.send(Stat.CONFERENCE_LENGTH, conferenceLength / 1000);
            }
        }
        
        if(isRecording.booleanValue() != originalConference.getIsRecording().booleanValue())
        {
            ConferenceHistory history = new ConferenceHistory();
            history.setConference(this);
            history.setUser("Current User"); // FIXME
            history.setDescription("Conference recording " + (isRecording ? "started" : "ended"));

            PersistenceService persistenceService = new PersistenceService(session);
            persistenceService.save(history);
            
            if(isRecording.booleanValue())
            {
                statSender.send(Stat.CONFERENCE_RECORDING_START);
            }
            else
            {
                statSender.send(Stat.CONFERENCE_RECORDING_STOP);
            }
        }
    }

    // TODO is this used anywhere anymore?
    public static Conference findByPinNumber(String pinNumber, Session session)
    {
        final String hql = "select c from Conference c join c.pins as p where p.number = ?";

        org.hibernate.Query query = session.createQuery(hql);
        query.setMaxResults(1);
        query.setString(0, pinNumber);

        return (Conference)query.uniqueResult();
    }

    @Override
    public boolean equals(Object that)
    {
        if(this == that)
        {
            return true;
        }

        if(!(that instanceof Conference))
        {
            return false;
        }

        final Conference conference = (Conference)that;

        if(!ComparisonUtil.isEqual(conference.getDescription(), getDescription()))
        {
            return false;
        }

        if(!ComparisonUtil.isEqual(conference.getUser(), getUser()))
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
        hash *= prime + (getDescription() == null ? 0 : getDescription().hashCode());
        hash *= prime + (getUser() == null ? 0 : getUser().hashCode());
        return hash;
    }
}
