package com.interact.listen.resource;

import com.interact.listen.PersistenceService;
import com.interact.listen.history.HistoryService;
import com.interact.listen.resource.Pin.PinType;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;
import com.interact.listen.stats.StatSenderFactory;
import com.interact.listen.util.ComparisonUtil;

import java.io.Serializable;
import java.util.*;

import javax.persistence.*;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.*;

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

    // TODO enforce unique description per Subscriber
    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

    @Column(name = "IS_STARTED", nullable = false)
    private Boolean isStarted = Boolean.FALSE;
    
    @Column(name = "IS_RECORDING", nullable = false)
    private Boolean isRecording = Boolean.FALSE;
    
    @Column(name = "START_TIME")
    private Date startTime;
    
    @Column(name = "ARCADE_ID")
    private String arcadeId;
    
    @Column(name = "RECORDING_SESSION_ID")
    private String recordingSessionId;

    @OneToMany(mappedBy = "conference", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE }, fetch = FetchType.EAGER)
    private Set<Pin> pins = new HashSet<Pin>();

    @OneToMany(mappedBy = "conference", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE }, fetch = FetchType.EAGER)
    private List<Participant> participants = new ArrayList<Participant>();

    @OneToMany(mappedBy = "conference", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE })
    private Set<ConferenceHistory> conferenceHistorys = new HashSet<ConferenceHistory>();

    @OneToMany(mappedBy = "conference", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE })
    private Set<ConferenceRecording> conferenceRecordings = new HashSet<ConferenceRecording>();

    @OneToMany(mappedBy = "conference", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE })
    private Set<ScheduledConference> scheduledConferences = new HashSet<ScheduledConference>();

    @JoinColumn(name = "SUBSCRIBER_ID")
    @ManyToOne
    private Subscriber subscriber;

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
    
    public String getArcadeId()
    {
        return arcadeId;
    }

    public void setArcadeId(String arcadeId)
    {
        this.arcadeId = arcadeId;
    }
    
    public String getRecordingSessionId()
    {
        return recordingSessionId;
    }

    public void setRecordingSessionId(String recordingSessionId)
    {
        this.recordingSessionId = recordingSessionId;
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
    
    public Set<ConferenceRecording> getConferenceRecordings()
    {
        return conferenceRecordings;
    }

    public void setConferenceRecordings(Set<ConferenceRecording> conferenceRecordings)
    {
        this.conferenceRecordings = conferenceRecordings;
    }

    public Set<ScheduledConference> getScheduledConferences()
    {
        return scheduledConferences;
    }

    public void setScheduledConferences(Set<ScheduledConference> scheduledConferences)
    {
        this.scheduledConferences = scheduledConferences;
    }

    public Subscriber getSubscriber()
    {
        return subscriber;
    }

    public void setSubscriber(Subscriber subscriber)
    {
        this.subscriber = subscriber;
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
        
        if(isStarted != null && isRecording != null && !isStarted && isRecording)
        {
            addToErrors("cannot record a conferences that is not started");
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
        copy.setConferenceRecordings(conferenceRecordings);
        copy.setIsStarted(isStarted);
        copy.setIsRecording(isRecording);
        copy.setParticipants(participants);

        for(Pin pin : pins)
        {
            copy.addToPins(pin.copy(false));
        }

        copy.setSubscriber(subscriber);

        return copy;
    }

    @Override
    public void afterSave(PersistenceService persistenceService)
    {
        StatSender statSender = StatSenderFactory.getStatSender();
        ConferenceHistory history = new ConferenceHistory();
        history.setConference(this);
        if(persistenceService.getCurrentSubscriber() != null)
        {
            if(persistenceService.getCurrentSubscriber() != null)
            {
                history.setSubscriber(persistenceService.getCurrentSubscriber().getUsername());
            }
        }
        history.setDescription("Conference created");
        persistenceService.save(history);
        
        if(isStarted.booleanValue())
        {
            startTime = new Date();
            statSender.send(Stat.CONFERENCE_START);
        }
        
        if(isRecording.booleanValue())
        {
            history = new ConferenceHistory();
            history.setConference(this);
            if(persistenceService.getCurrentSubscriber() != null)
            {
                history.setSubscriber(persistenceService.getCurrentSubscriber().getUsername());
            }
            history.setDescription("Conference recording started");

            persistenceService.save(history);
            statSender.send(Stat.CONFERENCE_RECORDING_START);
        }
    }

    @Override
    public void afterUpdate(PersistenceService persistenceService, Resource original)
    {
        HistoryService historyService = new HistoryService(persistenceService);

        StatSender statSender = StatSenderFactory.getStatSender();
        Conference originalConference = (Conference)original;
        if(isStarted.booleanValue() != originalConference.getIsStarted().booleanValue())
        {
            ConferenceHistory history = new ConferenceHistory();
            history.setConference(this);
            if(persistenceService.getCurrentSubscriber() != null)
            {
                history.setSubscriber(persistenceService.getCurrentSubscriber().getUsername());
            }
            history.setDescription("Conference " + (isStarted ? "started" : "ended"));
            persistenceService.save(history);
            
            if(isStarted)
            {
                historyService.writeStartedConference(getDescription());
            }
            else
            {
                historyService.writeStoppedConference(getDescription());
            }

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
                
                //Spot will have stopped any recordings, just covering our bases here
                isRecording = false;
            }
        }
        
        if(isRecording.booleanValue() != originalConference.getIsRecording().booleanValue())
        {
            ConferenceHistory history = new ConferenceHistory();
            history.setConference(this);
            if(persistenceService.getCurrentSubscriber() != null)
            {
                history.setSubscriber(persistenceService.getCurrentSubscriber().getUsername());
            }
            history.setDescription("Conference recording " + (isRecording ? "started" : "ended"));
            persistenceService.save(history);

            if(isRecording.booleanValue())
            {
                historyService.writeStartedRecordingConference(getDescription());
                statSender.send(Stat.CONFERENCE_RECORDING_START);
            }
            else
            {
                historyService.writeStoppedRecordingConference(getDescription());
                statSender.send(Stat.CONFERENCE_RECORDING_STOP);
            }
        }
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

        if(!ComparisonUtil.isEqual(conference.getSubscriber(), getSubscriber()))
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
        hash *= prime + (getSubscriber() == null ? 0 : getSubscriber().hashCode());
        return hash;
    }

    public static List<Conference> queryAllPaged(Session session, int first, int max)
    {
        DetachedCriteria subquery = DetachedCriteria.forClass(Conference.class);
        subquery.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        subquery.setProjection(Projections.id());

        Criteria criteria = session.createCriteria(Conference.class);
        criteria.add(Subqueries.propertyIn("id", subquery));
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);

        criteria.setFirstResult(first);
        criteria.setMaxResults(max);
        criteria.addOrder(Order.asc("description"));

        criteria.setFetchMode("subscriber", FetchMode.SELECT);
        criteria.setFetchMode("pins", FetchMode.SELECT);
        criteria.setFetchMode("participants", FetchMode.SELECT);
        criteria.setFetchMode("conferenceHistorys", FetchMode.SELECT);
        criteria.setFetchMode("conferenceRecordings", FetchMode.SELECT);

        return (List<Conference>)criteria.list();
    }

    public static Long count(Session session)
    {
        Criteria criteria = session.createCriteria(Conference.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.setProjection(Projections.rowCount());
        return (Long)criteria.list().get(0);
    }

    public String firstAdminSessionId(Session session)
    {
        String adminSessionId = Participant.queryConferenceAdminSessionId(session, this);
        if(adminSessionId == null)
        {
            // FIXME maybe use a checked exception here
            throw new IllegalStateException("Could not find Admin participant for Conference");
        }
        return adminSessionId;
    }

    public static Conference createNew(PersistenceService persistenceService, Subscriber forSubscriber)
    {
        Pin activePin = Pin.newRandomInstance(PinType.ACTIVE);
        Pin adminPin = Pin.newRandomInstance(PinType.ADMIN);
        Pin passivePin = Pin.newRandomInstance(PinType.PASSIVE);

        persistenceService.save(activePin);
        persistenceService.save(adminPin);
        persistenceService.save(passivePin);

        Conference conference = new Conference();
        conference.setDescription(forSubscriber.conferenceDescription());
        conference.setIsStarted(Boolean.FALSE);
        conference.setIsRecording(Boolean.FALSE);
        conference.addToPins(activePin);
        conference.addToPins(adminPin);
        conference.addToPins(passivePin);
        persistenceService.save(conference);

        forSubscriber.addToConferences(conference);
        return conference;
    }
}
