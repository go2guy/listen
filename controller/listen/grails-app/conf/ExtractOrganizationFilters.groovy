import com.interact.listen.Organization
import com.interact.listen.SingleOrganizationConfiguration
import javax.servlet.http.HttpServletResponse

import com.interact.listen.FilterUtil;

class ExtractOrganizationFilters {
    def filters = {
        extractOrganization(controller: 'login', action: 'auth') {
            before =
            {
                FilterUtil fu = new FilterUtil();
                if(log.isDebugEnabled() && fu.shouldLog(controllerName, actionName))
                {
                    log.debug "Extracting organization for controller [${controllerName}], action [${actionName}]"
                }
                if(!fu.shouldExtractOrganization(controllerName, actionName)) {
                    if(fu.shouldLog(controllerName, actionName)) {
                        log.debug "Skipping organization extract for ${controllerName}/${actionName}"
                    }
                    return true
                }

                if(SingleOrganizationConfiguration.exists() && params.organizationContext != 'custodian') {
                    session.organizationContext = SingleOrganizationConfiguration.retrieve().contextPath
                    if(fu.shouldLog(controllerName, actionName)) {
                        log.debug "Forcibly set session organization context to [${session.organizationContext}]"
                    }
                } else if(params.organizationContext) {
                    session.organizationContext = params.organizationContext
                }

                if(session.organizationContext) {
                    if(session.organizationContext == 'custodian') {
                        session.organization = null
                        if(fu.shouldLog(controllerName, actionName)) {
                            log.debug "Organization context is 'custodian'"
                        }
                        return true
                    }

                    def organization = Organization.findByContextPath(session.organizationContext)
                    if(!organization || !organization.enabled) {
                        session.organizationContext = null
                        if(fu.shouldLog(controllerName, actionName)) {
                            log.warn "Organization not found for context [${session.organizationContext}]"
                        }
                        response.sendError(HttpServletResponse.SC_NOT_FOUND)
                        return false
                    }

                    session.organization = organization
                } else {
                    if(fu.shouldLog(controllerName, actionName)) {
                        log.warn "No organization in session"
                    }
                    response.sendError(HttpServletResponse.SC_NOT_FOUND)
                    return false
                }
            }
        }
    }
}
