package com.interact.listen.pbx

class ExtensionService {
    static transactional = true

    def cloudToDeviceService
    def messageLightService
    def spotCommunicationService
    def springSecurityService

    Extension create(def params, def checkPermission = true) {
        def user = springSecurityService.getCurrentUser()
        if(checkPermission && !user.hasRole('ROLE_ORGANIZATION_ADMIN')) {
            // TODO better exception type
            throw new AssertionError('Action not allowed')
        }

        def extension = new Extension(params)
        if(extension.validate() && extension.save()) {
            cloudToDeviceService.sendContactSync()
            messageLightService.toggle(extension)
            // TODO history?
        }

        return extension
    }

    void delete(Extension extension) {
        def user = springSecurityService.getCurrentUser()
        if(!user.hasRole('ROLE_ORGANIZATION_ADMIN')) {
            // TODO better exception type
            throw new AssertionError('Action not allowed')
        }

        extension.delete()
        
        // TODO history?

        cloudToDeviceService.sendContactSync()
        messageLightService.toggle(extension.number, extension.ip, false)
        if(extension.greeting) {
            spotCommunicationService.deleteArtifact(extension.greeting.uri)
        }
    }

    Extension update(Extension extension, def params) {
        def user = springSecurityService.getCurrentUser()

        def originalNumber = extension.number
        def originalIp = extension.ip

        if(user.hasRole('ROLE_ORGANIZATION_ADMIN')) {
            extension.properties = params
        } else {
            extension.properties['forwardedTo', 'greeting'] = params
        }

        if(extension.validate() && extension.save()) {
            cloudToDeviceService.sendContactSync()

            if(originalNumber != extension.number || originalIp != extension.ip) {
                messageLightService.toggle(originalNumber, originalIp, false)
                messageLightService.toggle(extension)
            }
            // TODO history?
        }

        return extension
    }
}
