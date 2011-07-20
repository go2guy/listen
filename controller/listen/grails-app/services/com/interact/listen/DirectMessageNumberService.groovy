package com.interact.listen

class DirectMessageNumberService {
    def springSecurityService

    DirectMessageNumber create(def params) {
        throwErrorIfNotAllowed()

        def directMessageNumber = new DirectMessageNumber(params)
        if(directMessageNumber.validate() && directMessageNumber.save()) {
            // TODO history?
        }

        return directMessageNumber
    }

    void delete(DirectMessageNumber directMessageNumber) {
        throwErrorIfNotAllowed()
        directMessageNumber.delete()
        // TODO history?
    }

    DirectMessageNumber update(DirectMessageNumber directMessageNumber, def params) {
        throwErrorIfNotAllowed()

        // TODO make sure owner is in current user organization? thats a pretty far-out check :O

        directMessageNumber.properties = params
        if(directMessageNumber.validate() && directMessageNumber.save()) {
            // TODO history?
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
