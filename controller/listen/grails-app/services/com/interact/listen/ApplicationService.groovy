package com.interact.listen

import com.interact.listen.license.ListenFeature

class ApplicationService {
    static transactional = false

    def licenseService

    def listApplications(def organization = null) {
        def applications = []

        if(licenseService.canAccess(ListenFeature.AFTERHOURS, organization)) {
            applications << 'After Hours'
        }
        
        if(licenseService.canAccess(ListenFeature.ATTENDANT, organization)) {
            applications << 'Attendant'
        }

        if(licenseService.canAccess(ListenFeature.BROADCAST, organization)) {
            applications << 'Broadcast'
        }

        if(licenseService.canAccess(ListenFeature.CONFERENCING, organization)) {
            applications << 'Conferencing'
        }

        if(licenseService.canAccess(ListenFeature.VOICEMAIL, organization) || licenseService.canAccess(ListenFeature.FAX, organization)) {
            applications << 'Direct Mailbox'
            applications << 'Direct Message'
        }

        if(licenseService.canAccess(ListenFeature.IPPBX, organization)) {
            applications << 'IP PBX'
            applications << 'Direct Inward Dial'
            applications << 'Monitor ACD Call'
        }

        if(licenseService.canAccess(ListenFeature.VOICEMAIL, organization)) {
            applications << 'Mailbox'
            applications << 'Voicemail'
        }

        return applications.sort()
    }
}
