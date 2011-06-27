package com.interact.listen.voicemail

class DirectVoicemailNumberService {
    static transactional = true

    def springSecurityService

    DirectVoicemailNumber create(def params) {
        throwErrorIfNotAllowed()

        def directVoicemailNumber = new DirectVoicemailNumber(params)
        if(directVoicemailNumber.validate() && directVoicemailNumber.save()) {
            // TODO history?
        }

        return directVoicemailNumber
    }

    void delete(DirectVoicemailNumber directVoicemailNumber) {
        throwErrorIfNotAllowed()
        directVoicemailNumber.delete()
        // TODO history?
    }

    DirectVoicemailNumber update(DirectVoicemailNumber directVoicemailNumber, def params) {
        throwErrorIfNotAllowed()

        // TODO make sure owner is in current user organization? thats a pretty far-out check :O

        directVoicemailNumber.properties = params
        if(directVoicemailNumber.validate() && directVoicemailNumber.save()) {
            // TODO history?
        }

        return directVoicemailNumber
    }

    private boolean throwErrorIfNotAllowed() {
        def user = springSecurityService.getCurrentUser()
        if(!user.hasRole('ROLE_ORGANIZATION_ADMIN')) {
            // TODO better exception type? assertion may be okay, since we dont expect to fail
            throw new AssertionError('Action not allowed')
        }
    }
}
