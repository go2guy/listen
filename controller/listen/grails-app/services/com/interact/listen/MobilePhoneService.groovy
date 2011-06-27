package com.interact.listen

class MobilePhoneService {
    def cloudToDeviceService
    def springSecurityService

    MobilePhone create(def params) {
        def user = springSecurityService.getCurrentUser()
        def mobilePhone = new MobilePhone(params)
        mobilePhone.owner = user

        if(mobilePhone.validate() && mobilePhone.save()) {
            if(mobilePhone.isPublic) {
                cloudToDeviceService.sendContactSync()
            }
            // TODO history?
        }
        return mobilePhone
    }

    void delete(MobilePhone mobilePhone) {
        def user = springSecurityService.getCurrentUser()

        if(mobilePhone.owner != user) {
            // TODO use better exception type
            throw new AssertionError('Action not allowed')
        }

        mobilePhone.delete()
        if(mobilePhone.isPublic) {
            cloudToDeviceService.sendContactSync()
        }
    }

    MobilePhone update(MobilePhone mobilePhone, def params) {
        def user = springSecurityService.getCurrentUser()
        def originallyPublic = mobilePhone.isPublic
        mobilePhone.properties = params
        mobilePhone.owner = user

        if(mobilePhone.validate() && mobilePhone.save()) {
            if(originallyPublic != mobilePhone.isPublic) {
                cloudToDeviceService.sendContactSync()
            }
            // TODO history?
        }

        return mobilePhone
    }
}
