package com.interact.listen.phone

import com.interact.listen.PhoneNumber
import com.interact.listen.PhoneNumberType

class UpdatePhoneNumberService {
    static scope = 'singleton'
    static transactional = true

    def cloudToDeviceService
    def messageLightService
    def springSecurityService

    def updatePhoneNumberByOperator(PhoneNumber phoneNumber, def params) {
        def oNumber = phoneNumber.number
        def oSupports = phoneNumber.supportsMessageLight

        phoneNumber.properties['number', 'owner', 'type'] = params
        if(phoneNumber.validate() && phoneNumber.save()) {
            cloudToDeviceService.sendContactSync()

            if(phoneNumber.supportsMessageLight && (oNumber != phoneNumber.number || !oSupports)) {
                messageLightService.toggle(oNumber, false)
                messageLightService.toggle(phoneNumber)
            } else if(oSupports && !phoneNumber.supportsMessageLight) {
                messageLightService.toggle(oNumber, false)
            }

            // TODO history?
            // TODO stat?
        }

        return phoneNumber
    }

    def updateSystemPhoneNumberByUser(PhoneNumber phoneNumber, def params) {
        def user = springSecurityService.getCurrentUser()

        if(phoneNumber.owner != user || !phoneNumber.type == PhoneNumberType.EXTENSION) {
            // TODO use better exception type
            throw new AssertionError('Action not allowed')
        }

        params.forwardedTo = params.forwardedTo && params.forwardedTo != '' ? params.forwardedTo : null
        phoneNumber.properties['forwardedTo'] = params
        if(phoneNumber.validate() && phoneNumber.save()) {
            // TODO does the sync need to be here?
            cloudToDeviceService.sendContactSync()
            // TODO history?
            // TODO stat?
        }

        return phoneNumber
    }

    def updateUserPhoneNumberByUser(PhoneNumber phoneNumber, def params) {
        def user = springSecurityService.getCurrentUser()

        if(phoneNumber.owner != user || phoneNumber.type.isSystem()) {
            // TODO use better exception type
            throw new AssertionError('Action not allowed')
        }

        phoneNumber.properties['number', 'isPublic'] = params
        def type = PhoneNumberType.valueOf(params.type)
        phoneNumber.type = type.isSystem() ? PhoneNumberType.other : type

        if(phoneNumber.validate() && phoneNumber.save()) {
            cloudToDeviceService.sendContactSync()
            // TODO history?
            // TODO stat?
        }

        return phoneNumber
    }
}
