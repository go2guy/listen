package com.interact.listen.attendant

import org.springframework.web.multipart.MultipartFile

class PromptFileService {
    static transactional = false

    // TODO fix hard-coded path
    static final File storage = new File('/interact/listen/artifacts/attendant')
    
    File save(MultipartFile prompt, def organizationId) {
        def dir = new File(storage, (String)organizationId)
        if(!dir.exists() && !dir.mkdirs()) {
            throw new FileNotFoundException('Could not use organization-specific prompt storage directory')
        }

        if(!dir.isDirectory()) {
            throw new IllegalStateException('Organization-specific storage path is not a directory')
        }

        log.debug "Adding prompt [${prompt.originalFilename}] to storage dir [${dir}]"

        def destination = new File(dir, prompt.originalFilename)
        prompt.transferTo(destination)
        return destination
    }

    def listNames(def organizationId) {
        def dir = new File(storage, (String)organizationId)
        return dir.listFiles().collect { it.name }
    }
}
