package com.interact.listen.resource;

import com.interact.listen.PersistenceService;
import com.interact.listen.spot.SpotCommunicationException;
import com.interact.listen.spot.SpotSystem;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.persistence.*;

import org.hibernate.Criteria;
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
    public void beforeUpdate(Session session, Resource original) throws PersistenceException
    {
        Participant originalParticipant = (Participant)original;

        try
        {
            if(isAdminMuted.booleanValue() != originalParticipant.getIsAdminMuted().booleanValue())
            {
                List<ListenSpotSubscriber> spotSubscribers = getSpotSubscribers(session);
                for(ListenSpotSubscriber spotSubscriber : spotSubscribers)
                {
                    SpotSystem spotSystem = new SpotSystem(spotSubscriber.getHttpApi());
                    if(isAdminMuted.booleanValue())
                    {
                        spotSystem.muteParticipant(this);
                    }
                    else
                    {
                        spotSystem.unmuteParticipant(this);
                    }
                }

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
        catch(SpotCommunicationException e)
        {
            throw new PersistenceException(e);
        }
        catch(IOException e)
        {
            throw new PersistenceException(e);
        }
    }

    public void beforeDelete(Session session) throws PersistenceException
    {
        try
        {
            // FIXME what happens when the first one succeeds and the second one fails? do we "rollback" the first one?
            // there's no way we can do it with 100% reliability (because the "rollback" might fail, too)
            // - in all likelihood there will only be one Spot subscriber, but we should accommodate many
            List<ListenSpotSubscriber> spotSubscribers = getSpotSubscribers(session);
            for(ListenSpotSubscriber spotSubscriber : spotSubscribers)
            {
                SpotSystem spotSystem = new SpotSystem(spotSubscriber.getHttpApi());
                spotSystem.dropParticipant(this);
            }

            ConferenceHistory history = new ConferenceHistory();
            history.setConference(conference);
            history.setUser("Current User"); // FIXME
            history.setDescription(number + " was dropped");

            PersistenceService persistenceService = new PersistenceService(session);
            persistenceService.save(history);
        }
        catch(SpotCommunicationException e)
        {
            throw new PersistenceException(e);
        }
        catch(IOException e)
        {
            throw new PersistenceException(e);
        }
    }

    private List<ListenSpotSubscriber> getSpotSubscribers(Session session)
    {
        Criteria criteria = session.createCriteria(ListenSpotSubscriber.class);
        return criteria.list();
    }
}
