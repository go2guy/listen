package com.interact.listen

import com.interact.listen.license.ListenFeature

class ApplicationService {
    static transactional = false

    def licenseService

    def listApplications() {
        def applications = []

        if(licenseService.canAccess(ListenFeature.AFTERHOURS)) {
            applications << 'After Hours'
        }

        if(licenseService.canAccess(ListenFeature.ATTENDANT)) {
            applications << 'Attendant'
        }

        if(licenseService.canAccess(ListenFeature.BROADCAST)) {
            applications << 'Broadcast'
        }

        if(licenseService.canAccess(ListenFeature.CONFERENCING)) {
            applications << 'Conferencing'
        }

        if(licenseService.canAccess(ListenFeature.VOICEMAIL) || licenseService.canAccess(ListenFeature.FAX)) {
            applications << 'Direct Message'
        }

        if(licenseService.canAccess(ListenFeature.VOICEMAIL)) {
            applications << 'Mailbox'
            applications << 'Voicemail'
        }

        if(licenseService.canAccess(ListenFeature.FINDME)) {
            applications << 'Find Me Config'
        }

        if(licenseService.canAccess(ListenFeature.IPPBX)) {
            applications << 'IP PBX'
        }

        return applications.sort()
    }
}
