package com.interact.listen.android

import com.interact.listen.User

class DeviceRegistration {
    String deviceId
    DeviceType deviceType
    Set registeredTypes = [] as Set
    String registrationToken
    User user

    static hasMany = [registeredTypes: RegisteredType]

    static constraints = {
        deviceId blank: false
        registrationToken blank: false
    }

    static mapping = {
        registeredTypes joinTable: [
            name: 'device_registration_registered_types',
            key: 'device_registration_id',
            column: 'registered_type',
            type: 'text'
        ]
    }

    public enum DeviceType {
        ANDROID
    }

    public enum RegisteredType {
        VOICEMAIL,
        CONTACTS
    }

    def isRegistered(DeviceType deviceType, RegisteredType registeredType) {
        log.debug "Checking if device is registered for device type [${deviceType}] and registeredType [${registeredType}]"
        if(!registrationToken) {
            log.debug "Device has no registration token, and will be treated as not registered"
            return false
        }
        log.debug "Device has device type [${deviceType}] and is registered for [${registeredTypes}]"
        return this.deviceType == deviceType && this.registeredTypes.contains(registeredType)
    }

    public DeviceRegistrationProxy asProxy() {
        def proxy = new DeviceRegistrationProxy()
        proxy.deviceId = deviceId
        proxy.deviceType = deviceType
        proxy.registrationToken = registrationToken
        proxy.username = user.username

        registeredTypes.each { type ->
            proxy.registeredTypes << type
        }

        return proxy
    }
}
