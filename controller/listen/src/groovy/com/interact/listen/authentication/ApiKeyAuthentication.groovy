package com.interact.listen.authentication

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority

class ApiKeyAuthentication implements Authentication {
    String name
    Collection<GrantedAuthority> authorities
    Object credentials
    Object details
    Object principal
    boolean authenticated
}
