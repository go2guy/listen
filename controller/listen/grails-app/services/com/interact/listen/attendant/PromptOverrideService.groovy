package com.interact.listen.attendant

import org.springframework.web.multipart.MultipartFile

class PromptOverrideService {
    def promptFileService
    def springSecurityService

    PromptOverride create(def params, MultipartFile file = null) {

        // TODO using the user organization is a bit inconsistent, since the override
        // uses the organization of its MenuGroup; in most cases they will be the same.
        // if we allow custodians to manage them, we will need to change this
        def user = springSecurityService.getCurrentUser()
        def organization = user.organization

        if(file && !file.isEmpty()) {
            def savedFile = promptFileService.save(file, organization.id)
            params.optionsPrompt = savedFile.name
        }

        def promptOverride = new PromptOverride(params)
        if(promptOverride.validate() && promptOverride.save()) {
            // TODO history?
        }
        return promptOverride
    }

    void delete(PromptOverride promptOverride) {
        promptOverride.delete()
        // TODO history?
    }
}
