package com.interact.listen

import com.interact.listen.GlobalOutdialRestriction
import com.interact.listen.mail.MailConfiguration
import grails.plugin.springsecurity.annotation.Secured
import org.apache.log4j.Logger

@Secured(['ROLE_CUSTODIAN'])
class CustodianAdministrationController {
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
        log.debug "Custodian administration goto outdialing from index"
        redirect(action: 'outdialing')
    }

    def addRestriction = {
        log.debug "Custodian administration add restriction"
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
        log.debug "Custodian administration delete restriction"
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
        log.debug "Custodian administration goto mail from mail"
        def list = MailConfiguration.list()
        def mail = list.size() > 0 ? list[0] : new MailConfiguration()
        render(view: 'mail', model: [mail: mail])
    }

    def outdialing = {
        log.debug "Custodian administration goto outdialing from outdialing"
        def model = restrictionModel()
        log.debug "Custodian administration going to render view outdialing"
        render(view: 'outdialing', model: model)
    }

    def saveMail = {
        log.debug "Custodian administration save mail"
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
        log.debug "Custodian administration update restriction"
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
        // Gorm failed me here.  without the 'ByPatternLike' on this 'findAll' the 'findAll' would throw exception
        def restrictions = GlobalOutdialRestriction.findAllByPatternLike('%', [sort: 'pattern', order: 'asc'])
        return [
            restrictions: restrictions,
        ]
    }
}
