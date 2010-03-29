package com.interact.listen.resource;

import java.util.Date;

import javax.persistence.*;

@Entity
public class Voicemail implements Resource
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Version
    private Integer version;

    @ManyToOne
    private Subscriber subscriber;

    private String fileLocation;
    private Date dateCreated = new Date();
    private Boolean isNew = Boolean.TRUE;

    @Override
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

    public Subscriber getSubscriber()
    {
        return subscriber;
    }

    public void setSubscriber(Subscriber subscriber)
    {
        this.subscriber = subscriber;
    }

    public String getFileLocation()
    {
        return fileLocation;
    }

    public void setFileLocation(String fileLocation)
    {
        this.fileLocation = fileLocation;
    }

    public Date getDateCreated()
    {
        return dateCreated == null ? null : new Date(dateCreated.getTime());
    }

    public void setDateCreated(Date dateCreated)
    {
        this.dateCreated = (dateCreated == null ? null : new Date(dateCreated.getTime()));
    }

    public Boolean getIsNew()
    {
        return isNew;
    }

    public void setIsNew(Boolean isNew)
    {
        this.isNew = isNew;
    }

    @Override
    public boolean validate()
    {
        if(subscriber == null)
        {
            return false;
        }

        if(fileLocation == null || fileLocation.trim().equals(""))
        {
            return false;
        }

        if(dateCreated == null)
        {
            return false;
        }

        if(isNew == null)
        {
            return false;
        }

        return true;
    }
}
