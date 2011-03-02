package com.interact.listen.jobs;

import com.interact.listen.EmailerService;
import com.interact.listen.HibernateUtil;
import com.interact.listen.resource.FindMeNumber;
import com.interact.listen.resource.Subscriber;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

public class FindMeExpirationReminderJob implements Job
{
    private static final Logger LOG = Logger.getLogger(FindMeExpirationReminderJob.class);

    private EmailerService emailerService = new EmailerService();

    @Override
    public void execute(JobExecutionContext context)
    {
        LOG.debug("FindMeExpirationReminderJob is executing");

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();

        LocalDateTime now = new LocalDateTime().withSecondOfMinute(0).withMillisOfSecond(0);
        LocalDateTime start = now.plusMinutes(30);
        LocalDateTime end = now.plusMinutes(60);

        List<Subscriber> subscribers = Subscriber.queryAll(session);
        for(Subscriber subscriber : subscribers)
        {
            LOG.debug("Checking to see if subscriber [" + subscriber.getUsername() +
                      "] should be reminded about expiring Find Me");

            // do they have reminders enabled?
            String destination = subscriber.getFindMeReminderDestination();
            if(!subscriber.getSendFindMeReminder() || destination == null || destination.trim().equals(""))
            {
                LOG.debug("Subscriber [" + subscriber.getUsername() + "] with destination [" + destination +
                          "] does not have Find Me expiration reminder enabled, not reminding");
                continue;
            }

            // do they have any active find me numbers?
            Long findMeNumbers = FindMeNumber.countEnabledBySubscriber(session, subscriber);
            if(findMeNumbers == 0)
            {
                LOG.debug("Subscriber [" + subscriber.getUsername() + "] has no active Find Me numbers, not reminding");
                continue; // no active find me numbers, so expiration is irrelevant
            }

            // do they have a null Find Me expiration date?
            if(subscriber.getFindMeExpiration() == null)
            {
                LOG.debug("Subscriber [" + subscriber.getUsername() +
                          "] has a null Find Me expiration date, not reminding)");
                continue; // no expiration, no reminder. derp.
            }

            // is their Find Me expiration date in the reminder window that we're currently checking?
            LocalDateTime expires = LocalDateTime.fromDateFields(subscriber.getFindMeExpiration());
            if(expires.isBefore(start) || expires.isAfter(end.minusMillis(1)))
            {
                LOG.debug("Subscriber [" + subscriber.getUsername() + "]'s Find Me expiration [" +
                          subscriber.getFindMeExpiration() +
                          "] is outside of the reminder window for this iteration, not reminding");
                continue;
            }

            // all conditions met, send the reminder
            int minutes = new Period(now, expires).toStandardMinutes().getMinutes();
            String message = "Your Find Me / Follow Me configuration expires in " + minutes + " minutes";
            LOG.debug("Sending reminder to subscriber [" + subscriber.getUsername() + "]: [" + message + "]");

            emailerService.sendSms(destination, message);
        }

        tx.commit();
    }
}
