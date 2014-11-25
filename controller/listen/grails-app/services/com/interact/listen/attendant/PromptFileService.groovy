package com.interact.listen.attendant

import org.springframework.web.multipart.MultipartFile

class PromptFileService
{
    static transactional = false

    def grailsApplication
    
    File save(File storage, MultipartFile prompt, def organizationId)
    {
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

    /***
     * Overridden save method to correct the old filthy code, which didn't even save in the organization's dir.
     *
     * @param storage Directory location
     * @param prompt The prompt to save.
     * @param organizationId Organization ID of the user
     * @return The file that was saved.
     */
    File save(String storage, MultipartFile prompt, def organizationId)
    {
        String fileName = buildFilename(storage, organizationId);
        File dir = new File(fileName, "")
        return save(dir, prompt, organizationId);
    }

    def listNames(File storageLocation, def organizationId)
    {
        return storageLocation.listFiles().collect { it.name }.sort(java.text.Collator.instance)
    }

    def listNames(String storageLocation, def organizationId)
    {
        String fileName = buildFilename(storageLocation, organizationId);
        File dir = new File(fileName, "")
        return listNames(dir, organizationId);
    }

    private String buildFilename(String storageLocation, def organizationId)
    {
        return grailsApplication.config.com.interact.listen.artifactsDirectory + File.separatorChar +
                organizationId + File.separatorChar + storageLocation
    }
}
