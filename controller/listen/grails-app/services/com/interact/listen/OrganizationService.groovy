package com.interact.listen

import com.interact.listen.pbx.SipPhone

class OrganizationService {
    def ldapService

    Organization create(def params, def features = [] as Set) {
        def organization = new Organization(params)
        features.each {
            organization.addToEnabledFeatures(it)
        }
        if(organization.validate() && organization.save()) {
            // TODO history
            ldapService.addOrganization(organization)
        }
        return organization
    }

    Organization parseFromSipContact(String sipAuthorization)
    {
        Organization theOrganization = null;

        if(sipAuthorization != null && !sipAuthorization.isEmpty())
        {
            if(log.isDebugEnabled())
            {
                log.debug("Attempting to retrieve organization from sipContact[" + sipAuthorization + "]");
            }

            String[] parse1 = sipAuthorization.split("@");
            if(parse1 != null && parse1.length > 0)
            {
                String parse2 = parse1[1].substring(0, parse1[1].length() - 1)
                if(parse2 != null && parse2.length() > 0)
                {
                    log.debug("Parsed sip IP: " + parse2);

                    SipPhone sipPhone = SipPhone.findByIp(parse2);
                    if(sipPhone != null)
                    {
                        theOrganization = sipPhone.organization;
                    }

                    if(log.isDebugEnabled() && theOrganization != null)
                    {
                        log.debug("Organization is : " + theOrganization.name);
                    }
                }
            }
        }

        return theOrganization;
    }
}
