package com.interact.listen.c2dm;

import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;
import com.interact.listen.resource.*;
import com.interact.listen.resource.DeviceRegistration.DeviceType;
import com.interact.listen.resource.DeviceRegistration.RegisteredType;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.hibernate.Session;

public enum C2DMessaging
{
    INSTANCE;

    public enum Type
    {
        SYNC_VOICEMAILS("sync-voicemails", "0-", RegisteredType.VOICEMAIL),
        SYNC_CONTACTS("sync-contacts", "2-", RegisteredType.CONTACTS),
        SYNC_CONFIG_CHANGED("sync-config", "1-", null);
        
        private final String message;
        private final String keyAppend;
        private final RegisteredType rType;
        
        private Type(String message, String keyAppend, RegisteredType rType)
        {
            this.message = message;
            this.keyAppend = keyAppend;
            this.rType = rType;
        }
        
        public String getMessage()
        {
            return message;
        }

        public String getCollapseKey(String username)
        {
            return keyAppend + Long.toHexString(username.hashCode());
        }
        
        public RegisteredType getRegisteredType()
        {
            return rType;
        }
    }
    
    private static final Logger LOG = Logger.getLogger(C2DMessaging.class);

    private final transient ScheduledThreadPoolExecutor threadPool = new ScheduledThreadPoolExecutor(1);
    private final transient Map< C2DMessage, ScheduledFuture<?> > futures =
        Collections.synchronizedMap(new WeakHashMap< C2DMessage, ScheduledFuture<?> >());
    
    private transient Boolean currentEnabled = null;

    public synchronized void setEnabled(boolean enabled)
    {
        Configuration.set(Property.Key.ANDROID_C2DM_ENABLED, Boolean.valueOf(enabled).toString());
        currentEnabled = enabled;
    }
    
    public synchronized boolean isEnabled()
    {
        if(currentEnabled == null)
        {
            String value = Configuration.get(Property.Key.ANDROID_C2DM_ENABLED);
            currentEnabled = Boolean.valueOf(value);
        }
        return currentEnabled;
    }

    public void enqueueAllSyncMessages(Session session, DeviceType dType, Type type, String useToken)
    {
        if(session == null)
        {
            LOG.error("session null executing enqueueAllSyncMessages");
            return;
        }
        List<DeviceRegistration> devices = DeviceRegistration.queryByDeviceType(session, dType);

        LOG.info("Sending " + type + " change to all devices, " + devices.size() + ", registered with " + type.getRegisteredType());
        
        for(DeviceRegistration device : devices)
        {
            if(device.getRegistrationToken() == null || device.getRegistrationToken().length() == 0 ||
                (type.getRegisteredType() != null && !device.isRegistered(dType, type.getRegisteredType())))
            {
                LOG.info("skipping device: " + device.getDeviceId());
                continue;
            }

            String username = device.getSubscriber().getUsername();
            LOG.info("queueing " + type + " change for " + username + " '" + device.getRegistrationToken() + "'");

            String collapseKey = type.getCollapseKey(username);
            Map<String, String[]> params = createParamMap(username, type);

            C2DMessage message = new C2DMessage(device.getRegistrationToken(), collapseKey, params, true);
            queue(message, false, useToken);
        }
    }

    public void enqueueDeviceSyncMessage(Session session, Subscriber subscriber, Type type, String notToDeviceId)
    {
        enqueueDeviceSyncMessage(subscriber, type, null, notToDeviceId);
    }

    private static Map<String, String[]> createParamMap(String username, Type type)
    {
        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("data.account_name", new String[] {username});
        params.put("data.message", new String[] {type.getMessage()});

        return params;
    }
    
    private void enqueueDeviceSyncMessage(Subscriber subscriber, Type type,
                                          String useToken, String notToDeviceId)
    {
        final boolean force = type == Type.SYNC_CONFIG_CHANGED;     // force config changes even if disabled
        final boolean withRetry = type != Type.SYNC_CONFIG_CHANGED; // don't bother with retries for config changes

        if(!force && !isEnabled())
        {
            LOG.debug("C2DM is not enabled");
            return;
        }

        if(subscriber == null)
        {
            LOG.error("C2DM can't be sent: subscriber unknown");
            return;
        }
        
        String username = subscriber.getUsername();
        if(username == null)
        {
            LOG.error("subscriber username is null");
            return;
        }
        Set<DeviceRegistration> devices = subscriber.getDevices();
        if(devices == null || devices.isEmpty())
        {
            LOG.debug(username + " has no devices");
            return;
        }
        
        String collapseKey = type.getCollapseKey(username);
        Map<String, String[]> params = createParamMap(username, type);

        int numDeviceMessages = 0;
        for(DeviceRegistration device : devices)
        {
            if(!device.isRegistered(DeviceRegistration.DeviceType.ANDROID, type.getRegisteredType()) ||
                device.getDeviceId().equals(notToDeviceId))
            {
                LOG.info("skipping device: " + device.getDeviceId());
                continue;
            }

            ++numDeviceMessages;
            C2DMessage message = new C2DMessage(device.getRegistrationToken(), collapseKey, params, true);
            queue(message, withRetry, useToken);
        }

        LOG.info("scheduled " + numDeviceMessages + " device messages for " + username);
    }

    void scheduleRetry(RetryC2DM task, int retryCount)
    {
        if(retryCount < 0)
        {
            throw new IllegalArgumentException("retry count must by non-negative");
        }

        LOG.info("Scheduling retry " + retryCount);
        ScheduledFuture<?> future = threadPool.schedule(task, (long)Math.pow(2, retryCount), TimeUnit.MINUTES);
        cancelAndAddFuture(task.getMessage(), future);
    }
    
    private void queue(C2DMessage message, boolean withRetry, String useToken)
    {
        RetryC2DM task = new RetryC2DM(message, withRetry, useToken);
        ScheduledFuture<?> future = threadPool.schedule(task, 1, TimeUnit.SECONDS); // give it a second in case there is a burst
        cancelAndAddFuture(message, future);
    }
    
    void clearFuture(C2DMessage message)
    {
        if(futures.remove(message) != null)
        {
            LOG.debug("removed future from cache");
        }
    }
    
    private void cancelAndAddFuture(C2DMessage message, ScheduledFuture<?> newFuture)
    {
        ScheduledFuture<?> sf = futures.put(message, newFuture);
        if(sf != null)
        {
            if(sf.cancel(false))
            {
                LOG.debug("canceled pending C2DM message");
            }
        }
    }
    
}
