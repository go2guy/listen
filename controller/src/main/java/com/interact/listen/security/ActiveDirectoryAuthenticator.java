package com.interact.listen.security;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.log4j.Logger;

public class ActiveDirectoryAuthenticator
{
    private static final Logger LOG = Logger.getLogger(ActiveDirectoryAuthenticator.class);

    private final String server;
    private final String domain;

    public ActiveDirectoryAuthenticator(String server, String domain)
    {
        this.server = server;
        this.domain = domain;
    }

    public AuthenticationResult authenticate(String username, String password) throws AuthenticationException
    {
        AuthenticationResult result = new AuthenticationResult();

        String principal = username + "@" + domain;
        String url = "ldap://" + server + "." + domain + "/";

        Hashtable<String, String> props = new Hashtable<String, String>();
        props.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        props.put(Context.SECURITY_AUTHENTICATION, "simple");
        props.put(Context.SECURITY_PRINCIPAL, principal);
        props.put(Context.SECURITY_CREDENTIALS, password);
        props.put(Context.PROVIDER_URL, url);

        LOG.debug("SECURITY_PRINCIPAL = [" + principal + "]");
        LOG.debug("PROVIDER_URL = [" + url + "]");

        try
        {
            LdapContext context = new InitialLdapContext(props, null);
            SearchResult searchResult = queryUserRecord(principal, context);
            result.setGroups(extractUserGroups(searchResult, context));
            result.setSuccessful(true);

            try
            {
                result.setDisplayName(extractAttribute(searchResult, "displayName"));
                result.setTelephoneNumber(extractAttribute(searchResult, "telephoneNumber"));
            }
            catch(NamingException e)
            {
                LOG.warn("Error extracting attribute data for user [" + username + "]", e);
            }
        }
        catch(javax.naming.AuthenticationException e)
        {
            try
            {
                String explanation = e.getExplanation();
                int ldapErrorCode = Integer.parseInt(explanation.split("LDAP: error code ")[1].split(" ")[0]);
                // String adErrorCode = explanation.split("AcceptSecurityContext error, data ")[0].split(",")[0];
                switch(ldapErrorCode)
                {
                    case 49: // invalid credentials
                        result.setSuccessful(false);
                        break;
                    default:
                        throw new AuthenticationException(e);
                }
            }
            catch(NumberFormatException f)
            {
                LOG.error("Could not parse error information from explanation", f);
                throw new AuthenticationException(e);
            }
        }
        catch(NamingException e)
        {
            LOG.error(e);
            throw new AuthenticationException(e);
        }
        LOG.debug("AuthenticationResult: " + result);
        return result;
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

    private SearchResult queryUserRecord(String principal, DirContext context) throws AuthenticationException
    {
        try
        {
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            String search = "(& (userPrincipalName=" + principal + ")(objectClass=user))";
            NamingEnumeration<SearchResult> results = context.search(toDC(domain), search, controls);
            if(!results.hasMore())
            {
                throw new AuthenticationException("Cannot locate user information for [" + principal + "]");
            }
            return results.next();
        }
        catch(NamingException e)
        {
            throw new AuthenticationException(e);
        }
    }

    private Set<String> extractUserGroups(SearchResult result, DirContext context) throws AuthenticationException
    {
        try
        {
            Set<String> groups = new HashSet<String>();
            Attribute memberOf = result.getAttributes().get("memberOf");
            if(memberOf != null) // null if this user belongs to no group at all
            {
                for(int i = 0; i < memberOf.size(); i++)
                {
                    String[] commonName = new String[1];
                    commonName[0] = "CN";
                    Attributes attributes = context.getAttributes(memberOf.get(i).toString(), commonName);
                    Attribute attribute = attributes.get("CN");
                    groups.add(attribute.get().toString());
                }
            }
            return groups;
        }
        catch(NamingException e)
        {
            throw new AuthenticationException(e);
        }
    }

    private String extractAttribute(SearchResult result, String attributeName) throws NamingException
    {
        Attribute attribute = result.getAttributes().get(attributeName);
        if(attribute == null)
        {
            return null;
        }
        return attribute.get().toString();
    }
}
