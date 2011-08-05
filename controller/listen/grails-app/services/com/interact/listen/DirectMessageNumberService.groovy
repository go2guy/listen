package com.interact.listen

import com.interact.listen.license.ListenFeature

class DirectMessageNumberService {
    def historyService
    def ldapService
    def licenseService
    def springSecurityService

    DirectMessageNumber create(def params) {
        throwErrorIfNotAllowed()

        def directMessageNumber = new DirectMessageNumber(params)
        if(directMessageNumber.validate() && directMessageNumber.save()) {
            historyService.createdDirectMessageNumber(directMessageNumber)
            if(licenseService.canAccess(ListenFeature.FAX, directMessageNumber.owner.organization)) {
                ldapService.addFaxNumber(directMessageNumber.owner, directMessageNumber.number)
            }
        }

        return directMessageNumber
    }

    void delete(DirectMessageNumber directMessageNumber) {
        throwErrorIfNotAllowed()
        directMessageNumber.delete()
        if(licenseService.canAccess(ListenFeature.FAX, directMessageNumber.owner.organization)) {
            ldapService.removeFaxNumber(directMessageNumber.owner, directMessageNumber.number)
        }
        historyService.deletedDirectMessageNumber(directMessageNumber)
    }

    DirectMessageNumber update(DirectMessageNumber directMessageNumber, def params) {
        throwErrorIfNotAllowed()

        // TODO make sure owner is in current user organization? thats a pretty far-out check :O

        def originalNumber = directMessageNumber.number
        def originalOwner = directMessageNumber.owner

        directMessageNumber.properties = params
        if(directMessageNumber.validate() && directMessageNumber.save()) {
            if(licenseService.canAccess(ListenFeature.FAX, directMessageNumber.owner.organization) && originalNumber != directMessageNumber.number) {
                ldapService.changeFaxNumber(directMessageNumber.owner, originalNumber, directMessageNumber.number)
            }

            if(originalNumber != directMessageNumber.number || originalOwner != directMessageNumber.owner) {
                def fake = new Expando(number: originalNumber,
                                       owner: originalOwner)
                historyService.deletedDirectMessageNumber(fake)
                historyService.createdDirectMessageNumber(directMessageNumber)
            }
        }

        return directMessageNumber
    }

    private boolean throwErrorIfNotAllowed() {
        def user = springSecurityService.getCurrentUser()
        if(!user.hasRole('ROLE_ORGANIZATION_ADMIN')) {
            // TODO better exception type? assertion may be okay, since we dont expect to fail
            throw new AssertionError('Action not allowed')
        }
    }
}
