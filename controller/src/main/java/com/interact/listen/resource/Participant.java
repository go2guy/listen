package com.interact.listen.resource;

import java.io.Serializable;

import javax.persistence.*;

@Entity
public class Participant extends Resource implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Version
    private Integer version = Integer.valueOf(0);

    @Column(nullable = false, unique = true)
    private String number;

    @ManyToOne
    private Conference conference;

    @Column(nullable = false)
    private Boolean isAdmin;
    
    @Column(nullable = false)
    private Boolean isHolding;
    
    @Column(nullable = false)
    private Boolean isMuted;
    
    @Column(nullable = false)
    private Boolean isAdminMuted;
    
    @Column(nullable = false)
    private String audioResource;
    
    @Column(nullable = false)
    private String sessionID;

    public String getAudioResource()
    {
        return audioResource;
    }

    public void setAudioResource(String audioResource)
    {
        this.audioResource = audioResource;
    }

    public String getSessionID()
    {
        return sessionID;
    }

    public void setSessionID(String sessionID)
    {
        this.sessionID = sessionID;
    }

    public Boolean getIsAdmin()
    {
        return isAdmin;
    }

    public void setIsAdmin(Boolean admin)
    {
        this.isAdmin = admin;
    }

    public Boolean getIsHolding()
    {
        return isHolding;
    }

    public void setIsHolding(Boolean holding)
    {
        this.isHolding = holding;
    }

    public Boolean getIsMuted()
    {
        return isMuted;
    }

    public void setIsMuted(Boolean muted)
    {
        this.isMuted = muted;
    }
    
    public Boolean getIsAdminMuted()
    {
        return isAdminMuted;
    }

    public void setIsAdminMuted(Boolean adminMuted)
    {
        this.isAdminMuted = adminMuted;
    }

    public String getNumber()
    {
        return number;
    }

    public void setNumber(String number)
    {
        this.number = number;
    }

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

    public void setConference(Conference conference)
    {
        this.conference = conference;
    }

    public Conference getConference()
    {
        return conference;
    }

    public void setVersion(Integer version)
    {
        this.version = version;
    }

    @Override
    public void validate()
    {
        if(audioResource == null)
        {
            addToErrors("audioResource cannot be null");
        }

        if(conference == null)
        {
            addToErrors("conference cannot be null");
        }

        if(isAdmin == null)
        {
            addToErrors("isAdmin cannot be null");
        }

        if(isHolding == null)
        {
            addToErrors("isHolding cannot be null");
        }

        if(isMuted == null)
        {
            addToErrors("isMuted cannot be null");
        }
        
        if(isAdminMuted == null)
        {
            addToErrors("isAdminMuted cannot be null");
        }
        
        // Admin cannot be admin muted
        if(isAdmin != null && isAdminMuted != null && (isAdmin && isAdminMuted))
        {
            addToErrors("Admin participants cannot be muted by another Admin");
        }

        if(number == null || number.trim().equals(""))
        {
            addToErrors("Participant must have a number");
        }

        if(sessionID == null || sessionID.trim().equals(""))
        {
            addToErrors("Participant must have a sessionID");
        }
    }
}
