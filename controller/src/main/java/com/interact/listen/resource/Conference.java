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
@Table(name = "CONFERENCE")
public class Conference extends Resource implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    @Column(name = "VERSION")
    @Version
    private Integer version = Integer.valueOf(0);

    // TODO enforce unique description per User
    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

    @Column(name = "IS_STARTED", nullable = false)
    private Boolean isStarted;
    
    @Column(name = "IS_RECORDING", nullable = false)
    private Boolean isRecording;
    
    @Column(name = "START_TIME", nullable = false)
    private Date startTime = new Date();

    @JoinTable(name = "CONFERENCE_PIN",
               joinColumns = @JoinColumn(name = "CONFERENCE_ID", unique = true),
               inverseJoinColumns = @JoinColumn(name = "PIN_ID"))
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Pin> pins = new HashSet<Pin>();

    @JoinTable(name = "CONFERENCE_PARTICIPANT",
               joinColumns = @JoinColumn(name = "CONFERENCE_ID", unique = true),
               inverseJoinColumns = @JoinColumn(name = "PARTICIPANT_ID"))
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Participant> participants = new TreeSet<Participant>();

    @JoinTable(name = "CONFERENCE_CONFERENCE_HISTORY",
               joinColumns = @JoinColumn(name = "CONFERENCE_ID", unique = true),
               inverseJoinColumns = @JoinColumn(name = "CONFERENCE_HISTORY_ID"))
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<ConferenceHistory> conferenceHistorys = new HashSet<ConferenceHistory>();
    
    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.EAGER)
    private Set<Audio> recordings = new HashSet<Audio>();

    @JoinTable(name = "USER_CONFERENCE",
               joinColumns = @JoinColumn(name = "CONFERENCE_ID"),
               inverseJoinColumns = @JoinColumn(name = "USER_ID"))
    @ManyToOne(optional = true)
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
    }

    public void addToPins(Pin pin)
    {
        this.pins.add(pin);
    }

    public void removeFromPins(Pin pin)
    {
        this.pins.remove(pin);
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

    public Set<Participant> getParticipants()
    {
        return participants;
    }

    public void setParticipants(Set<Participant> participants)
    {
        this.participants = participants;
    }

    public void addToParticipants(Participant participant)
    {
        this.participants.add(participant);
    }

    public void removeFromParticipants(Participant participant)
    {
        this.participants.remove(participant);
    }

    public Set<ConferenceHistory> getConferenceHistorys()
    {
        return conferenceHistorys;
    }

    public void setConferenceHistorys(Set<ConferenceHistory> conferenceHistorys)
    {
        this.conferenceHistorys = conferenceHistorys;
    }

    public void addToConferenceHistorys(ConferenceHistory conferenceHistory)
    {
        this.conferenceHistorys.add(conferenceHistory);
    }

    public void removeFromConferenceHistorys(ConferenceHistory conferenceHistory)
    {
        this.conferenceHistorys.remove(conferenceHistory);
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

        Set<Pin> newPins = new HashSet<Pin>();
        for(Pin pin : pins)
        {
            newPins.add(pin.copy(false));
        }
        copy.setPins(newPins);
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
