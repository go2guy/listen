package com.interact.listen.attendant

import org.springframework.web.multipart.MultipartFile

class PromptFileService {
    static transactional = false
    
    File save(File storage, MultipartFile prompt, def organizationId) {
        def dir = new File(storage, "")
        log.debug("Path of file ${dir.absolutePath}");
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

    def listNames(def storageLocation, def organizationId) {
        def dir = new File(storageLocation, "")
        return storageLocation.listFiles().collect { it.name }.sort(java.text.Collator.instance)
    }
}
