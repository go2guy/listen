package com.interact.listen

import com.interact.listen.mail.MailConfiguration
import grails.plugins.springsecurity.Secured

@Secured(['ROLE_CUSTODIAN'])
class CustodianAdministrationController {
    def springSecurityService // injected

    static allowedMethods = [
        index: 'GET',
        addRestriction: 'POST',
        deleteRestriction: 'POST',
        mail: 'GET',
        outdialing: 'GET',
        saveMail: 'POST',
        updateRestriction: 'POST'
    ]

    def index = {
        redirect(action: 'outdialing')
    }

    def addRestriction = {
        def restriction = new GlobalOutdialRestriction()
        restriction.pattern = params.pattern

        if(restriction.validate() && restriction.save()) {
            flash.successMessage = "Restriction for ${restriction.pattern} saved"
            redirect(action: 'outdialing')
        } else {
            def model = restrictionModel()
            model.newRestriction = restriction
            render(view: 'outdialing', model: model)
        }
    }

    def deleteRestriction = {
        if(!params.id) {
            flash.errorMessage = 'Restriction not found'
            redirect(action: 'outdialing')
            return
        }

        def restriction = GlobalOutdialRestriction.get(params.id)
        if(!restriction) {
            flash.errorMessage = 'Restriction not found'
            redirect(action: 'outdialing')
            return
        }

        restriction.delete()
        flash.successMessage = 'Restriction deleted'
        redirect(action: 'outdialing')
    }

    def mail = {
        def list = MailConfiguration.list()
        def mail = list.size() > 0 ? list[0] : new MailConfiguration()
        render(view: 'mail', model: [mail: mail])
    }

    def outdialing = {
        render(view: 'outdialing', model: restrictionModel())
    }

    def saveMail = {
        def list = MailConfiguration.list()
        def mail = list.size() > 0 ? list[0] : new MailConfiguration()
        mail.properties = params

        if(mail.validate() && mail.save()) {
            flash.successMessage = 'Mail settings updated'
            redirect(action: 'mail')
        } else {
            render(view: 'mail', model: [mail: mail])
        }
    }

    def updateRestriction = {
        if(!params.id) {
            flash.errorMessage = 'Restriction not found'
            redirect(action: 'restrictions')
            return
        }

        def restriction = GlobalOutdialRestriction.get(params.id)
        if(!restriction) {
            flash.errorMessage = 'Restriction not found'
            redirect(action: 'restrictions')
            return
        }

        restriction.pattern = params.pattern

        if(restriction.validate() && restriction.save()) {
            flash.successMessage = "Restriction for ${restriction.pattern} saved"
            redirect(action: 'outdialing')
        } else {
            def model = restrictionModel()
            model.updatedRestriction = restriction
            render(view: 'outdialing', model: model)
        }
    }

    private def restrictionModel() {
        def restrictions = GlobalOutdialRestriction.findAll([sort: 'pattern', order: 'asc'])
        return [
            restrictions: restrictions,
        ]
    }
}
