package com.interact.listen.attendant

import com.interact.listen.util.FileTypeDetector
import org.springframework.web.multipart.MultipartFile

class PromptOverrideService {
    def historyService
    def promptFileService
    def springSecurityService

    PromptOverride create(def params, MultipartFile file = null) {

        // TODO using the user organization is a bit inconsistent, since the override
        // uses the organization of its MenuGroup; in most cases they will be the same.
        // if we allow custodians to manage them, we will need to change this
        def user = springSecurityService.getCurrentUser()
        def organization = user.organization

        def promptOverride = new PromptOverride(params)
        promptOverride.validate(['date', 'useMenu', 'overridesMenu'])

        if(file && !file.isEmpty()) {
            def detector = new FileTypeDetector()
            def detectedType = detector.detectContentType(file.inputStream, file.originalFilename)
            if(detectedType != 'audio/x-wav') {
                promptOverride.optionsPrompt = file.originalFilename
                promptOverride.errors.rejectValue('optionsPrompt', 'promptOverride.optionsPrompt.must.be.wav')
            } else {
                def savedFile = promptFileService.save(file, organization.id)
                promptOverride.optionsPrompt = file.originalFilename
            }
        }

        if(!promptOverride.hasErrors() && promptOverride.save()) {
            historyService.createdAttendantHoliday(promptOverride)
        }
        return promptOverride
    }

    void delete(PromptOverride promptOverride) {
        promptOverride.delete()
        historyService.deletedAttendantHoliday(promptOverride)
    }
}
