import com.interact.listen.Organization
import javax.servlet.http.HttpServletResponse

import static com.interact.listen.FilterUtil.*

class ExtractOrganizationFilters {
    def filters = {
        extractOrganization(uri: '/**') {
            before = {
                if(shouldLog(controllerName, actionName)) {
                    log.debug "Extracting organization for controller [${controllerName}], action [${actionName}]"
                }
                if(!shouldExtractOrganization(controllerName, actionName)) {
                    if(shouldLog(controllerName, actionName)) {
                        log.debug "Skipping organization extract for ${controllerName}/${actionName}"
                    }
                    return true
                }

                if(params.organizationContext) {
                    session.organizationContext = params.organizationContext
                }

                if(session.organizationContext) {
                    if(session.organizationContext == 'custodian') {
                        session.organization = null
                        if(shouldLog(controllerName, actionName)) {
                            log.debug "Organization context is 'custodian'"
                        }
                        return true
                    }

                    def organization = Organization.findByContextPath(session.organizationContext)
                    if(!organization) {
                        session.organizationContext = null
                        if(shouldLog(controllerName, actionName)) {
                            log.warn "Organization not found for context [${session.organizationContext}]"
                        }
                        response.sendError(HttpServletResponse.SC_NOT_FOUND)
                        return false
                    }

                    session.organization = organization
                } else {
                    if(shouldLog(controllerName, actionName)) {
                        log.warn "No organization in session"
                    }
                    response.sendError(HttpServletResponse.SC_NOT_FOUND)
                    return false
                }
            }
        }
    }
}
