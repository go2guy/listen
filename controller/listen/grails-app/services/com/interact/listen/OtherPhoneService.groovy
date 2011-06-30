package com.interact.listen

class OtherPhoneService {
    def cloudToDeviceService
    def springSecurityService

    OtherPhone create(def params) {
        def user = springSecurityService.getCurrentUser()
        def otherPhone = new OtherPhone(params)
        otherPhone.owner = user

        if(otherPhone.validate() && otherPhone.save()) {
            if(otherPhone.isPublic) {
                cloudToDeviceService.sendContactSync()
            }
            // TODO history?
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
    }

    OtherPhone update(OtherPhone otherPhone, def params) {
        def user = springSecurityService.getCurrentUser()
        def originallyPublic = otherPhone.isPublic
        otherPhone.properties = params
        otherPhone.owner = user

        if(otherPhone.validate() && otherPhone.save()) {
            if(originallyPublic != otherPhone.isPublic) {
                cloudToDeviceService.sendContactSync()
            }
            // TODO history?
        }

        return otherPhone
    }
}
