package com.interact.listen.fax

import com.interact.listen.UserFile
import grails.plugins.springsecurity.Secured
import javax.servlet.http.HttpServletResponse
import org.joda.time.format.DateTimeFormat
import org.springframework.web.multipart.MultipartFile

@Secured(['ROLE_FAX_USER'])
class FaxController {
    static allowedMethods = [
        index: 'GET',
        create: 'GET',
        download: 'GET',
        prepare: 'GET',
        prepareStatus: 'GET',
        send: 'POST'
    ]

    def faxSenderService
    def historyService

    def index = {
        redirect(action: 'create')
    }

    def create = {
        def fax = OutgoingFax.get(params.id)
        render(view: 'create', model: [fax: fax])
    }

    def download = {
        def preserve = [:]
        if(params.sort) preserve.sort = params.sort
        if(params.order) preserve.order = params.order
        if(params.offset) preserve.offset = params.offset
        if(params.max) preserve.max = params.max

        def fax = Fax.get(params.id)
        if(!fax) {
            flash.errorMessage = 'Fax not found'
            redirect(controller: 'messages', action: 'inbox', params: preserve)
            return
        }

        if(fax.owner != authenticatedUser) {
            redirect(controller: 'login', action: 'denied')
            return
        }

        // TODO do we need to do any special escaping of the filename to avoid malicious activity?
        def date = DateTimeFormat.forPattern("yyyyMMddHHmmss").print(fax.dateCreated)
        def name = "Fax - ${fax.from()} - ${date}.tiff"

        response.contentType = 'image/tiff'
        response.setHeader('Content-disposition', "attachment;filename=${name}")

        response.outputStream << fax.file.newInputStream()
        response.flushBuffer()

        historyService.downloadedFax(fax)
    }

    def save = { MultiFileUploadCommand command ->
        def user = authenticatedUser

        def uploaded = [] as List
        def names = [] as List
        def subdir = System.currentTimeMillis() as String
        command.files.each { file ->
            log.debug "Handling file [${file}]"
            if(file instanceof MultipartFile) {
                if(!file.isEmpty()) {
                    def filename = file.originalFilename
                    def count = names.count(filename)
                    if(count > 0) {
                        filename = "(${count}) ${filename}"
                    }

                    names << file.originalFilename

                    def target = UserFile.pathFor(user, subdir, filename)
                    file.transferTo(target)

                    def userFile = new UserFile(owner: user, file: target)
                    userFile.validate() && userFile.save()
                    uploaded << userFile
                }
            } else {
                def userFile = UserFile.get(file)
                if(userFile) {
                    uploaded << userFile
                }
            }
        }

        def fax = new OutgoingFax()
        fax.properties['dnis'] = params
        fax.toMerge.addAll(uploaded)
        fax.sender = user

        if(fax.validate() && fax.save()) {
            faxSenderService.prepareInBackground(fax)
            redirect(action: 'prepare', params: [id: fax.id])
        } else {
            render(view: 'create', model: [fax: fax])
        }
    }

    def prepare = {
        def fax = OutgoingFax.get(params.id)
        if(!fax) {
            flash.errorMessage = 'Fax not found'
            redirect(action: 'create')
            return
        }
        render(view: 'preparing', model: [fax: fax])
    }

    def prepareStatus = {
        def fax = OutgoingFax.get(params.id)
        if(!fax) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }

        render(contentType: 'application/json') {
            ready = fax.merged != null
            pages = fax.pages
            status = fax.preparationStatus
        }
    }

    def send = {
        def fax = OutgoingFax.get(params.id)
        if(!fax) {
            flash.errorMessage = 'Fax not found'
            redirect(action: 'create')
            return
        }

        if(!fax.merged) {
            flash.errorMessage = 'Fax has not been prepared'
            redirect(action: 'prepare', params: [id: fax.id])
            return
        }

        faxSenderService.send(fax)
        flash.successMessage = 'Fax sent'
        render(view: 'create')
    }
}
