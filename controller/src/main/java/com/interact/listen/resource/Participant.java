package com.interact.listen.resource;

import com.interact.listen.PersistenceService;
import com.interact.listen.history.HistoryService;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;
import com.interact.listen.stats.StatSenderFactory;
import com.interact.listen.util.ComparisonUtil;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.*;

@Entity
@Table(name = "participant")
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
    public void afterSave(PersistenceService persistenceService, HistoryService historyService)
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
    public void afterUpdate(PersistenceService persistenceService, HistoryService historyService, Resource original)
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
    public void afterDelete(PersistenceService persistenceService, HistoryService historyService)
    {
        ConferenceHistory history = new ConferenceHistory();
        history.setConference(conference);
        if(persistenceService.getCurrentSubscriber() != null)
        {
            history.setSubscriber(persistenceService.getCurrentSubscriber().getUsername());
        }
        history.setDescription(number + " was dropped");
        persistenceService.save(history);

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

    public static Participant queryById(Session session, Long id)
    {
        return (Participant)session.get(Participant.class, id);
    }

    public static List<Participant> queryByConferencePaged(Session session, Conference conference, int first, int max)
    {
        DetachedCriteria subquery = DetachedCriteria.forClass(Participant.class);
        subquery.createAlias("conference", "conference_alias");
        subquery.add(Restrictions.eq("conference_alias.id", conference.getId()));
        subquery.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        subquery.setProjection(Projections.id());

        Criteria criteria = session.createCriteria(Participant.class);
        criteria.add(Subqueries.propertyIn("id", subquery));

        criteria.setFirstResult(first);
        criteria.setMaxResults(max);
        // TODO it might be nice to order by join date, but that's not a field (yet?)
        criteria.addOrder(Order.asc("id"));

        criteria.setFetchMode("conference", FetchMode.SELECT);
        return (List<Participant>)criteria.list();
    }

    public static Long countByConference(Session session, Conference conference)
    {
        Criteria criteria = session.createCriteria(Participant.class);
        criteria.setProjection(Projections.rowCount());
        criteria.createAlias("conference", "conference_alias");
        criteria.add(Restrictions.eq("conference_alias.id", conference.getId()));
        return (Long)criteria.list().get(0);
    }

    /**
     * Searches for admin participants in the provided conference and returns the sessionID of the first one found. If
     * no admins are found, returns {@code null}.
     * 
     * @param session session
     * @param conference conference to search
     * @return first admin sessionID, or {@code null} if no admin found
     */
    public static String queryConferenceAdminSessionId(Session session, Conference conference)
    {
        org.hibernate.Query query = session.createQuery("select p.sessionID from Participant p where p.conference.id = :cid and p.isAdmin = true");
        query.setParameter("cid", conference.getId());
        List<String> result = (List<String>)query.list();

        return result.size() == 0 ? null : result.get(0);
    }
}
