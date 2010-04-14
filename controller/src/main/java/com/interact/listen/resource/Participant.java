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
    public boolean validate()
    {
        boolean isValid = true;

        if(audioResource == null)
        {
            addToErrors("audioResource cannot be null");
            isValid = false;
        }

        if(conference == null)
        {
            addToErrors("conference cannot be null");
            isValid = false;
        }

        if(isAdmin == null)
        {
            addToErrors("isAdmin cannot be null");
            isValid = false;
        }

        if(isHolding == null)
        {
            addToErrors("isHolding cannot be null");
            isValid = false;
        }

        if(isMuted == null)
        {
            addToErrors("isMuted cannot be null");
            isValid = false;
        }

        if(isAdminMuted == null)
        {
            addToErrors("isAdminMuted cannot be null");
            isValid = false;
        }

        // Admin cannot be admin muted
        if(isAdmin != null && isAdminMuted != null && (isAdmin && isAdminMuted))
        {
            addToErrors("Admin participants cannot be muted by another Admin");
            isValid = false;
        }

        if(number == null || number.trim().equals(""))
        {
            addToErrors("Participant must have a number");
            isValid = false;
        }

        if(sessionID == null || sessionID.trim().equals(""))
        {
            addToErrors("Participant must have a sessionID");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Static factory method for copying a {@code Participant}. Note that this method <strong>does not</strong> copy the
     * {@code id} of the {@code Participant} being copied.
     * <p>
     * Developer note: this method is to avoid using {@link Object}'s {@code clone()} method, which is a broken
     * principle.
     * 
     * @param participant {@code Participant} to copy
     * @return new {@code Participant}
     */
    public static Participant copy(Participant participant)
    {
        Participant copy = new Participant();
        copy.setAudioResource(participant.audioResource);
        copy.setConference(participant.getConference());
        copy.setIsAdmin(participant.getIsAdmin());
        copy.setIsAdminMuted(participant.getIsAdminMuted());
        copy.setIsHolding(participant.getIsHolding());
        copy.setIsMuted(participant.getIsMuted());
        copy.setNumber(participant.getNumber());
        copy.setSessionID(participant.getSessionID());
        return copy;
    }
}
