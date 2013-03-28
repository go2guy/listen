package com.interact.listen

import com.interact.listen.OtherPhone

class OtherPhoneService {
    def cloudToDeviceService
    def historyService
    def springSecurityService

    OtherPhone create(def params) {
        def user = springSecurityService.getCurrentUser()
        def otherPhone = new OtherPhone(params)
        otherPhone.owner = user

        if(otherPhone.validate() && otherPhone.save()) {
            if(otherPhone.isPublic) {
                cloudToDeviceService.sendContactSync()
            }
            historyService.createdOtherPhone(otherPhone)
        }
        return otherPhone
    }

    void delete(OtherPhone otherPhone) {
        def user = springSecurityService.getCurrentUser()

        if(otherPhone.owner != user) {
            // TODO use better exception type
            throw new AssertionError('Action not allowed')
        }

        otherPhone.delete()
        if(otherPhone.isPublic) {
            cloudToDeviceService.sendContactSync()
        }
        historyService.deletedOtherPhone(otherPhone)
    }

    OtherPhone update(OtherPhone otherPhone, def params) {
        def user = springSecurityService.getCurrentUser()
        def originallyPublic = otherPhone.isPublic
        def originalNumber = otherPhone.number
        otherPhone.properties = params
        otherPhone.owner = user

        if(otherPhone.validate() && otherPhone.save()) {
            if(originalNumber != otherPhone.number) {
                def fake = new Expando(number: originalNumber, owner: otherPhone.owner)
                historyService.deletedOtherPhone(fake)
                historyService.createdOtherPhone(otherPhone)
            } else {
                if(originallyPublic != otherPhone.isPublic) {
                    historyService.changedOtherPhoneVisibility(otherPhone)
                }
            }

            if(originallyPublic != otherPhone.isPublic) {
                cloudToDeviceService.sendContactSync()
            }
        }

        return otherPhone
    }
}
