package com.interact.listen.attendant

import com.interact.listen.util.FileTypeDetector
import org.springframework.web.multipart.MultipartFile
import org.joda.time.LocalDate

class PromptOverrideService {
    def historyService
    def promptFileService
    def springSecurityService

    // TODO fix hard-coded path
    static final File storageLocation = new File('/interact/listen/artifacts/attendant')
    
    PromptOverride create(def params, MultipartFile file = null) {

        // TODO using the user organization is a bit inconsistent, since the override
        // uses the organization of its MenuGroup; in most cases they will be the same.
        // if we allow custodians to manage them, we will need to change this
        def user = springSecurityService.getCurrentUser()
        def organization = user.organization
        log.debug "promptOverride create with params [${params}]"
        params.remove('date')
        def promptOverride = new PromptOverride()
        // Removing 'date' from the params because it's populated wit 'struct' and this was breaking the validation.
        params.remove('date')
        promptOverride.properties = params
        promptOverride.date = new LocalDate(params?.date_year.toInteger(), params?.date_month.toInteger(), params?.date_day.toInteger())
        promptOverride.validate(['date', 'useMenu', 'overridesMenu'])
        if (promptOverride.hasErrors()) {
            log.error "promptOverride has errors [${promptOverride.errors}]"
        }

        if(file && !file.isEmpty()) {
            def detector = new FileTypeDetector()
            def detectedType = detector.detectContentType(file.inputStream, file.originalFilename)
            if(detectedType != 'audio/x-wav') {
                promptOverride.optionsPrompt = file.originalFilename
                promptOverride.errors.rejectValue('optionsPrompt', 'promptOverride.optionsPrompt.must.be.wav')
            } else {
                def savedFile = promptFileService.save(storageLocation, file, organization.id)
                promptOverride.optionsPrompt = file.originalFilename
            }
        }

        if(!promptOverride.hasErrors() && promptOverride.save()) {
            historyService.createdAttendantHoliday(promptOverride)
        } else {
            log.error "promptOverride has failed with errors [${promptOverride.errors}]"
        }
        return promptOverride
    }

    void delete(PromptOverride promptOverride) {
        promptOverride.delete()
        historyService.deletedAttendantHoliday(promptOverride)
    }
}
