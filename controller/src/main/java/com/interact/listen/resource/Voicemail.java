package com.interact.listen.resource;

import com.interact.listen.EmailerService;
import com.interact.listen.PersistenceService;
import com.interact.listen.history.HistoryService;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;
import com.interact.listen.stats.StatSenderFactory;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.*;

@Entity
public class Voicemail extends Audio implements Serializable
{
    private static final long serialVersionUID = 1L;

    @JoinColumn(name = "SUBSCRIBER_ID")
    @ManyToOne
    private Subscriber subscriber;

    @Column(name = "IS_NEW", nullable = true)
    private Boolean isNew = Boolean.TRUE;

    @Column(name = "LEFT_BY", nullable = true)
    private String leftBy;

    public Subscriber getSubscriber()
    {
        return subscriber;
    }

    public void setSubscriber(Subscriber subscriber)
    {
        this.subscriber = subscriber;
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
        copy.setIsNew(isNew);
        copy.setLeftBy(leftBy);
        copy.setSubscriber(subscriber);
        copy.setUri(getUri());
        return copy;
    }
    
    @Override
    public void afterSave(PersistenceService persistenceService)
    {
        EmailerService emailService = new EmailerService();
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
    }

    @Override
    public void afterDelete(PersistenceService persistenceService)
    {
        HistoryService historyService = new HistoryService(persistenceService);
        historyService.writeDeletedVoicemail(getSubscriber(), getLeftBy(), getDateCreated());
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
        criteria.setProjection(Projections.rowCount());
        return (Long)criteria.list().get(0);
    }
}
