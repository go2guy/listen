package com.interact.listen.resource;

import com.interact.listen.util.ComparisonUtil;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

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
}
