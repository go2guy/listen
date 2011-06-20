package com.interact.listen.voicemail

import com.interact.listen.license.ListenFeature

class NewVoicemailCountService {
    static scope = 'singleton'
    static transactional = true

    def licenseService
    def springSecurityService

    def count(def user = null) {
        if(!licenseService.isLicensed(ListenFeature.VOICEMAIL)) {
            return 0
        }

        def forUser = user ?: springSecurityService.getCurrentUser()
        return Voicemail.countByOwnerAndIsNew(forUser, true)
    }
}
