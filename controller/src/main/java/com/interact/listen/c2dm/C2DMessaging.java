package com.interact.listen.c2dm;

import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;
import com.interact.listen.resource.DeviceRegistration;
import com.interact.listen.resource.DeviceRegistration.DeviceType;
import com.interact.listen.resource.Subscriber;

import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.hibernate.Session;

public final class C2DMessaging
{
    public static enum Type
    {
        SYNC_VOICEMAILS("sync-voicemails", "0-"),
        SYNC_CONFIG_CHANGED("sync-config", "1-");
        
        private final String message;
        private final String keyAppend;
        
        private Type(String message, String keyAppend)
        {
            this.message = message;
            this.keyAppend = keyAppend;
        }
        
        public String getMessage()
        {
            return message;
        }
        
        public String getCollapseKey(String username)
        {
            return keyAppend + Long.toHexString(username.hashCode());
        }
    }
    
    private static enum Instance
    {
        INSTANCE;
        
        private C2DMessaging m;
        
        private Instance()
        {
            m = new C2DMessaging();
        }
    }
    
    private static final Logger LOG = Logger.getLogger(C2DMessaging.class);

    private final ScheduledThreadPoolExecutor threadPool = new ScheduledThreadPoolExecutor(1);
    private Boolean currentEnabled = null;

    private C2DMessaging()
    {
    }

    public static C2DMessaging getInstance()
    {
        return Instance.INSTANCE.m;
    }

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

    public void enqueueConfigChanges(Session session, DeviceType type, String useToken)
    {
        List<DeviceRegistration> devices = DeviceRegistration.queryByDevice(session, type, null);

        LOG.info("Sending config change to all devices: " + devices.size());
        
        for(DeviceRegistration device : devices)
        {
            if(device.getRegistrationToken() == null || device.getRegistrationToken().length() == 0)
            {
                continue;
            }

            String username = device.getSubscriber().getUsername();
            LOG.info("queueing config change for " + username + " '" + device.getRegistrationToken() + "'");

            String collapseKey = Type.SYNC_CONFIG_CHANGED.getCollapseKey(username);
            Map<String, String[]> params = createParamMap(username, Type.SYNC_CONFIG_CHANGED);

            C2DMessage message = new C2DMessage(device.getRegistrationToken(), collapseKey, params, true);
            queue(message, false, useToken);
        }
    }

    public void enqueueDeviceSyncMessage(Session session, Subscriber subscriber, Type type, String notToDeviceId)
    {
        enqueueDeviceSyncMessage(session, subscriber, type, null, notToDeviceId);
    }

    private static Map<String, String[]> createParamMap(String username, Type type)
    {
        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("data.account_name", new String[] {username});
        params.put("data.message", new String[] {type.getMessage()});

        return params;
    }
    
    private void enqueueDeviceSyncMessage(Session session, Subscriber subscriber, Type type,
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
        Set<DeviceRegistration> devices = subscriber.getDevices();
        if(devices.isEmpty())
        {
            LOG.debug(username + " has no devices");
            return;
        }
        
        String collapseKey = type.getCollapseKey(username);
        Map<String, String[]> params = createParamMap(username, type);

        int numDeviceMessages = 0;
        for(DeviceRegistration device : devices)
        {
            if(device.getRegistrationToken().length() == 0 ||
                DeviceRegistration.DeviceType.ANDROID != device.getDeviceType() ||
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

    void scheduleRetry(Runnable task, int retryCount)
    {
        if(retryCount < 0)
        {
            throw new IllegalArgumentException("retry count must by non-negative");
        }

        LOG.info("Sceduling retry " + retryCount);
        threadPool.remove(task);
        threadPool.schedule(task, (long)Math.pow(2, retryCount), TimeUnit.MINUTES);
    }
    
    private void queue(C2DMessage message, boolean withRetry, String useToken)
    {
        Runnable task = new RetryC2DM(message, withRetry, useToken);
        threadPool.remove(task);
        threadPool.submit(task);
    }

}
