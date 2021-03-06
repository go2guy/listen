package com.interact.listen.resource;

import com.interact.listen.util.ComparisonUtil;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name = "conference_history")
public class ConferenceHistory extends Resource implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    @Column(name = "VERSION")
    @Version
    private Integer version = Integer.valueOf(0);

    @Column(name = "DATE_CREATED")
    private Date dateCreated = new Date();

    @JoinColumn(name = "CONFERENCE_ID")
    @ManyToOne
    private Conference conference;

    @Column(name = "SUBSCRIBER", nullable = true)
    private String subscriber;

    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

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

    public Date getDateCreated()
    {
        return dateCreated == null ? null : new Date(dateCreated.getTime());
    }

    public void setDateCreated(Date dateCreated)
    {
        this.dateCreated = dateCreated == null ? null : new Date(dateCreated.getTime());
    }

    public Conference getConference()
    {
        return conference;
    }

    public void setConference(Conference conference)
    {
        this.conference = conference;
    }

    public String getSubscriber()
    {
        return subscriber;
    }

    public void setSubscriber(String subscriber)
    {
        this.subscriber = subscriber;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    @Override
    public boolean validate()
    {
        boolean isValid = true;
        if(conference == null)
        {
            addToErrors("conference cannot be null");
            isValid = false;
        }

        if(subscriber == null || subscriber.trim().equals(""))
        {
            addToErrors("subscriber cannot be null or blank");
            isValid = false;
        }

        if(description == null || description.trim().equals(""))
        {
            addToErrors("description cannot be null or blank");
            isValid = false;
        }

        return isValid;
    }

    @Override
    public ConferenceHistory copy(boolean withIdAndVersion)
    {
        ConferenceHistory copy = new ConferenceHistory();
        if(withIdAndVersion)
        {
            copy.setId(id);
            copy.setVersion(version);
        }

        copy.setConference(conference);
        copy.setDateCreated(dateCreated == null ? null : new Date(dateCreated.getTime()));
        copy.setDescription(description);
        copy.setSubscriber(subscriber);
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

        if(!(that instanceof ConferenceHistory))
        {
            return false;
        }

        ConferenceHistory conferenceHistory = (ConferenceHistory)that;

        if(!ComparisonUtil.isEqual(conferenceHistory.getConference(), getConference()))
        {
            return false;
        }

        if(!ComparisonUtil.isEqual(conferenceHistory.getDateCreated(), getDateCreated()))
        {
            return false;
        }

        if(!ComparisonUtil.isEqual(conferenceHistory.getDescription(), getDescription()))
        {
            return false;
        }

        if(!ComparisonUtil.isEqual(conferenceHistory.getSubscriber(), getSubscriber()))
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
        hash *= prime + (getConference() == null ? 0 : getConference().hashCode());
        hash *= prime + (getDateCreated() == null ? 0 : getDateCreated().hashCode());
        hash *= prime + (getDescription() == null ? 0 : getDescription().hashCode());
        hash *= prime + (getSubscriber() == null ? 0 : getSubscriber().hashCode());
        return hash;
    }
}
