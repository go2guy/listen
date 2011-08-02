package com.interact.listen

class MobilePhoneService {
    def cloudToDeviceService
    def ldapService
    def springSecurityService

    MobilePhone create(def params) {
        def user = springSecurityService.getCurrentUser()
        def mobilePhone = new MobilePhone(params)
        mobilePhone.owner = user

        if(mobilePhone.validate() && mobilePhone.save()) {
            if(mobilePhone.isPublic) {
                cloudToDeviceService.sendContactSync()
                ldapService.addMobileNumber(mobilePhone.owner, mobilePhone.number)
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
            ldapService.removeMobileNumber(mobilePhone.owner, mobilePhone.number)
        }
    }

    MobilePhone update(MobilePhone mobilePhone, def params) {
        def user = springSecurityService.getCurrentUser()
        def originallyPublic = mobilePhone.isPublic
        def originalNumber = mobilePhone.number
        mobilePhone.properties = params
        mobilePhone.owner = user

        if(mobilePhone.validate() && mobilePhone.save()) {
            ldapService.removeMobileNumber(mobilePhone.owner, originalNumber)
            ldapService.removeMobileNumber(mobilePhone.owner, mobilePhone.number)

            if(mobilePhone.isPublic) {
                ldapService.addMobileNumber(mobilePhone.owner, mobilePhone.number)
            }

            if(originallyPublic != mobilePhone.isPublic) {
                cloudToDeviceService.sendContactSync()
            }

            // TODO history?
        }

        return mobilePhone
    }
}
