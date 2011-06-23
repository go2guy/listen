import com.interact.listen.stats.Stat
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

class LogoutController {

    def historyService
    def springSecurityService
    def statWriterService

	def index = {
        historyService.loggedOut(springSecurityService.getCurrentUser())
        statWriterService.send(Stat.GUI_LOGOUT)
		redirect uri: SpringSecurityUtils.securityConfig.logout.filterProcessesUrl // '/j_spring_security_logout'
	}
}
