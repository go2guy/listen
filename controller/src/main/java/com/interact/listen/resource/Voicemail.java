package com.interact.listen.resource;

import com.interact.listen.EmailerService;
import com.interact.listen.PersistenceService;
import com.interact.listen.history.HistoryService;
import com.interact.listen.spot.SpotCommunicationException;
import com.interact.listen.spot.SpotSystem;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;
import com.interact.listen.stats.StatSenderFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import javax.persistence.*;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.*;

@Entity
public class Voicemail extends Audio implements Serializable
{
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(Voicemail.class);

    @JoinColumn(name = "FORWARDED_BY_SUBSCRIBER_ID", nullable = true)
    @ManyToOne
    private Subscriber forwardedBy;

    @Column(name = "IS_NEW", nullable = true)
    private Boolean isNew = Boolean.TRUE;

    @Column(name = "LEFT_BY", nullable = true)
    private String leftBy;

    @JoinColumn(name = "SUBSCRIBER_ID")
    @ManyToOne
    private Subscriber subscriber;

    public enum MessageLightState
    {
        ON, OFF;
    }

    public Subscriber getForwardedBy()
    {
        return forwardedBy;
    }

    public void setForwardedBy(Subscriber forwardedBy)
    {
        this.forwardedBy = forwardedBy;
    }

    public Boolean getIsNew()
    {
        return isNew;
    }

    public void setIsNew(Boolean isNew)
    {
        this.isNew = isNew;
    }

    public String getLeftBy()
    {
        return leftBy;
    }

    public void setLeftBy(String leftBy)
    {
        this.leftBy = leftBy;
    }

    public Subscriber getSubscriber()
    {
        return subscriber;
    }

    public void setSubscriber(Subscriber subscriber)
    {
        this.subscriber = subscriber;
    }

    @Override
    public boolean validate()
    {
        super.validate();

        if(subscriber == null)
        {
            addToErrors("subscriber cannot be null");
        }

        if(isNew == null)
        {
            addToErrors("isNew cannot be null");
        }

        return !hasErrors();
    }

    @Override
    public Voicemail copy(boolean withIdAndVersion)
    {
        Voicemail copy = new Voicemail();
        if(withIdAndVersion)
        {
            copy.setId(getId());
            copy.setVersion(getVersion());
        }

        copy.setDateCreated(getDateCreated() == null ? null : new Date(getDateCreated().getTime()));
        copy.setDescription(getDescription());
        copy.setDuration(getDuration());
        copy.setFileSize(getFileSize());
        copy.setForwardedBy(getForwardedBy());
        copy.setIsNew(isNew);
        copy.setLeftBy(leftBy);
        copy.setSubscriber(subscriber);
        copy.setUri(getUri());
        return copy;
    }
    
    @Override
    public void afterSave(PersistenceService persistenceService)
    {
        HistoryService historyService = new HistoryService(persistenceService);
        if(getForwardedBy() == null)
        {
            historyService.writeLeftVoicemail(this);
        }
        else
        {
            historyService.writeForwardedVoicemail(this);
        }

        EmailerService emailService = new EmailerService(persistenceService);
        StatSender statSender = StatSenderFactory.getStatSender();
        Subscriber voicemailSubscriber = (Subscriber)persistenceService.get(Subscriber.class, getSubscriber().getId());

        if(voicemailSubscriber.getIsEmailNotificationEnabled().booleanValue())
        {
            statSender.send(Stat.VOICEMAIL_EMAIL_NOTIFICATION);
            emailService.sendEmailVoicmailNotification(this, voicemailSubscriber);
        }
        
        if(voicemailSubscriber.getIsSmsNotificationEnabled().booleanValue())
        {
            statSender.send(Stat.VOICEMAIL_SMS_NOTIFICATION);
            emailService.sendSmsVoicemailNotification(this, voicemailSubscriber);
        }

        toggleMessageLight(persistenceService, getSubscriber());
    }

    @Override
    public void afterDelete(PersistenceService persistenceService)
    {
        HistoryService historyService = new HistoryService(persistenceService);
        historyService.writeDeletedVoicemail(this);

        // TODO we duplicate this looping code several places, it should be refactored
        List<ListenSpotSubscriber> spotSubscribers = ListenSpotSubscriber.list(persistenceService.getSession());
        for(ListenSpotSubscriber spotSubscriber : spotSubscribers)
        {
            SpotSystem spotSystem = new SpotSystem(spotSubscriber.getHttpApi(),
                                                   persistenceService.getCurrentSubscriber());
            try
            {
                spotSystem.deleteArtifact(getUri());
            }
            catch(SpotCommunicationException e)
            {
                LOG.error(e);
            }
            catch(IOException e)
            {
                LOG.error(e);
            }
        }

        toggleMessageLight(persistenceService, getSubscriber());
    }

