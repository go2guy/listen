package com.interact.listen.phone

import com.interact.listen.PhoneNumber
import com.interact.listen.PhoneNumberType

class CreatePhoneNumberService {
    static scope = 'singleton'
    static transactional = true

    def cloudToDeviceService
    def messageLightService
    def springSecurityService

    def createPhoneNumberByOperator(def params) {
        def phoneNumber = new PhoneNumber(params)
        if(phoneNumber.validate() && phoneNumber.save()) {
            cloudToDeviceService.sendContactSync()
            messageLightService.toggle(phoneNumber)
            // TODO history?
            // TODO stat?
        }

        return phoneNumber
    }

    def createPhoneNumberByUser(def params) {
        def user = springSecurityService.getCurrentUser()

        def phoneNumber = new PhoneNumber()
        phoneNumber.properties['number', 'isPublic'] = params
        def type = PhoneNumberType.valueOf(params.type)
        phoneNumber.type = type.isSystem() ? PhoneNumberType.OTHER : type
        phoneNumber.owner = user

        if(phoneNumber.validate() && phoneNumber.save()) {
            cloudToDeviceService.sendContactSync()
        }

        return phoneNumber
    }
}
