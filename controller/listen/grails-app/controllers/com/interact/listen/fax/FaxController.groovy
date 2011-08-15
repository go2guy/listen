package com.interact.listen.fax

import com.interact.listen.UserFile
import com.interact.listen.util.FileTypeDetector
import com.interact.listen.util.FileTypeDetectionException
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
        resend: 'POST',
        save: 'POST',
        sending: 'GET',
        status: 'GET'
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

        try {
            def detector = new FileTypeDetector()
            response.contentType = detector.detectContentType(fax.file)
        } catch(FileTypeDetectionException e) {
            response.contentType = 'application/octet-stream'
        }
        response.setHeader('Content-disposition', "attachment;filename=${fax.file.name}")

        response.outputStream << fax.file.newInputStream()
        response.flushBuffer()

        historyService.downloadedFax(fax)
    }

    def resend = {
        def fax = OutgoingFax.get(params.id)
        if(!fax) {
            flash.errorMessage = 'Fax not found'
            redirect(action: 'create')
            return
        }

        faxSenderService.send(fax)
        redirect(action: 'sending', id: fax.id)
    }

    def save = { MultiFileUploadCommand command ->
        def user = authenticatedUser

        def uploaded = [] as List
        def names = [] as List
        def subdir = System.currentTimeMillis() as String
        command.files.each { file ->
            if(file instanceof MultipartFile) {
                log.debug "Handling file [${file.originalFilename}]"
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
        fax.sourceFiles.addAll(uploaded)
        fax.sender = user

        if(fax.validate() && fax.save()) {
            faxSenderService.send(fax)
            redirect(action: 'sending', id: fax.id)
        } else {
            render(view: 'create', model: [fax: fax])
        }
    }

    def sending = {
        def fax = OutgoingFax.get(params.id)
        if(!fax) {
            flash.errorMessage = 'Fax not found'
            redirect(action: 'create')
            return
        }

        render(view: 'sending', model: [fax: fax])
    }

    def status = {
        def fax = OutgoingFax.get(params.id)
        if(!fax) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }

        render(contentType: 'application/json') {
            attempts = fax.attempts
            pages = fax.pages
            delegate.status = fax.status
        }
    }

    // TODO add success/error spinner images
}
