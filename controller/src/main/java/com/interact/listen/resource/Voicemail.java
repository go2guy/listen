package com.interact.listen.resource;

import com.interact.listen.EmailerService;
import com.interact.listen.PersistenceService;
import com.interact.listen.history.HistoryService;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;
import com.interact.listen.stats.StatSenderFactory;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name = "VOICEMAIL")
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
}
