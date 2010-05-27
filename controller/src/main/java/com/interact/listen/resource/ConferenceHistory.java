package com.interact.listen.resource;

import com.interact.listen.util.ComparisonUtil;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name = "CONFERENCE_HISTORY")
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

    @ManyToOne
    private Conference conference;

    @Column(name = "USER", nullable = false)
    private String user;

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

    public String getUser()
    {
        return user;
    }

    public void setUser(String user)
    {
        this.user = user;
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

        if(user == null || user.trim().equals(""))
        {
            addToErrors("user cannot be null or blank");
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
        copy.setUser(user);
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

        if(!ComparisonUtil.isEqual(conferenceHistory.getUser(), getUser()))
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
        hash *= prime + (getUser() == null ? 0 : getUser().hashCode());
        return hash;
    }
}
