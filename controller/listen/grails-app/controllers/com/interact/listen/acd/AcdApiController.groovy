package com.interact.listen.acd

import com.interact.listen.Organization
import com.interact.listen.User
import com.interact.listen.fax.Fax
import com.interact.listen.fax.OutgoingFax
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.joda.time.DateTime

import javax.servlet.http.HttpServletResponse

//import grails.plugins.springsecurity.Secured
@Secured(['ROLE_KEY_API'])
class AcdApiController
{
    static allowedMethods = [
        updateAgent: 'PUT'
    ]

    def historyService;

    def updateAgent = {

        if(!params.id)
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameter [id]")
            log.warn 'Missing required parameter [id]'
            return
        }

        if(!params.apiKey)
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameter [apiKey]")
            log.warn 'Missing required parameter [apiKey]'
            return
        }

        if(!params.status)
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameter [status]")
            log.warn 'Missing required parameter [status]'
            return
        }

        def agentId = params.get("id");
        String toggle = params.get("status");
        String apiKey = params.get("apiKey");

        log.debug("agentId: " + agentId);
        log.debug("toggle: " + toggle);
        log.debug("apiKey: " + apiKey);

        Organization org = Organization.findByApiKey(apiKey);
        String statusSet = "";

        User agent = User.findByIdAndOrganization(agentId, org);
        if(agent != null)
        {
            if(toggle != null && toggle.equalsIgnoreCase(AcdQueueStatus.Available.toString()))
            {
                agent.getAcdUserStatus().setAcdQueueStatus(AcdQueueStatus.Available);
                agent.getAcdUserStatus().setStatusModified(DateTime.now());
                statusSet = AcdQueueStatus.Available.toString();
            }
            else if(toggle != null && toggle.equalsIgnoreCase(AcdQueueStatus.Unavailable.toString()))
            {
                agent.getAcdUserStatus().setAcdQueueStatus(AcdQueueStatus.Unavailable);
                agent.getAcdUserStatus().setStatusModified(DateTime.now());
                statusSet = AcdQueueStatus.Unavailable.toString();
            }
            else
            {
                log.warn("Bad status value: " + toggle);
                response.status = HttpServletResponse.SC_BAD_REQUEST;
            }

            if(agent.validate() && agent.save())
            {
                log.debug("Agent status updated.");
                historyService.acdApiSet(org, agent, statusSet);
                response.status = HttpServletResponse.SC_OK;
                render(contentType: 'application/json') {
                    id = agent.id;
                    status = agent.getAcdUserStatus().getAcdQueueStatus().toString();
                    statusModified = agent.getAcdUserStatus().getStatusModified();
                }
            }
            else
            {
                log.error("Unable to save agent.");
                response.status = HttpServletResponse.SC_BAD_REQUEST;
            }
        }
        else
        {
            log.warn("Bad agent value: " + agentId);
            render(status: HttpServletResponse.SC_NOT_FOUND, contentType: 'application/json',
                    text: '{"message": "The agent requested could not be found"}');
        }

        response.flushBuffer()
    }

    def getAgent = {
        if(!params.id)
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameter [id]")
            log.warn 'Missing required parameter [id]'
            return
        }

        if(!params.apiKey)
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameter [apiKey]")
            log.warn 'Missing required parameter [apiKey]'
            return
        }

        def agentId = params.get("id");
        String apiKey = params.get("apiKey");

        log.debug("agentId: " + agentId);
        log.debug("apiKey: " + apiKey);

        Organization org = Organization.findByApiKey(apiKey);

        User agent = User.findByIdAndOrganization(agentId, org);
        if(agent != null)
        {
            render(contentType: 'application/json') {
                id = agent.id;
                status = agent.getAcdUserStatus().getAcdQueueStatus().toString();
                statusModified = agent.getAcdUserStatus().getStatusModified();
            }
        }
        else
        {
            log.warn "Agent not found."
            render(status: HttpServletResponse.SC_NOT_FOUND, contentType: 'application/json',
                    text: '{"message": "The agent requested could not be found"}');
            return;
        }

        response.flushBuffer()
    }
}
