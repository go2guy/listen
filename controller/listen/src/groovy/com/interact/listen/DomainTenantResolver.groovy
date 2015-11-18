package com.interact.listen

import grails.plugin.multitenant.core.resolve.TenantResolver
import grails.plugin.multitenant.core.exception.TenantResolveException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

/**
 * @see http://multi-tenant.github.com/grails-multi-tenant-single-db/
 */
class DomainTenantResolver implements TenantResolver {

    Integer resolve(HttpServletRequest request) throws TenantResolveException {
/*        */

        int orgId = -1;

        HttpSession session = request.getSession();

        if(session.organizationContext != null)
        {
            if(session.organizationContext == 'custodian')
            {
                session.organization = null
                log.debug "Organization context is 'custodian'"
                return -1;
            }
        }

        if(SingleOrganizationConfiguration.exists() && session.organizationContext != 'custodian')
        {
            session.organizationContext = SingleOrganizationConfiguration.retrieve().contextPath
        }
        else
        {
            String host = request.getServerName()
            def organization = Organization.findByContextPath(host)
            if(!organization || !organization.enabled)
            {
                session.organizationContext = null
                log.warn "Organization not found for context [" + host + "]"
                orgId = -1;
            }
            else
            {
                session.organization = organization
                session.organizationContext = organization.contextPath;
                orgId = organization.id;
            }
        }

/*        switch (host) {
            case "john.greatapp.com":
                return 1
            case "mary.greatapp.com":
                return 2
            case "paul.greatapp.com":
                return 3

            default:
                // WARNING: Returning null will disable the Hibernate filter
                // don't do this unless you know what you're doing!
                return null
        }
*/
/*        else
        {
            String a = request.getRequestURI();
            String[] paths = a.split('/');
            if(paths.length > 0)
            {
                for(String thisPath : paths)
                {
                    if(thisPath.equalsIgnoreCase("custodian"))
                    {
                        orgId = -1;
                        break;
                    }
                    Organization thisOrg = Organization.findByContextPath(thisPath);
                    if(thisOrg != null)
                    {
                        orgId = thisOrg.id;
                        break;
                    }
                }
            }
        }*/

        return orgId;
    }
}