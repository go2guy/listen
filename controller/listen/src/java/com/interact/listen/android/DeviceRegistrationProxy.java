package com.interact.listen.android;

import com.interact.listen.android.DeviceRegistration.DeviceType;
import com.interact.listen.android.DeviceRegistration.RegisteredType;

import java.util.HashSet;
import java.util.Set;

public class DeviceRegistrationProxy
{
    private String deviceId;
    private DeviceType deviceType;
    private Set<RegisteredType> registeredTypes = new HashSet<RegisteredType>();
    private String registrationToken;
    private String username;

    public String getDeviceId()
    {
        return deviceId;
    }

    public void setDeviceId(String deviceId)
    {
        this.deviceId = deviceId;
    }

    public DeviceType getDeviceType()
    {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType)
    {
        this.deviceType = deviceType;
    }

    public void setRegisteredTypes(Set<RegisteredType> registeredTypes)
    {
        this.registeredTypes = registeredTypes;
    }

    public Set<RegisteredType> getRegisteredTypes()
    {
        return registeredTypes;
    }

    public void setRegistrationToken(String registrationToken)
    {
        this.registrationToken = registrationToken;
    }

    public String getRegistrationToken()
    {
        return registrationToken;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public boolean isRegistered(DeviceType deviceType, RegisteredType registeredType)
    {
        if(registrationToken == null || registrationToken.length() == 0)
        {
            return false;
        }

        return this.deviceType == deviceType && this.registeredTypes.contains(registeredType);
    }
}