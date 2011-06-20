package com.interact.listen

import com.interact.listen.license.ListenFeature

class ApplicationService {
    static scope = 'singleton'
    static transactional = false

    def licenseService // injected

    def listApplications() {
        def applications = []

        if(licenseService.canAccess(ListenFeature.AFTERHOURS)) {
           applications.add('After Hours')
        }

        if(licenseService.canAccess(ListenFeature.ATTENDANT)) {
            applications.add('Attendant')
        }

        if(licenseService.canAccess(ListenFeature.BROADCAST)) {
            applications.add('Broadcast')
        }

        if(licenseService.canAccess(ListenFeature.CONFERENCING)) {
            applications.add('Conferencing')
        }

        if(licenseService.canAccess(ListenFeature.VOICEMAIL)) {
            applications.add('Direct Voicemail')
            applications.add('Mailbox')
            applications.add('Voicemail')
        }

        if(licenseService.canAccess(ListenFeature.FINDME)) {
            applications.add('Find Me Config')
        }

        if(licenseService.canAccess(ListenFeature.IPPBX)) {
            applications.add('IP PBX')
        }

        return applications.sort()
    }
}
