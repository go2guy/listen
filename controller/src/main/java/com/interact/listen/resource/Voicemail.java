package com.interact.listen.resource;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;

@Entity
public class Voicemail extends Resource implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Version
    private Integer version;

    @ManyToOne
    private Subscriber subscriber;

    @Column(nullable = false)
    private String fileLocation;

    @Column(nullable = false)
    private Date dateCreated = new Date();

    @Column(nullable = false)
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
        this.dateCreated = dateCreated == null ? null : new Date(dateCreated.getTime());
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
        boolean isValid = true;
        if(subscriber == null)
        {
            addToErrors("subscriber cannot be null");
            isValid = false;
        }

        if(fileLocation == null || fileLocation.trim().equals(""))
        {
            addToErrors("fileLocation cannot be null");
            isValid = false;
        }

        if(dateCreated == null)
        {
            addToErrors("dateCreated cannot be null");
            isValid = false;
        }

        if(isNew == null)
        {
            addToErrors("isNew cannot be null");
            isValid = false;
        }
        return isValid;
    }
}
