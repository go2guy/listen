import com.interact.listen.stats.Stat
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

class LogoutController {

    def historyService
    def springSecurityService
    def statWriterService

	def index = {
        historyService.loggedOut(springSecurityService.getCurrentUser())
        statWriterService.send(Stat.GUI_LOGOUT)

        def url = SpringSecurityUtils.securityConfig.logout.filterProcessesUrl
        if(session.organization) {
            url += "?spring-security-redirect=/${session.organization.contextPath}"
        } else if(session.organizationContext == 'custodian') {
            url += "?spring-security-redirect=/custodian"
        }

        redirect(uri: url)
	}
}
