package com.interact.listen.jobs;

import com.interact.listen.EmailerService;
import com.interact.listen.HibernateUtil;
import com.interact.listen.PersistenceService;
import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;
import com.interact.listen.history.Channel;
import com.interact.listen.history.HistoryService;
import com.interact.listen.resource.AccessNumber;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.Voicemail;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;
import com.interact.listen.stats.StatSenderFactory;

import java.util.*;

import org.apache.log4j.Logger;
import org.hibernate.*;
import org.joda.time.DateTime;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class NewVoicemailPagerJob implements Job
{
    private static final Logger LOG = Logger.getLogger(NewVoicemailPagerJob.class);
    private static Map<Long, DateTime> voicemailPagingStatus = new HashMap<Long, DateTime>();
    
    private EmailerService emailerService = new EmailerService();
    private StatSender statSender = StatSenderFactory.getStatSender();
    
    public void execute(JobExecutionContext arg0) throws JobExecutionException
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        PersistenceService persistenceService = new PersistenceService(session, null, Channel.AUTO);
        HistoryService historyService = new HistoryService(persistenceService);
        
        // We need the 'pagerSubscriber' so we know when to send pages to the alternate number, null if one doesn't exist
        String pagerNumber = Configuration.get(Property.Key.PAGER_NUMBER);
        Subscriber pagerSubscriber = null;
        
        try
        {
            pagerSubscriber = AccessNumber.queryByNumber(session, pagerNumber).getSubscriber();
        }
        catch(NullPointerException e)
        {
            LOG.info("No subscriber is currently setup as the after-hours pager subscriber");
        }
        
        List<Subscriber> subscribers = Subscriber.queryPagingEnabledSubscribers(session);
        List<Long> subscriberIds = new ArrayList<Long>();
        
        LOG.debug("Processing pages for " + subscribers.size() + " subscribers");
        
        for(Subscriber subscriber : subscribers)
        {
            subscriberIds.add(subscriber.getId());
        }
        
        if(subscribers.size() > 0)
        {
            List<Voicemail> newVoicemails = Voicemail.queryNewVoicemailsBySubscriberList(session, subscriberIds);
            
            LOG.debug("Sending pages for  " + newVoicemails.size() + " new voicemails");
            
            //Get concurrent modification errors when trying to iterate over a map made by the keyset.  Using this as a workaround
            Set<Long> voicemailIds = new HashSet<Long>();
            for(Long voicemailId : voicemailPagingStatus.keySet())
            {
                voicemailIds.add(voicemailId);
            }
            
            Set<Long> newVoicemailIds = getVoicemailIds(newVoicemails);
            
            //make this nine minutes ago since the job runs every minute.  This should keep it on 10 minute intervals
            DateTime nineMinutesAgo = new DateTime().minusMinutes(9);
            
            for(Voicemail newVoicemail : newVoicemails)
            {
                if(voicemailIds.contains(newVoicemail.getId()))
                {
                    //The voicemail has been paged about before, check if it's been 10 mins since the last one
                    DateTime lastPage = voicemailPagingStatus.get(newVoicemail.getId());
                    
                    //Need the isAfterNow check for daylight savings changes
                    if(lastPage.isBefore(nineMinutesAgo) || lastPage.isAfterNow())
                    {
                        sendPage(newVoicemail, newVoicemail.getSubscriber(), pagerSubscriber, historyService);
                        voicemailPagingStatus.put(newVoicemail.getId(), new DateTime());
                    }
                }
                else
                {
                    //This is a new voicemail that hasn't been paged for yet, page for it and add it to the paging status map
                    sendPage(newVoicemail, newVoicemail.getSubscriber(), pagerSubscriber, historyService);
                    voicemailPagingStatus.put(newVoicemail.getId(), new DateTime());
                }
            }
            
            for(Long voicemailIdToRemove : voicemailIds)
            {
                //if paging status map has a voicemail that isn't in the list of new voicemails, that means it's not new anymore and 
                //does not need to be tracked any longer
                if(!newVoicemailIds.contains(voicemailIdToRemove))
                {
                    voicemailPagingStatus.remove(voicemailIdToRemove);
                }
            }
        }
        
        tx.commit();
    }
    
    private Set<Long> getVoicemailIds(List<Voicemail> voicemails)
    {
        Set<Long> voicemailIds = new HashSet<Long>();
        
        for(Voicemail voicemail : voicemails)
        {
            voicemailIds.add(voicemail.getId());
        }
        
        return voicemailIds;
    }
    
    private void sendPage(Voicemail voicemail, Subscriber subscriber, Subscriber pagerSubscriber, HistoryService historyService)
    {
        emailerService.sendSmsVoicemailNotification(voicemail, subscriber);
        statSender.send(Stat.VOICEMAIL_PAGE_SENT);
        
        historyService.writeSentVoicemailPage(voicemail);
        
        // if this is a page to the after hours support pager, send a page to the alternate number as well
        if(pagerSubscriber != null && subscriber.equals(pagerSubscriber))
        {
            if(!Configuration.get(Property.Key.ALTERNATE_NUMBER).trim().equals(""))
            {
                emailerService.sendAlternateNumberSmsVoicemailNotification(voicemail);
                statSender.send(Stat.VOICEMAIL_ALTERNATE_NUMBER_PAGE_SENT);
            }
        }
    }
}
