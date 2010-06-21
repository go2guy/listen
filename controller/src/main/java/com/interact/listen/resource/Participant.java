package com.interact.listen.resource;

import com.interact.listen.PersistenceService;
import com.interact.listen.history.HistoryService;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;
import com.interact.listen.stats.StatSenderFactory;
import com.interact.listen.util.ComparisonUtil;

import java.io.Serializable;

import javax.persistence.*;

@Entity
@Table(name = "PARTICIPANT")
public class Participant extends Resource implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    @Column(name = "VERSION")
    @Version
    private Integer version = Integer.valueOf(0);

    @Column(name = "AUDIO_RESOURCE", nullable = false)
    private String audioResource;

    @JoinColumn(name = "CONFERENCE_ID")
    @ManyToOne
    private Conference conference;

    @Column(name = "IS_ADMIN", nullable = false)
    private Boolean isAdmin;

    @Column(name = "IS_ADMIN_MUTED", nullable = false)
    private Boolean isAdminMuted;

    @Column(name = "IS_MUTED", nullable = false)
    private Boolean isMuted;

    @Column(name = "IS_PASSIVE", nullable = false)
    private Boolean isPassive;

    @Column(name = "NUMBER", nullable = false, unique = true)
    private String number;

    @Column(name = "SESSION_ID", nullable = false)
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
    public void afterSave(PersistenceService persistenceService)
    {
        StatSender statSender = StatSenderFactory.getStatSender();
        ConferenceHistory history = new ConferenceHistory();
        history.setConference(conference);
        if(persistenceService.getCurrentSubscriber() != null)
        {
            history.setSubscriber(persistenceService.getCurrentSubscriber().getUsername());
        }
        history.setDescription(number + " joined");
        persistenceService.save(history);
        
        if(isAdmin)
        {
            statSender.send(Stat.ADMIN_PARTICIPANT_JOIN);
        }
        else if(isPassive)
        {
            statSender.send(Stat.PASSIVE_PARTICIPANT_JOIN);
        }
        else
        {
            statSender.send(Stat.ACTIVE_PARTICIPANT_JOIN);
        }
    }

    @Override
    public void afterUpdate(PersistenceService persistenceService, Resource original)
    {
        Participant originalParticipant = (Participant)original;

        if(isAdminMuted.booleanValue() != originalParticipant.getIsAdminMuted().booleanValue())
        {
            ConferenceHistory history = new ConferenceHistory();
            history.setConference(conference);
            if(persistenceService.getCurrentSubscriber() != null)
            {
                history.setSubscriber(persistenceService.getCurrentSubscriber().getUsername());
            }

            if(isAdminMuted)
            {
                history.setDescription(number + " was placed in passive mode");
            }
            else
            {
                history.setDescription(number + " was placed in active mode");
            }
            persistenceService.save(history);

            HistoryService historyService = new HistoryService(persistenceService);
            if(isAdminMuted)
            {
                historyService.writeMutedConferenceCaller(getNumber(), getConference().getDescription());
            }
            else
            {
                historyService.writeUnmutedConferenceCaller(getNumber(), getConference().getDescription());
            }
        }
    }

    @Override
    public void afterDelete(PersistenceService persistenceService)
    {
        ConferenceHistory history = new ConferenceHistory();
        history.setConference(conference);
        if(persistenceService.getCurrentSubscriber() != null)
        {
            history.setSubscriber(persistenceService.getCurrentSubscriber().getUsername());
        }
        history.setDescription(number + " was dropped");
        persistenceService.save(history);

        HistoryService historyService = new HistoryService(persistenceService);
        historyService.writeDroppedConferenceCaller(getNumber(), getConference().getDescription());
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
