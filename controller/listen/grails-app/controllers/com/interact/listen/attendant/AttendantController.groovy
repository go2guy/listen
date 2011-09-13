package com.interact.listen.attendant

import com.interact.listen.util.FileTypeDetector
import grails.plugins.springsecurity.Secured
import org.json.simple.JSONArray
import org.json.simple.JSONValue

@Secured(['ROLE_ATTENDANT_ADMIN'])
class AttendantController {
    static allowedMethods = [
        index: 'GET',
        addHoliday: 'POST',
        deleteHoliday: 'POST',
        holidays: 'GET',
        menu: 'GET',
        save: 'POST'
    ]

    def menuGroupService
    def promptFileService
    def promptOverrideService

    def index = {
        redirect(action: 'menu')
    }

    def addHoliday = {
        def promptOverride = promptOverrideService.create(params, request.getFile('uploadedPrompt'))
        if(promptOverride.hasErrors()) {
            def model = promptOverrideModel()
            model.newPromptOverride = promptOverride
            render(view: 'holidays', model: model)
        } else {
            flash.successMessage = 'Holiday created'
            redirect(action: 'holidays')
        }
    }

    def deleteHoliday = {
        def promptOverride = PromptOverride.get(params.id)
        if(!promptOverride) {
            flash.errorMessage = 'Holiday not found'
            redirect(action: 'holidays')
            return
        }

        promptOverrideService.delete(promptOverride)
        flash.successMessage = 'Holiday deleted'
        redirect(action: 'holidays')
    }

    def holidays = {
        render(view: 'holidays', model: promptOverrideModel())
    }

    def menu = {
        def groups = MenuGroup.findAllByOrganization(authenticatedUser.organization, [sort: 'isDefault', order: 'desc'])
        render(view: 'menu', model: [groups: groups])
    }

    def save = {
        JSONArray jsonGroups = (JSONArray)JSONValue.parse(params.groups)
        log.debug "Received menu for saving: ${jsonGroups.toJSONString()}"

        try {
            menuGroupService.saveGroups(jsonGroups)
            flash.successMessage = 'Menu saved'
            redirect(action: 'menu')
        } catch(MenuGroupValidationException e) {
            render(view: 'menu', model: [groups: e.groups])
        }
    }

    def uploadPrompt = {
        def file = request.getFile('uploadFile')
        if(!file) {
            render('Please select a file to upload')
            return
        }

        def detector = new FileTypeDetector()
        def detectedType = detector.detectContentType(file.inputStream, file.originalFilename)
        if(detectedType != 'audio/x-wav') {
            render('File must be a wav file')
            return
        }

        def user = authenticatedUser
        promptFileService.save(file, user.organization.id)

        render('Success')
    }

    private def promptOverrideModel() {
        def user = authenticatedUser
        return [
            promptOverrideList: PromptOverride.findAllByOrganizationAndNotPast(user.organization, [sort: 'date', order: 'asc'])
        ]
    }
}
