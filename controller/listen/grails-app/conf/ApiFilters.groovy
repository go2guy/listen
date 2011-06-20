import com.interact.listen.User
import com.interact.listen.history.Channel
import org.apache.log4j.Logger

class ApiFilters {
    static final Logger log = Logger.getLogger('com.interact.listen.ApiFilters')

    def hrefParserService

    def filters = {
        redirectedExtract(controller: 'spotApi', action: '*') {
            before = {
                def href = request.getHeader('X-Listen-Subscriber')
                if(href) {
                    log.debug "Found X-Listen-Subscriber header with value [${href}]"
                    def id = hrefParserService.idFromHref(href)
                    def user = User.get(id)
                    request.setAttribute('tui-user', user)
                    log.debug "Set request attribute [tui-user] to ${user}"
                }

                if(request.getHeader('X-Listen-Channel')) {
                    def channel = Channel.valueOf(request.getHeader('X-Listen-Channel'))
                    request.setAttribute('tui-channel', channel)
                    log.debug "Set request attribute [tui-channel] to ${channel}"
                }
            }
        }
    }
}
