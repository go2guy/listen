import com.interact.listen.FilterUtil
import com.interact.listen.FilterUtil.*

class RequestInformationFilters {
    def filters = {
        requestInformation(uri: '/**') {
            before = {
                if(new FilterUtil().shouldLog(controllerName, actionName)) {
                    log.debug "Request Details:"

                    log.debug "  Params organization context  [${params.organizationContext}]"
                    log.debug "  Session organization context [${session.organizationContext}]"
                    log.debug "  Session organization object  [${session.organization?.name}]"

                    log.debug "  Remote Address [${request.remoteAddr}]"
                    log.debug "  Method         [${request.method}]"
                    log.debug "  URI            [${request.requestURI}]"
                    log.debug "  Query String   [${request.queryString}]"

                    log.debug "  Full Params: [${params}]"
                
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
}
