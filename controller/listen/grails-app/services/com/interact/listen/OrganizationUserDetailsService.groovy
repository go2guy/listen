package com.interact.listen

import grails.plugin.springsecurity.userdetails.GormUserDetailsService
import org.apache.log4j.Logger
//import org.codehaus.groovy.grails.plugins.springsecurity.GormUserDetailsService
import grails.plugin.springsecurity.SpringSecurityUtils
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException

class OrganizationUserDetailsService extends GormUserDetailsService {

    private Logger _log = Logger.getLogger(getClass())

//  def grailsApplication

    @Override
    UserDetails loadUserByUsername(String compositeUsername, boolean loadRoles) throws UsernameNotFoundException {
        def conf = SpringSecurityUtils.securityConfig
        Class<?> User = grailsApplication.getDomainClass(conf.userLookup.userDomainClassName).clazz

        _log.debug "Authenticating [${compositeUsername}] with loadRoles [${loadRoles}]"

        def user
        def username
        User.withTransaction { status ->
            if(compositeUsername.split(':').length == 2) {
                def s = compositeUsername.split(':')
                username = s[1]
                _log.debug "Look for organization based upon [${s[0]}]"
                def organization = Organization.get(s[0])

                _log.debug "Username [${username}], Organization [${organization.name}]"

                if(!organization) {
                    _log.warn "Organization not found with id [${s[0]}] for token [${compositeUsername}]"
                    throw new UsernameNotFoundException('User not found', username)
                }

                user = User.findWhere(organization: organization, (conf.userLookup.usernamePropertyName): username, isActiveDirectory: false)
                if(!user) {
                    _log.warn "User [${username}] not found for organization [${organization.id}:${organization.name}]"
                    throw new UsernameNotFoundException('User not found', username)
                }
            } else {
                // custodian login
                username = compositeUsername
                _log.debug "Username [${username}], no organization (custodian lookup)"

                user = User.findWhere((conf.userLookup.usernamePropertyName): username)

                if(!user) {
                    _log.warn "Custodian user not found with username [${username}]"
                    throw new UsernameNotFoundException('User not found', username)
                }

                if(!user.hasRole('ROLE_CUSTODIAN')) {
                    _log.warn "Custodian login attempted for user [${username}] but does not have correct role"
                    throw new UsernameNotFoundException('User not found', username)
                }
            }

            Collection<GrantedAuthority> authorities = loadAuthorities(user, username, loadRoles)
            return createUserDetails(user, authorities)
        }
    }

    @Override
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return loadUserByUsername(username, true)
    }

    protected Logger getLog() { _log }
}
