package com.interact.listen.resource;

import com.interact.listen.util.ComparisonUtil;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "AUDIO")
public abstract class Audio extends Resource implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    @Column(name = "VERSION")
    @Version
    private Integer version = Integer.valueOf(0);

    @Column(name = "URI", nullable = false)
    private String uri;
    
    @Column(name = "DESCRIPTION", nullable = false)
    private String description;
    
    @Column(name = "FILE_SIZE", nullable = false)
    private String fileSize;

    @Column(name = "DATE_CREATED", nullable = false)
    private Date dateCreated = new Date();

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

    public void setUri(String uri)
    {
        this.uri = uri;
    }
    
    public String getUri()
    {
        return uri;
    }
    
    public void setDescription(String description)
    {
        this.description = description;
    }
    
    public String getDescription()
    {
        return description;
    }
    
    public void setFileSize(String fileSize)
    {
        this.fileSize = fileSize;
    }
    
    public String getFileSize()
    {
        return fileSize;
    }
    
    public Date getDateCreated()
    {
        return dateCreated == null ? null : new Date(dateCreated.getTime());
    }

    public void setDateCreated(Date dateCreated)
    {
        this.dateCreated = dateCreated == null ? null : new Date(dateCreated.getTime());
    }

    @Override
    public boolean validate()
    {
        if(uri == null || uri.trim().equals(""))
        {
            addToErrors("uri cannot be null or blank");
        }

        if(description == null || description.trim().equals(""))
        {
            addToErrors("description cannot be null or blank");
        }
        
        if(fileSize == null || fileSize.trim().equals(""))
        {
            addToErrors("fileSize cannot be null or blank");
        }

        if(dateCreated == null)
        {
            addToErrors("dateCreated cannot be null");
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

        if(that == null)
        {
            return false;
        }

        if(!(that instanceof Audio))
        {
            return false;
        }

        Audio audioResource = (Audio)that;

        if(!ComparisonUtil.isEqual(audioResource.getUri(), getUri()))
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
        hash *= prime + (getUri() == null ? 0 : getUri().hashCode());
        return hash;
    }
}
