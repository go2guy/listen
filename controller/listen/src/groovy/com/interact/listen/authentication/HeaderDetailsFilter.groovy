package com.interact.listen.authentication

import com.interact.listen.Organization

import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.apache.commons.codec.binary.Base64
import org.apache.log4j.Logger
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.GenericFilterBean

class HeaderDetailsFilter extends GenericFilterBean
{
    private static final Logger log = Logger.getLogger(HeaderDetailsFilter.class)
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
            def date = ((HttpServletRequest) request).getHeader(DATE_HEADER)
            def signature = ((HttpServletRequest) request).getHeader(SIGNATURE_HEADER)
            def username = ((HttpServletRequest) request).getHeader(USERNAME_HEADER)
            def password = ((HttpServletRequest) request).getHeader(PASSWORD_HEADER)
            def apiKey = request.getParameter(APIKEY_PARAM);

            def token
            if (date && signature)
            {
                token = new ApiKeyAuthentication(name: '',
                        credentials: [date: date, signature: signature],
                        principal: [id: ''],
                        authenticated: true)
                log.debug 'Attempting API key header authentication'
            }
            else if (username && password)
            {
                // FIXME ugly organization query, only works for one organization
                def organization
                Organization.withTransaction {
                    def list = Organization.list(max: 1)
                    organization = list.size() > 0 ? list[0] : null
                }

                username = new String(Base64.decodeBase64(username))
                password = new String(Base64.decodeBase64(password))
                token = new UsernamePasswordAuthenticationToken((organization ? "${organization.id}:" : '') + username, password)
                log.debug 'Attempting username/password header authentication'
            }
            else if(apiKey != null && !apiKey.isEmpty())
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
                }
            }
            else
            {
                log.warn 'Not enough header information to perform authentication'
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
