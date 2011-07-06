import com.interact.listen.Organization
import javax.servlet.http.HttpServletResponse

class ExtractOrganizationFilters {
    def filters = {
        extractOrganization(uri: '/**') {
            before = {
                if(params.organizationContext) {
                    session.organizationContext = params.organizationContext
                }

                if(session.organizationContext) {
                    if(session.organizationContext == 'custodian') {
                        session.organization = null
                        log.debug "Organization context is 'custodian'"
                        return true
                    }

                    def organization = Organization.findByContextPath(session.organizationContext)
                    if(!organization) {
                        session.organizationContext = null
                        log.warn "Organization not found for context [${session.organizationContext}]"
                        response.sendError(HttpServletResponse.SC_NOT_FOUND)
                        return false
                    }

                    session.organization = organization
                } else {
                    log.warn "No organization in session"
                    response.sendError(HttpServletResponse.SC_NOT_FOUND)
                    return false
                }
            }
        }
    }
}
