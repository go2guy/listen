package com.interact.listen.android;

import com.interact.insa.client.StatId;
import com.interact.listen.android.DeviceRegistration;
import com.interact.listen.android.DeviceRegistration.DeviceType;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

class RetryC2DM implements Runnable // can be the first time too if queuing the first attempt message
{
    public static final int MAX_RETRIES = 3;

    private static final Logger LOG = Logger.getLogger(RetryC2DM.class);
    
    private final C2DMessaging messaging;
    private final C2DMessage message;

    private int retryCount;
    private String useToken;
    
    public RetryC2DM(C2DMessaging messaging, C2DMessage message, boolean withRetry, String useToken)
    {
        this.messaging = messaging;
        this.message = message;
        this.retryCount = withRetry ? 0 : Integer.MAX_VALUE;
        this.useToken = useToken;
    }

    @Override
    public void run()
    {
        messaging.clearFuture(message);
        
        if(retryCount == 0 || retryCount == Integer.MAX_VALUE)
        {
            messaging.writeStat(StatId.LISTEN_C2DM_QUEUED_MESSAGE);
        }
        try
        {
            C2DError error = messaging.send(message, useToken);
            if(error == null)
            {
                messaging.writeStat(StatId.LISTEN_C2DM_SENT_SUCCESFULLY);
                LOG.info("sent message succesfully");
            }
            else
            {
                messaging.writeStat(error.getStatId());
                LOG.info("Error result " + error + " for " + message.getRegistrationId());
                if(error.isRetryable())
                {
                    if(++retryCount >= MAX_RETRIES)
                    {
                        messaging.writeStat(StatId.LISTEN_C2DM_DISCARD_DUE_TO_RETRYS);
                        LOG.error("Maximum retries reached: " + message.getRegistrationId());
                    }
                    else
                    {
                        messaging.writeStat(StatId.LISTEN_C2DM_QUEUED_RETRY);
                        messaging.scheduleRetry(this, retryCount);
                    }
                }
                else if(error.isDeviceInvalid())
                {
                    messaging.deleteRegistration(message.getRegistrationId());
                }
            }
        }
        catch(IOException e)
        {
            messaging.writeStat(StatId.LISTEN_C2DM_UNKNOWN_ERROR);
            LOG.error("un-retryable error: + " + message.getRegistrationId(), e);
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

    public C2DMessage getMessage()
    {
        return message;
    }
}
