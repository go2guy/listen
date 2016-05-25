package com.interact.listen

import com.interact.listen.pbx.Extension
import com.interact.listen.pbx.NumberRoute
import com.interact.listen.pbx.SipPhone
import org.apache.log4j.Logger

class CallRoutingService
{
    def organizationService;

    static transactional = false

    def routeCall(def ani, def dnis, String authorization)
    {
        log.debug "route call request for [${ani}][${dnis}]"
        //Check external number routes first
        def mappings = [:]
        Organization callerOrganization = null;
        String callerId = null;
        String routeType = null;

        if(authorization == null || authorization.isEmpty())
        {
            log.warn("Didn't get sip authorization.");
            List<SingleOrganizationConfiguration> socList = SingleOrganizationConfiguration.list();
            //There's only one here
            if(socList != null && socList.size() > 0)
            {
                callerOrganization = socList.get(0).organization;
                if(log.isDebugEnabled())
                {
                    log.debug("Single organization mode enabled, using default org.");
                }
            }
        }

        if(callerOrganization == null)
        {
            //Look up organization by the user id that is making the call
            SipPhone sipPhone = SipPhone.findByPhoneUserId(ani);
            if(sipPhone != null)
            {
                callerOrganization = sipPhone.getOrganization();
                log.debug("Caller Organization: " + callerOrganization.getName());
            }
        }

        NumberRoute.findAllByType(NumberRoute.Type.EXTERNAL).each {
            mappings.put(it.pattern, it)
        }

        def matcher = new WildcardNumberMatcher()
        def mapping = matcher.findMatch(dnis, mappings)
        def extension
        if(mapping)
        {
            log.debug "returning because of mappings"
            def dmnExtension = ''

            if(mapping?.destination == 'Direct Message')
            {
                log.debug "Lookup direct message number [${dnis}]"
                def dmn = DirectMessageNumber.findByNumber(dnis)
                if(dmn && dmn.owner.enabled())
                {
                    log.debug "User [${dmn.owner?.username}] associated with direct message number [${dnis}]"
                    extension = Extension.findByOwner(dmn.owner)
                    if(extension) {
                        dmnExtension = extension.number
                        log.debug "Extension [${dmnExtension}] associated with direct message number [${dnis}]"
                    }
                }
            }

            //Check if caller is in the same organization
            if((callerOrganization != null) && (mapping.organization != callerOrganization))
            {
                //Caller is from another org. Set the callerId
                callerId = callerOrganization.outboundCallid;
                if(log.isDebugEnabled())
                {
                    log.debug("Setting external mapping callerId to : " + callerId);
                }
                routeType = "external";
            }
            else
            {
                if(callerOrganization != null)
                {
                    //Need to look up the actual callerid based on the inbound ani (phone's user id)
                    SipPhone callerSipPhone = SipPhone.findByOrganizationAndPhoneUserId(callerOrganization, ani);
                    if(callerSipPhone != null && callerSipPhone.getExtension() != null)
                    {
                        callerId = callerSipPhone.getExtension().getNumber();
                        log.debug("Setting internal mapping callerId to " + callerId);
                        routeType = "internal";
                    }
                }
                else
                {
                    //This is an external call
                    callerId = ani;
                    routeType = "external";
                }
            }

            return [application: mapping.destination, organization: mapping.organization, dmnExtension: dmnExtension,
                callerId: callerId, routeType: routeType];
        }

        if(callerOrganization == null)
        {
            //Unknown ip dialed a non-external route.  We do not know what organization to use here, so fail
            log.error "call with unknown organization"
            return null
        }
        else
        {
            //Need to look up the actual callerid based on the inbound ani (phone's user id)
            SipPhone callerSipPhone = SipPhone.findByOrganizationAndPhoneUserId(callerOrganization, ani);
            if(callerSipPhone != null && callerSipPhone.getExtension() != null)
            {
                callerId = callerSipPhone.getExtension().getNumber();
                log.debug("Setting callerId to " + callerId);
                routeType = "internal";
            }
        }

        mappings.clear()
        NumberRoute.findAllByOrganizationAndType(callerOrganization, NumberRoute.Type.INTERNAL).each {
                mappings.put(it.pattern, it)
        }

        mapping = matcher.findMatch(dnis, mappings)

        if(mapping)
        {
            log.debug "returning final with mappings"
            return [application: mapping.destination, organization: callerOrganization, callerId: callerId,
                    routeType: routeType];
        }
        else
        {
            log.debug "Returning without application mappings"
            return null
        }
    }
    
    def determineOutboundCallId(def user) {
        log.debug ("Determine outbound caller id for user [${user?.realName}] with org [${user?.organization?.name}]")
        
        if(user?.organization?.outboundCallidByDid){
            def did = DirectInwardDialNumber.findByOwner(user)
            if(did){
                log.debug "Setting outbound callid by DID [${did?.number}]"
                return did?.number
            } else {
                log.debug "extension [${user?.realName}] does not have a did"
                return user?.organization?.outboundCallid
            }
        } else {
            log.debug "Setting outbound callid by organizational default"
            return user?.organization?.outboundCallid
        }
        return null
    }
}
