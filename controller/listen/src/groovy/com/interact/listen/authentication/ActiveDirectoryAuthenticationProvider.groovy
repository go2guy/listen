package com.interact.listen.authentication

import com.interact.listen.*

import javax.naming.Context
import javax.naming.NamingEnumeration
import javax.naming.NamingException
import javax.naming.directory.*
import javax.naming.AuthenticationException
import javax.naming.ldap.InitialLdapContext
import javax.naming.ldap.LdapContext

import org.apache.log4j.Logger
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.security.authentication.*
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UsernameNotFoundException

class ActiveDirectoryAuthenticationProvider implements AuthenticationProvider, ApplicationContextAware {
    private static final Logger log = Logger.getLogger(ActiveDirectoryAuthenticationProvider.class)

    ApplicationContext applicationContext

    def grailsApplication
    def userCreationService // not injected or wired - lazily initialized (below) using ApplicationContext
    def userDetailsService

    @Override
    public Authentication authenticate(Authentication auth) {
        // if for some reason we received a principal with an organization id, remove it
        def principal = auth.principal
        if(principal.contains(':')) {
            def index = principal.indexOf(':')
            principal = principal.substring(index + 1)
            log.debug "Removed organization id from principal [${auth.principal}], resulting in [${principal}]"
        }

        def ad = validateActiveDirectoryUser(principal, auth.credentials)
        if(!ad) {
            log.debug "User [${principal}] does not exist on Active Directory server"
            return null
        }

        createUserIfNotExists(principal, ad)

        try {
            def userDetails = userDetailsService.loadUserByUsername(principal)

            if(!userDetails.isAccountNonLocked()) {
                throw new LockedException('Account is locked', userDetails)
            }

            if(!userDetails.isEnabled()) {
                throw new DisabledException('Account is disabled', userDetails)
            }

            if(!userDetails.isAccountNonExpired()) {
                throw new AccountExpiredException('Account has expired', userDetails)
            }

            def result = new UsernamePasswordAuthenticationToken(userDetails, auth.credentials, userDetails.authorities)
            result.details = auth.details
            return result
        } catch(UsernameNotFoundException e) {
            log.warn "User row not found for AD user [${principal}]"
        }
    }

    private def validateActiveDirectoryUser(String username, String password) {
        def server = grailsApplication.config.com.interact.listen.activeDirectory.server
        def domain = grailsApplication.config.com.interact.listen.activeDirectory.domain

        def principal = "${username}@${domain}"
        def url = "ldap://${server}.${domain}/"

        def props = [:] as Hashtable
        props[Context.INITIAL_CONTEXT_FACTORY] = 'com.sun.jndi.ldap.LdapCtxFactory' as String
        props[Context.SECURITY_AUTHENTICATION] = 'simple' as String
        props[Context.SECURITY_PRINCIPAL] = principal as String
        props[Context.SECURITY_CREDENTIALS] = password as String
        props[Context.PROVIDER_URL] = url as String

        log.debug "AD authentication, principal = [{$principal}], url = [${url}]"

        try {
            LdapContext context = new InitialLdapContext(props, null)
            SearchResult searchResult = queryUserRecord(domain, principal, context)

            try {
                return [
                    displayName: extractAttribute(searchResult, 'displayName'),
                    mail: extractAttribute(searchResult, 'mail'),
                    telephoneNumber: extractAttribute(searchResult, 'telephoneNumber')
                ]
            } catch(NamingException e) {
                log.warn "Error extracting attribute data for user [${username}]"
            }
        } catch(AuthenticationException e) {
            try {
                def explanation = e.explanation
                int ldapErrorCode = Integer.parseInt(explanation.split('LDAP: error code')[1].split(' ')[0])
                switch(ldapErrorCode) {
                    case 49: // 49: invalid credentials
                        throw new BadCredentialsException('Invalid credentials', e)
                    default:
                        throw new AuthenticationServiceException('Authentication error', e)
                }
            } catch(NumberFormatException f) {
                log.warn 'Could not parse error information from explanation'
                throw new AuthenticationServiceException('Authentication error (could not identify explanation)', f)
            }
        } catch(NamingException e) {
            log.error e
            throw new AuthenticationServiceException('Authentication error', e)
        }

        throw new AuthenticationServiceException('Authentication error (Unknown)')
    }

    private void createUserIfNotExists(def username, def ad) {
        def exists = User.countByUsername(username) > 0
        if(exists) {
            return
        }

        log.debug "Creating new AD user [${username}], displayName [${ad.displayName}], mail [${ad.mail}]"

        def params = [
            username: username,
            realName: ad.displayName,
            emailAddress: ad.mail,
            pass: 'ad',
            confirm: 'ad'
        ]

        if(ad.telephoneNumber) {
            params.extension = ad.telephoneNumber
        }

        if(!userCreationService) {
            userCreationService = applicationContext.getBean('userCreationService')
        }

        User.withTransaction { status ->
            def organization = retrieveOrganization()
            def user = userCreationService.createUser(params, organization)
            if(user.hasErrors()) {
                log.error "Attempted to create new user, but there were validation errors: ${user.errors}"
                status.setRollbackOnly()
                throw new AuthenticationServiceException('Authentication error (could not create stub account)')
            }

            user.isActiveDirectory = true
            user.save(flush: true)
        }
    }

    private Organization retrieveOrganization() {
        // FIXME ugly organization query, only works for one organization
        def list = Organization.list(max: 1)
        return list.size() > 0 ? list[0] : null
    }

    /**
     * Given a domain name, returns an LDAP DC query string. For example, if the provided domain name were
     * "example.com", the DC query "DC=example,DC=com" would be returned.
     * 
     * @param domainName domain name to convert to DC query string
     * @return DC query string
     */
    private String toDC(String domainName)
    {
        StringBuilder dc = new StringBuilder();
        for(String token : domainName.split("\\."))
        {
            if(token.length() == 0)
            {
                continue;
            }
            if(dc.length() > 0)
            {
                dc.append(",");
            }
            dc.append("DC=").append(token);
        }
        return dc.toString();
    }

    private SearchResult queryUserRecord(String domain, String principal, DirContext context)
    {
        try
        {
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            String search = "(& (userPrincipalName=" + principal + ")(objectClass=user))";
            NamingEnumeration<SearchResult> results = context.search(toDC(domain), search, controls);
            if(!results.hasMore())
            {
                throw new AuthenticationServiceException("Cannot locate user information for [" + principal + "]");
            }
            return results.next();
        }
        catch(NamingException e)
        {
            throw new AuthenticationServiceException('Authentication error', e);
        }
    }

    private String extractAttribute(SearchResult result, String attributeName)
    {
        Attribute attribute = result.getAttributes().get(attributeName);
        if(attribute == null)
        {
            return null;
        }
        return attribute.get().toString();
    }

    boolean supports(Class authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication)
    }
}
