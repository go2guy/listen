package com.interact.listen.resource;

import com.interact.listen.EmailerService;
import com.interact.listen.PersistenceService;
import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;
import com.interact.listen.util.ComparisonUtil;

import java.io.Serializable;
import java.util.*;

import javax.persistence.*;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.*;

@Entity
@Table(name = "SCHEDULED_CONFERENCE")
public class ScheduledConference extends Resource implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    @Column(name = "VERSION")
    @Version
    private Integer version = Integer.valueOf(0);

    @Column(name = "START_DATE", nullable = false)
    private Date startDate;

    @Column(name = "END_DATE", nullable = false)
    private Date endDate;

    @Column(name = "TOPIC")
    private String topic;

    @Column(name = "NOTES")
    private String notes;

    @JoinColumn(name = "CONFERENCE_ID", nullable = false)
    @ManyToOne
    private Conference conference;

    @JoinColumn(name = "SCHEDULED_BY_SUBSCRIBER_ID", nullable = false)
    @ManyToOne
    private Subscriber scheduledBy;

    @CollectionTable(name = "SCHEDULED_CONFERENCE_ACTIVE_CALLERS", joinColumns = @JoinColumn(name = "SCHEDULED_CONFERENCE_ID"))
    @Column(name = "EMAIL_ADDRESS")
    @ElementCollection
    private Set<String> activeCallers = new HashSet<String>();

    @CollectionTable(name = "SCHEDULED_CONFERENCE_PASSIVE_CALLERS", joinColumns = @JoinColumn(name = "SCHEDULED_CONFERENCE_ID"))
    @Column(name = "EMAIL_ADDRESS")
    @ElementCollection
    private Set<String> passiveCallers = new HashSet<String>();

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

    public Date getStartDate()
    {
        return startDate == null ? null : new Date(startDate.getTime());
    }

    public void setStartDate(Date startDate)
    {
        this.startDate = startDate == null ? null : new Date(startDate.getTime());
    }

    public Date getEndDate()
    {
        return endDate == null ? null : new Date(endDate.getTime());
    }

    public void setEndDate(Date endDate)
    {
        this.endDate = endDate == null ? null : new Date(endDate.getTime());
    }

    public String getTopic()
    {
        return topic;
    }

    public void setTopic(String topic)
    {
        this.topic = topic;
    }

    public String getNotes()
    {
        return notes;
    }

    public void setNotes(String notes)
    {
        this.notes = notes;
    }

    public Conference getConference()
    {
        return conference;
    }

    public void setConference(Conference conference)
    {
        this.conference = conference;
    }

    public Subscriber getScheduledBy()
    {
        return scheduledBy;
    }

    public void setScheduledBy(Subscriber scheduledBy)
    {
        this.scheduledBy = scheduledBy;
    }

    public Set<String> getActiveCallers()
    {
        return activeCallers;
    }

    public void setActiveCallers(Set<String> activeCallers)
    {
        this.activeCallers = activeCallers;
    }

    public Set<String> getPassiveCallers()
    {
        return passiveCallers;
    }

    public void setPassiveCallers(Set<String> passiveCallers)
    {
        this.passiveCallers = passiveCallers;
    }

    @Override
    public Resource copy(boolean withIdAndVersion)
    {
        ScheduledConference copy = new ScheduledConference();
        if(withIdAndVersion)
        {
            copy.setId(getId());
            copy.setVersion(getVersion());
        }

        copy.setActiveCallers(getActiveCallers());
        copy.setConference(getConference());
        copy.setEndDate(getEndDate());
        copy.setPassiveCallers(getPassiveCallers());
        copy.setStartDate(getStartDate());
        copy.setTopic(getTopic());
        copy.setNotes(getNotes());
        copy.setScheduledBy(getScheduledBy());
        return copy;
    }

    @Override
    public boolean validate()
    {
        if(startDate == null)
        {
            addToErrors("Start Date cannot be null");
        }

        if(endDate == null)
        {
            addToErrors("End Date cannot be null");
        }

        if(conference == null)
        {
            addToErrors("Conference cannot be null");
        }

        return !hasErrors();
    }

    @Override
    public boolean equals(Object that)
    {
        if(this == that)
        {
            return true;
        }

        if(!(that instanceof ScheduledConference))
        {
            return false;
        }

        final ScheduledConference scheduledConference = (ScheduledConference)that;

        if(!ComparisonUtil.isEqual(scheduledConference.getStartDate(), getStartDate()))
        {
            return false;
        }

        if(!ComparisonUtil.isEqual(scheduledConference.getConference(), getConference()))
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
        hash *= prime + (getStartDate() == null ? 0 : getStartDate().hashCode());
        hash *= prime + (getConference() == null ? 0 : getConference().hashCode());
        return hash;
    }

    public static List<ScheduledConference> queryByConferencePaged(Session session, Conference conference, int first, int max, boolean historic)
    {
        DetachedCriteria subquery = DetachedCriteria.forClass(ScheduledConference.class);
        subquery.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        subquery.setProjection(Projections.id());
        subquery.createAlias("conference", "conference_alias");
        subquery.add(Restrictions.eq("conference_alias.id", conference.getId()));

        Criteria criteria = session.createCriteria(ScheduledConference.class);
        criteria.add(Subqueries.propertyIn("id", subquery));
        if(historic)
        {
            criteria.add(Restrictions.lt("startDate", new Date()));
        }
        else
        {
            criteria.add(Restrictions.gt("startDate", new Date()));
        }

        criteria.setFirstResult(first);
        criteria.setMaxResults(max);
        criteria.addOrder(historic ? Order.desc("startDate") : Order.asc("startDate"));

        criteria.setFetchMode("conference", FetchMode.SELECT);
        criteria.setFetchMode("scheduledBy", FetchMode.SELECT);

        return (List<ScheduledConference>)criteria.list();
    }
    
    public static Long countByConference(Session session, Conference conference, boolean historic)
    {
        Criteria criteria = session.createCriteria(ScheduledConference.class);
        criteria.setProjection(Projections.rowCount());
        criteria.createAlias("conference", "conference_alias");
        criteria.add(Restrictions.eq("conference_alias.id", conference.getId()));
        if(historic)
        {
            criteria.add(Restrictions.lt("startDate", new Date()));
        }
        else
        {
            criteria.add(Restrictions.gt("startDate", new Date()));
        }
        return (Long)criteria.list().get(0);
    }

    public boolean sendEmails(PersistenceService persistenceService)
    {
        EmailerService emailService = new EmailerService(persistenceService);
        
        String phoneNumbers = Configuration.get(Property.Key.CONFERENCE_BRIDGES);

        if(phoneNumbers.equals(""))
        {
            // ***
            // Current implementation is a person has to have called into the spot system before the phone number is
            // available
            // We should still send the invites in my opinion
            // ***
            phoneNumbers = "Contact the conference administrator for access number";
        }

        boolean activeSuccess = true;
        boolean passiveSuccess = true;

        if(!activeCallers.isEmpty())
        {
            activeSuccess = emailService.sendScheduledConferenceActiveEmail(this, phoneNumbers);
        }

        if(!passiveCallers.isEmpty())
        {
            passiveSuccess = emailService.sendScheduledConferencePassiveEmail(this, phoneNumbers);
        }

        return activeSuccess && passiveSuccess;
    }
}
