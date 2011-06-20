class RequestInformationFilters {
    def filters = {
        requestInformation(uri: '/**') {
            before = {
                if(shouldLog(controllerName, actionName)) {
                    log.debug "Request Details:"
                    log.debug "  Remote Address [${request.remoteAddr}]"
                    log.debug "  Method         [${request.method}]"
                    log.debug "  URI            [${request.requestURI}]"
                    log.debug "  Query String   [${request.queryString}]"
                
                    def names = request.headerNames
                    while(names.hasMoreElements()) {
                        def name = names.nextElement()
                        def value = request.getHeader(name)
                        log.debug "  Header         [${name}] = [${name.toLowerCase().contains('password') ? 'protected' : value}]"
                    }
                }
            }
        }
    }

    private boolean shouldLog(def controller, def action) {
        if(controller == 'voicemail' && ['pollingList', 'newCount'].contains(action)) {
            return false
        }
        if(controller == 'login' && action == 'authAjax') {
            return false
        }
        if(controller == 'conferencing' && ['polledConference', 'ajaxPagination'].contains(action)) {
            return false
        }
        return true
    }
}
