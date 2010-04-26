package com.interact.listen.resource;

import com.interact.listen.PersistenceService;
import com.interact.listen.util.ComparisonUtil;

import java.io.Serializable;

import javax.persistence.*;

import org.hibernate.Session;

@Entity
public class Participant extends Resource implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Version
    private Integer version = Integer.valueOf(0);

    @Column(nullable = false)
    private String audioResource;

    @ManyToOne
    private Conference conference;

    @Column(nullable = false)
    private Boolean isAdmin;

    @Column(nullable = false)
    private Boolean isAdminMuted;

    @Column(nullable = false)
    private Boolean isMuted;

    @Column(nullable = false)
    private Boolean isPassive;

    @Column(nullable = false, unique = true)
    private String number;

    @Column(nullable = false)
    private String sessionID;

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

    public String getAudioResource()
    {
        return audioResource;
    }

    public void setAudioResource(String audioResource)
    {
        this.audioResource = audioResource;
    }

    public void setConference(Conference conference)
    {
        this.conference = conference;
    }

    public Conference getConference()
    {
        return conference;
    }

    public Boolean getIsAdmin()
    {
        return isAdmin;
    }

    public void setIsAdmin(Boolean admin)
    {
        this.isAdmin = admin;
    }

    public Boolean getIsAdminMuted()
    {
        return isAdminMuted;
    }

    public void setIsAdminMuted(Boolean adminMuted)
    {
        this.isAdminMuted = adminMuted;
    }

    public Boolean getIsMuted()
    {
        return isMuted;
    }

    public void setIsMuted(Boolean muted)
    {
        this.isMuted = muted;
    }

    public Boolean getIsPassive()
    {
        return isPassive;
    }

    public void setIsPassive(Boolean isPassive)
    {
        this.isPassive = isPassive;
    }

    public String getNumber()
    {
        return number;
    }

    public void setNumber(String number)
    {
        this.number = number;
    }

    public String getSessionID()
    {
        return sessionID;
    }

    public void setSessionID(String sessionID)
    {
        this.sessionID = sessionID;
    }

    @Override
    public boolean validate()
    {
        boolean isValid = true;

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

        if(isMuted == null)
        {
            addToErrors("isMuted cannot be null");
        }

        if(isAdminMuted == null)
        {
            addToErrors("isAdminMuted cannot be null");
        }

        if(isPassive == null)
        {
            addToErrors("isPassive cannot be null");
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

        return !hasErrors();
    }

    @Override
    public Participant copy(boolean withIdAndVersion)
    {
        Participant copy = new Participant();
        if(withIdAndVersion)
        {
            copy.setId(id);
            copy.setVersion(version);
        }

        copy.setAudioResource(audioResource);
        copy.setConference(conference);
        copy.setIsAdmin(isAdmin);
        copy.setIsAdminMuted(isAdminMuted);
        copy.setIsMuted(isMuted);
        copy.setIsPassive(isPassive);
        copy.setNumber(number);
        copy.setSessionID(sessionID);
        return copy;
    }

    @Override
    public void afterSave(Session session)
    {
        ConferenceHistory history = new ConferenceHistory();
        history.setConference(conference);
        history.setUser("Current User"); // FIXME
        history.setDescription(number + " joined");

        PersistenceService persistenceService = new PersistenceService(session);
        persistenceService.save(history);
    }

    @Override
    public void afterUpdate(Session session, Resource original)
    {
        Participant originalParticipant = (Participant)original;

        if(isAdminMuted.booleanValue() != originalParticipant.getIsAdminMuted().booleanValue())
        {
            ConferenceHistory history = new ConferenceHistory();
            history.setConference(conference);
            history.setUser("Current User"); // FIXME

            if(isAdminMuted)
            {
                history.setDescription(number + " was muted");
            }
            else
            {
                history.setDescription(number + " was unmuted");
            }

            PersistenceService persistenceService = new PersistenceService(session);
            persistenceService.save(history);
        }
    }

    @Override
    public void afterDelete(Session session)
    {
        ConferenceHistory history = new ConferenceHistory();
        history.setConference(conference);
        history.setUser("Current User"); // FIXME
        history.setDescription(number + " was dropped");

        PersistenceService persistenceService = new PersistenceService(session);
        persistenceService.save(history);
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

        if(!(that instanceof Participant))
        {
            return false;
        }

        Participant participant = (Participant)that;

        if(!ComparisonUtil.isEqual(participant.getSessionID(), getSessionID()))
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
        hash *= prime + (getSessionID() == null ? 0 : getSessionID().hashCode());
        return hash;
    }
}
