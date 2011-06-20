import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

class LogoutController {

    def historyService // injected
    def springSecurityService // injected

	def index = {
        historyService.loggedOut(springSecurityService.getCurrentUser())
		redirect uri: SpringSecurityUtils.securityConfig.logout.filterProcessesUrl // '/j_spring_security_logout'
	}
}
