package com.interact.listen

class UserService {
    def cloudToDeviceService
    def historyService
    def ldapService
    def springSecurityService

    User update(User user, def params, boolean byOperator = false) {
        def originalEmailAddress = user.emailAddress
        def originalRealName = user.realName

        if(byOperator) {
            user.properties['username', 'pass', 'confirm', 'realName', 'emailAddress'] = params
        } else {
            user.properties['realName', 'emailAddress', 'pass', 'confirm'] = params
        }

        if(user.pass?.trim()?.length() > 0) {
            user.password = springSecurityService.encodePassword(user.pass)
        }
        if(user.validate() && user.save()) {
            cloudToDeviceService.sendContactSync()

            if(originalEmailAddress != user.emailAddress) {
                ldapService.changeEmailAddress(user, originalEmailAddress, user.emailAddress)
            }

            if(originalRealName != user.realName) {
                ldapService.changeName(user, user.realName)
            }
        }
        return user
    }

    User disable(User user) {
        user.enabled = false
        if(user.validate() && user.save()) {
            historyService.disabledUser(user)
            // TODO disable on ldap?
        }
        return user
    }

    User enable(User user) {
        user.enabled = true
        if(user.validate() && user.save()) {
            historyService.enabledUser(user)
            // TODO enable on ldap?
        }
        return user
    }
}
