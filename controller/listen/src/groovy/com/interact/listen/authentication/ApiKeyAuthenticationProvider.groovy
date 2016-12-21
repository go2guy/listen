package com.interact.listen.authentication

import org.apache.log4j.Logger
import org.joda.time.LocalDateTime
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority

class ApiKeyAuthenticationProvider implements AuthenticationProvider
{
    private static final Logger log = Logger.getLogger(ApiKeyAuthenticationProvider.class)

    def grailsApplication

    Authentication authenticate(Authentication auth)
    {
        if(auth.credentials.signature &&
                (auth.credentials.signature == grailsApplication.config.com.interact.listen.api.adminSignature))
        {
            return authenticateAdmin(auth);
        }

        if(auth.credentials.date && auth.credentials.signature)
        {
            def expected = Signature.create(auth.credentials.date)
            if(expected != auth.credentials.signature) {
                log.warn 'API signature did not match expected signature'
                log.debug "  Expected: [${expected}]"
                log.debug "  Actual:   [${auth.credentials.signature}]"
                throw new BadCredentialsException('API signature did not match expected signature')
            }

            def messageDate = HttpDate.parse(auth.credentials.date)
            LocalDateTime local = new LocalDateTime(messageDate.time)
            LocalDateTime now = new LocalDateTime()

            if(local.isBefore(now.minusMinutes(5)) || local.isAfter(now.plusMinutes(5)))
            {
                log.warn 'API request has expired'
                throw new BadCredentialsException('API request has expired')
            }

            def token = new ApiKeyAuthentication(name: auth.name,
                                                 credentials: auth.credentials,
                                                 principal: [id: ''],
                                                 authenticated: true)
            token.authorities= [new SimpleGrantedAuthority('ROLE_SPOT_API')]
            return token
        }
        else if(auth.credentials.apiKey)
        {
            def token = new ApiKeyAuthentication(name: auth.name,
                    credentials: auth.credentials,
                    principal: [id: ''],
                    authenticated: true)
            token.authorities= [new SimpleGrantedAuthority('ROLE_KEY_API')]
            return token
        }
        else
        {
            throw new BadCredentialsException('Invalid API Signature');
        }
    }

    /**
     * Authenticate a request as admin, bypass validation.
     *
     * @param auth The authentication.
     * @return The granted token.
     */
    private Authentication authenticateAdmin(Authentication auth)
    {
        log.warn("Authenticating request as Admin!");
        def token = new ApiKeyAuthentication(name: auth.name,
                credentials: auth.credentials,
                principal: [id: ''],
                authenticated: true)
        token.authorities= [new SimpleGrantedAuthority('ROLE_SPOT_API')]
        return token
    }

    boolean supports(Class authentication) {
        return ApiKeyAuthentication.class.isAssignableFrom(authentication)
    }
}
