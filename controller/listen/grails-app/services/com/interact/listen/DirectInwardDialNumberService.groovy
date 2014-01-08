package com.interact.listen

import com.interact.listen.license.ListenFeature

class DirectInwardDialNumberService {
    def historyService
    def springSecurityService

    DirectInwardDialNumber create(def params) {
        throwErrorIfNotAllowed()

        def directInwardDialNumber = new DirectInwardDialNumber(params)
        if(directInwardDialNumber.validate() && directInwardDialNumber.save()) {
            historyService.createdDirectInwardDialNumber(directInwardDialNumber)
        }

        return directInwardDialNumber
    }

    void delete(DirectInwardDialNumber directInwardDialNumber) {
        throwErrorIfNotAllowed()
        directInwardDialNumber.delete()
        historyService.deletedDirectInwardDialNumber(directInwardDialNumber)
    }

    DirectInwardDialNumber update(DirectInwardDialNumber directInwardDialNumber, def params) {
        throwErrorIfNotAllowed()

        // TODO make sure owner is in current user organization? thats a pretty far-out check :O

        def originalNumber = directInwardDialNumber.number
        def originalOwner = directInwardDialNumber.owner

        directInwardDialNumber.properties = params
        if(directInwardDialNumber.validate() && directInwardDialNumber.save()) {

            if(originalNumber != directInwardDialNumber.number || originalOwner != directInwardDialNumber.owner) {
                def fake = new Expando(number: originalNumber,
                                       owner: originalOwner)
                historyService.deletedDirectInwardDialNumber(fake)
                historyService.createdDirectInwardDialNumber(directInwardDialNumber)
            }
        }

        return directInwardDialNumber
    }

    private boolean throwErrorIfNotAllowed() {
        def user = springSecurityService.getCurrentUser()
        if(!user.hasRole('ROLE_ORGANIZATION_ADMIN')) {
            // TODO better exception type? assertion may be okay, since we dont expect to fail
            throw new AssertionError('Action not allowed')
        }
    }
}
