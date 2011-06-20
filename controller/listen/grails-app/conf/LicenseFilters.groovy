import com.interact.listen.license.ListenFeature

class LicenseFilters {
    def licenseService // injected

    def filters = {
        verifyLicensed(uri: '/**') {
            before = {
                if(!isLicensed(controllerName, actionName)) {
                    redirect(controller: 'login', action: 'denied')
                    return false
                }
                return true
            }
        }
    }

    private boolean isLicensed(def controllerName, def actionName) {
        switch(controllerName) {

            case 'attendant':
                return licenseService.isLicensed(ListenFeature.ATTENDANT)

            case 'conferencing':
                return licenseService.isLicensed(ListenFeature.CONFERENCING)

            case 'voicemail':
                return licenseService.isLicensed(ListenFeature.VOICEMAIL)

        }
        return true
    }
}
