package com.interact.listen.authentication

import com.interact.listen.Organization
import org.apache.log4j.Logger
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.GenericFilterBean

import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletResponse

class ApiKeyFilter extends GenericFilterBean
{
    private static final Logger log = Logger.getLogger(ApiKeyFilter.class)
    private static final def DATE_HEADER = 'Date'
    private static final def SIGNATURE_HEADER = 'X-Listen-Signature'
    private static final def USERNAME_HEADER = 'X-Listen-AuthenticationUsername'
    private static final def PASSWORD_HEADER = 'X-Listen-AuthenticationPassword'
    private static final def APIKEY_PARAM = 'apiKey';

    def authenticationManager

    @SuppressWarnings('ReturnNullFromCatchBlock')
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    {
        if (!SecurityContextHolder.context.authentication)
        {
            def apiKey = request.getParameter(APIKEY_PARAM);

            def token
            if(apiKey != null && !apiKey.isEmpty())
            {
                log.debug("API KEY: " + apiKey);
                Organization org = Organization.findByApiKey(apiKey);
                if(org != null)
                {
                    log.debug("Organization: " + org);
                    token = new ApiKeyAuthentication(name: '',
                            credentials: [apiKey: apiKey],
                            principal: [id: ''],
                            authenticated: true);
                    log.debug 'Attempting API key authentication';
                }
                else
                {
                    log.debug("Invalid API Key");
                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    StringBuffer buffer = new StringBuffer();
                    buffer.append("{\"message\": \"Please use a valid API Key\"}");
                    response.getOutputStream().write(buffer.toString().getBytes());
                    response.getOutputStream().flush();
                    return;
                }
            }
            else
            {
                log.warn('API Key not provided.');
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                StringBuffer buffer = new StringBuffer();
                buffer.append("{\"message\": \"Please use a valid API Key\"}");
                response.getOutputStream().write(buffer.toString().getBytes());
                response.getOutputStream().flush();
                return;
            }

            if (token)
            {
                try
                {
                    token = authenticationManager.authenticate(token)
                    if (token)
                    {
                        log.debug 'Successfully authenticated using header details'
                        SecurityContextHolder.context.authentication = token
                    }
                } catch (AuthenticationException e)
                {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
                    return
                }
            }
        }
        chain.doFilter(request, response)

        // only keep the authentication valid for one request
        SecurityContextHolder.context.authentication = null
    }
}
