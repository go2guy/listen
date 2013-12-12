package com.interact.listen.android

import com.interact.listen.User
import com.interact.listen.android.DeviceRegistration.DeviceType

class CloudToDeviceService {
    @SuppressWarnings('GrailsStatelessService')
    def cloudToDeviceMessaging

    def sendContactSync() {
        def syncType = C2DMessaging.Type.SYNC_CONTACTS
        try {
            log.debug 'Sending C2DM contact sync to all devices'
            cloudToDeviceMessaging.enqueueAllSyncMessages(DeviceType.ANDROID, syncType, null)
        } catch (Exception e) {
            log.error 'Failed C2DM contacy sync [${e}]'
        }
    }

    def sendVoicemailSync(User user) {
        def syncType = C2DMessaging.Type.SYNC_VOICEMAILS
        log.debug "Sending C2DM voicemail sync to ${user}"

        // FIXME handle current device id (used to come from PersistenceService
        try {
            cloudToDeviceMessaging.enqueueDeviceSyncMessage(user.id, syncType, null)
        } catch (Exception e) {
            log.error 'Failed Sending C2DM voicemail sync [${e}]'
        }
    }

    // the following methods are called by the C2D* java files to avoid
    // having to deal with a session in the java code

    public void deleteRegistration(String registrationId) {
        log.info("request to delete registrations for [${registrationId}]")
        try {
            DeviceRegistration.findAllByRegistrationToken(registrationId).each {
                log.debug "Removing device registration [${it}]"
                it.delete(flush: true)
            }
        } catch (Exception e) {
            log.debug "Failed to delete device registration [${e}]"
        }
    }

    public List<DeviceRegistrationProxy> queryDevicesWithDeviceType(DeviceType deviceType) {
        return DeviceRegistration.findAllByDeviceType(deviceType).collect { device ->
            device.asProxy()
        } as List
    }

    public Set<DeviceRegistrationProxy> getUserDevices(Long userId) {
        def user = User.get(userId)
        if(!user) {
            throw new IllegalArgumentException("User not found with id ${userId}")
        }
        def result = DeviceRegistration.findAllByUser(user).collect { device ->
            device.asProxy()
        } as Set
        log.debug "Found ${result.size()} devices for user [${user}]"
        return result
    }
}
