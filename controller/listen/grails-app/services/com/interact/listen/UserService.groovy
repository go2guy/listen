package com.interact.listen

class UserService {
    def cloudToDeviceService
    def historyService
    def ldapService
    def springSecurityService

    User update(User user, def params, boolean byOperator = false) {
        def originalEmailAddress = user.emailAddress
        def originalPassword = user.password
        def originalRealName = user.realName
        def originalUsername = user.username

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
                historyService.changedAccountEmailAddress(user, originalEmailAddress)
                if(user.organization) {
                    ldapService.changeEmailAddress(user, originalEmailAddress, user.emailAddress)
                }
            }

            if(originalRealName != user.realName) {
                historyService.changedAccountName(user, originalRealName)
                if(user.organization) {
                    ldapService.changeName(user, user.realName)
                }
            }

            if(originalPassword != user.password) {
                historyService.changedAccountPassword(user)
            }

            if(originalUsername != user.username) {
                historyService.changedAccountUsername(user, originalUsername)
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
