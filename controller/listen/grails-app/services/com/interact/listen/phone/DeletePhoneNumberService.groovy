package com.interact.listen.phone

import com.interact.listen.PhoneNumber

class DeletePhoneNumberService {
    static scope = 'singleton'
    static transactional = true

    def cloudToDeviceService
    def springSecurityService

    def deleteUserPhoneNumberByUser(PhoneNumber phoneNumber) {
        def user = springSecurityService.getCurrentUser()

        if(phoneNumber.owner != user || phoneNumber.type.isSystem()) {
            // TODO use better exception type
            throw new AssertionError('Action not allowed')
        }

        phoneNumber.delete()
        cloudToDeviceService.sendContactSync()
    }
}