    @Override
    public void afterUpdate(PersistenceService persistenceService, Resource original)
    {
        toggleMessageLight(persistenceService, getSubscriber());
    }

    public static Voicemail queryById(Session session, Long id)
    {
        return (Voicemail)session.get(Voicemail.class, id);
    }

    public static List<Voicemail> queryBySubscriberPaged(Session session, Subscriber subscriber, int first, int max)
    {
        DetachedCriteria subquery = DetachedCriteria.forClass(Voicemail.class);
        subquery.createAlias("subscriber", "subscriber_alias");
        subquery.add(Restrictions.eq("subscriber_alias.id", subscriber.getId()));
        subquery.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        subquery.setProjection(Projections.id());

        Criteria criteria = session.createCriteria(Voicemail.class);
        criteria.add(Subqueries.propertyIn("id", subquery));

        criteria.setFirstResult(first);
        criteria.setMaxResults(max);
        criteria.addOrder(Order.desc("dateCreated"));

        criteria.setFetchMode("subscriber", FetchMode.SELECT);
        criteria.setFetchMode("forwardedBy", FetchMode.SELECT);

        return (List<Voicemail>)criteria.list();
    }

    public static Long countBySubscriber(Session session, Subscriber subscriber)
    {
        Criteria criteria = session.createCriteria(Voicemail.class);
        criteria.setProjection(Projections.rowCount());
        criteria.createAlias("subscriber", "subscriber_alias");
        criteria.add(Restrictions.eq("subscriber_alias.id", subscriber.getId()));
        return (Long)criteria.list().get(0);
    }

    public static Long countNewBySubscriber(Session session, Subscriber subscriber)
    {
        Criteria criteria = session.createCriteria(Voicemail.class);
        criteria.add(Restrictions.eq("isNew", true));
        criteria.createAlias("subscriber", "subscriber_alias");
        criteria.add(Restrictions.eq("subscriber_alias.id", subscriber.getId()));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.setProjection(Projections.rowCount());
        return (Long)criteria.list().get(0);
    }

    public static List<Voicemail> queryNewVoicemailsBySubscriberList(Session session, List<Long> subscriberIds)
    {
        Criteria criteria = session.createCriteria(Voicemail.class);
        
        // only new records
        criteria.add(Restrictions.eq("isNew", true));

        // belonging to this subscriber
        criteria.createAlias("subscriber", "subscriber_alias");
        criteria.add(org.hibernate.criterion.Property.forName("subscriber_alias.id").in(subscriberIds));

        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return (ArrayList<Voicemail>)criteria.list();
    }

    public static void toggleMessageLight(PersistenceService persistenceService, AccessNumber accessNumber, MessageLightState state)
    {
        Set<SpotSystem> spotSystems = listSpotSystems(persistenceService);
        for(SpotSystem spotSystem : spotSystems)
        {
            try
            {
                if(state == MessageLightState.ON)
                {
                    spotSystem.turnMessageLightOn(accessNumber);
                }
                else
                {
                    spotSystem.turnMessageLightOff(accessNumber);
                }
            }
            catch(SpotCommunicationException e)
            {
                LOG.error(e);
            }
            catch(IOException e)
            {
                LOG.error(e);
            }
        }
    }

    public static void toggleMessageLight(PersistenceService persistenceService, AccessNumber accessNumber)
    {
        boolean hasNew = countNewBySubscriber(persistenceService.getSession(), accessNumber.getSubscriber()) > 0;
        toggleMessageLight(persistenceService, accessNumber, hasNew ? MessageLightState.ON : MessageLightState.OFF);
    }

    public static void toggleMessageLight(PersistenceService persistenceService, Subscriber subscriber)
    {
        Session session = persistenceService.getSession();
        List<AccessNumber> numbers = AccessNumber.queryBySubscriberWhereSupportsMessageLightTrue(session, subscriber);
        for(AccessNumber accessNumber : numbers)
        {
            toggleMessageLight(persistenceService, accessNumber);
        }
    }

    private static Set<SpotSystem> listSpotSystems(PersistenceService persistenceService)
    {
        List<ListenSpotSubscriber> spotSubscribers = ListenSpotSubscriber.list(persistenceService.getSession());
        Set<SpotSystem> spotSystems = new HashSet<SpotSystem>();
        for(ListenSpotSubscriber spotSubscriber : spotSubscribers)
        {
            spotSystems.add(new SpotSystem(spotSubscriber.getHttpApi(), persistenceService.getCurrentSubscriber()));
        }
        return spotSystems;
    }
}
