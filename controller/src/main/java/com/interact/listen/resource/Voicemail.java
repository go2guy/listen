package com.interact.listen.resource;

import com.interact.listen.util.ComparisonUtil;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name = "VOICEMAIL")
public class Voicemail extends Resource implements Serializable
{
    private static final long serialVersionUID = 1L;

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "VERSION")
    @Version
    private Integer version = Integer.valueOf(0);

    @JoinColumn(name = "SUBSCRIBER_ID")
    @ManyToOne
    private Subscriber subscriber;

    @Column(name = "FILE_LOCATION", nullable = false)
    private String fileLocation;

    @Column(name = "DATE_CREATED", nullable = false)
    private Date dateCreated = new Date();

    @Column(name = "IS_NEW", nullable = false)
    private Boolean isNew = Boolean.TRUE;

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

    @Override
    public Voicemail copy(boolean withIdAndVersion)
    {
        Voicemail copy = new Voicemail();
        if(withIdAndVersion)
        {
            copy.setId(id);
            copy.setVersion(version);
        }

        copy.setDateCreated(dateCreated == null ? null : new Date(dateCreated.getTime()));
        copy.setFileLocation(fileLocation);
        copy.setIsNew(isNew);
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

        if(!(that instanceof Voicemail))
        {
            return false;
        }

        Voicemail voicemail = (Voicemail)that;

        if(!ComparisonUtil.isEqual(voicemail.getFileLocation(), getFileLocation()))
        {
            return false;
        }

        if(!ComparisonUtil.isEqual(voicemail.getSubscriber(), getSubscriber()))
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
        hash *= prime + (getFileLocation() == null ? 0 : getFileLocation().hashCode());
        hash *= prime + (getSubscriber() == null ? 0 : getSubscriber().hashCode());
        return hash;
    }
}
