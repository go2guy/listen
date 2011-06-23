package com.interact.listen.android;

import com.interact.listen.android.DeviceRegistration.DeviceType;
import com.interact.listen.android.DeviceRegistration.RegisteredType;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatWriterService;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

public class C2DMessaging
{
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

    private C2DSender cloudToDeviceSender;
    private CloudToDeviceService cloudToDeviceService;
    private GoogleAuthService googleAuthService;
    private StatWriterService statWriterService;

    public void setCloudToDeviceSender(C2DSender cloudToDeviceSender)
    {
        this.cloudToDeviceSender = cloudToDeviceSender;
    }

    public void setCloudToDeviceService(CloudToDeviceService cloudToDeviceService)
    {
        this.cloudToDeviceService = cloudToDeviceService;
    }

    public void setGoogleAuthService(GoogleAuthService googleAuthService)
    {
        this.googleAuthService = googleAuthService;
    }

    public void setStatWriterService(StatWriterService statWriterService)
    {
        this.statWriterService = statWriterService;
    }

    public synchronized void setEnabled(boolean enabled)
    {
        googleAuthService.setEnabled(enabled);
        currentEnabled = enabled;
    }
    
    public synchronized boolean isEnabled()
    {
        if(currentEnabled == null)
        {
            currentEnabled = googleAuthService.isEnabled();
        }
        return currentEnabled;
    }

    public void enqueueAllSyncMessages(DeviceType dType, Type type, String useToken)
    {
        List<DeviceRegistrationProxy> devices = cloudToDeviceService.queryDevicesWithDeviceType(dType);

        LOG.info("Sending " + type + " change to all devices, " + devices.size() + ", registered with " + type.getRegisteredType());
        
        for(DeviceRegistrationProxy device : devices)
        {
            if(device.getRegistrationToken().length() == 0 ||
                (type.getRegisteredType() != null && !device.isRegistered(dType, type.getRegisteredType())))
            {
                LOG.info("Skipping device with id [" + device.getDeviceId() + "], it has no token, or is not registered for [" + type.getRegisteredType() + "] syncing");
                continue;
            }

            String username = device.getUsername();
            LOG.info("Queueing " + type + " change for [" + username + "], registration token [" + device.getRegistrationToken() + "]");

            String collapseKey = type.getCollapseKey(username);
            Map<String, String[]> params = createParamMap(username, type);

            C2DMessage message = new C2DMessage(device.getRegistrationToken(), collapseKey, params, true);
            queue(message, false, useToken);
        }
    }

    public void enqueueDeviceSyncMessage(Long userId, Type type, String notToDeviceId)
    {
        enqueueDeviceSyncMessage(userId, type, null, notToDeviceId);
    }

    private static Map<String, String[]> createParamMap(String username, Type type)
    {
        Map<String, String[]> params = new HashMap<String, String[]>();

        params.put("data.account_name", new String[] {username});
        params.put("data.message", new String[] {type.getMessage()});

        return params;
    }
    
    private void enqueueDeviceSyncMessage(Long userId, Type type,
                                          String useToken, String notToDeviceId)
    {
        final boolean force = type == Type.SYNC_CONFIG_CHANGED;     // force config changes even if disabled
        final boolean withRetry = type != Type.SYNC_CONFIG_CHANGED; // don't bother with retries for config changes

        if(!force && !isEnabled())
        {
            LOG.debug("C2DM is not enabled; force = [" + force + "], isEnabled = [" + isEnabled() + "]");
            return;
        }

        Set<DeviceRegistrationProxy> devices = cloudToDeviceService.getUserDevices(userId);
        if(devices == null || devices.isEmpty())
        {
            LOG.debug("User with id " + userId + " has no devices");
            return;
        }
        
        int numDeviceMessages = 0;
        for(DeviceRegistrationProxy device : devices)
        {
            String collapseKey = type.getCollapseKey(device.getUsername());
            Map<String, String[]> params = createParamMap(device.getUsername(), type);

            if(!device.isRegistered(DeviceRegistration.DeviceType.ANDROID, type.getRegisteredType()) ||
                device.getDeviceId().equals(notToDeviceId))
            {
                LOG.info("Skipping device with id [" + device.getDeviceId() +
                         "], it is either being explicitly skipped, or is not registered for [" +
                         type.getRegisteredType() + "] syncing");
                continue;
            }

            ++numDeviceMessages;
            C2DMessage message = new C2DMessage(device.getRegistrationToken(), collapseKey, params, true);
            queue(message, withRetry, useToken);

            LOG.info("scheduled device message for user " + device.getUsername() + " with device token " + device.getRegistrationToken());
        }
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
        RetryC2DM task = new RetryC2DM(this, message, withRetry, useToken);
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

    C2DError send(C2DMessage message, String useToken) throws IOException
    {
        return cloudToDeviceSender.send(message, useToken);
    }

    void deleteRegistration(String registrationId)
    {
        cloudToDeviceService.deleteRegistration(registrationId);
    }

    void writeStat(Stat stat)
    {
        statWriterService.send(stat);
    }
}
