package com.interact.listen.resource;

import java.io.Serializable;

import javax.persistence.*;

@Entity
public class ConferenceHistory extends Resource implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Version
    private Integer version = Integer.valueOf(0);

    @ManyToOne
    private Conference conference;

    @Column(nullable = false)
    private String user;

    @Column(nullable = false)
    private String description;

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
}
