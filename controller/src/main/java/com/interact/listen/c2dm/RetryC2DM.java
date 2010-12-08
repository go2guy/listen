package com.interact.listen.c2dm;

import com.interact.listen.HibernateUtil;
import com.interact.listen.resource.DeviceRegistration;
import com.interact.listen.resource.DeviceRegistration.DeviceType;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;
import com.interact.listen.stats.StatSenderFactory;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

class RetryC2DM implements Runnable // can be the first time too if queuing the first attempt message
{
    public static final int MAX_RETRIES = 3;

    private static final Logger LOG = Logger.getLogger(RetryC2DM.class);

    private static final StatSender statSender = StatSenderFactory.getStatSender();
    
    private final C2DMessage message;

    private int retryCount;
    private String useToken;
    
    public RetryC2DM(C2DMessage message, boolean withRetry, String useToken)
    {
        this.message = message;
        this.retryCount = withRetry ? 0 : Integer.MAX_VALUE;
        this.useToken = useToken;
    }
    
    @Override
    public void run()
    {
        if(retryCount == 0 || retryCount == Integer.MAX_VALUE)
        {
            statSender.send(Stat.C2DM_QUEUED_MESSAGE);
        }
        try
        {
            C2DError error = C2DSender.send(message, useToken);
            if(error == null)
            {
                statSender.send(Stat.C2DM_SENT_SUCCESFULLY);
                LOG.info("sent message succesfully");
            }
            else
            {
                statSender.send(error.getStat());
                LOG.info("Error result " + error + " for " + message.getRegistrationId());
                if(error.isRetryable())
                {
                    if(++retryCount >= MAX_RETRIES)
                    {
                        statSender.send(Stat.C2DM_DISCARD_DUE_TO_RETRYS);
                        LOG.error("Maximum retries reached: " + message.getRegistrationId());
                    }
                    else
                    {
                        statSender.send(Stat.C2DM_QUEUED_RETRY);
                        C2DMessaging.scheduleRetry(this, retryCount);
                    }
                }
                else if(error.isDeviceInvalid())
                {
                    deleteRegistration(message.getRegistrationId());
                }
            }
        }
        catch(IOException e)
        {
            statSender.send(Stat.C2DM_UNKNOWN_ERROR);
            LOG.error("un-retryable error: + " + message.getRegistrationId(), e);
        }
    }

    private static void deleteRegistration(String registrationId)
    {
        LOG.info("request to delete registrations for: " + registrationId);
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        try
        {
            transaction = session.beginTransaction();

            java.util.List<DeviceRegistration> devices;
            devices = DeviceRegistration.queryByDevice(session, DeviceType.ANDROID, registrationId);

            LOG.info("Removing device registrations: " + devices.size());

            for (DeviceRegistration device : devices)
            {
                session.delete(device);
            }

            transaction.commit();
        }
        catch(Throwable t) // SUPPRESS CHECKSTYLE IllegalCatchCheck
        {
            LOG.error(t);
            if(transaction != null)
            {
                transaction.rollback();
            }
        }
        finally
        {
            session.clear();
        }
    }
    
    @Override
    public boolean equals(Object obj)
    {
        return (obj instanceof RetryC2DM) ? message.equals(((RetryC2DM)obj).message) : false;
    }
    
    @Override
    public int hashCode()
    {
        return message.hashCode();
    }

}
