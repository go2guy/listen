package com.interact.listen.api;

import com.interact.listen.HibernateUtil;
import com.interact.listen.OutputBufferFilter;
import com.interact.listen.ServletUtil;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.resource.*;
import com.interact.listen.util.DateUtil;

import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.joda.time.LocalDateTime;
import org.json.simple.JSONArray;

public class FindMeNumbersServlet extends HttpServlet
{
    private static final Logger LOG = Logger.getLogger(FindMeNumbersServlet.class);
    private static final long serialVersionUID = 1L;
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        String href = ServletUtil.getNotNullNotEmptyString("subscriber", request, "subscriber");
        String destination = ServletUtil.getNotNullNotEmptyString("destination", request, "destination");
        String includeDisabled = ServletUtil.getNotNullNotEmptyString("includeDisabled", request, "includeDisabled");
        Long id = Marshaller.getIdFromHref(href);
        
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Subscriber subscriber = Subscriber.queryById(session, id);
        if(subscriber == null)
        {
            throw new BadRequestServletException("Subscriber not found");
        }
        
        Boolean includeDisabledBoolean = Boolean.valueOf(includeDisabled);
        
        TreeMap<Integer, List<FindMeNumber>> groups = FindMeNumber.queryBySubscriberInPriorityGroups(session,
                                                                                                     subscriber,
                                                                                                     includeDisabledBoolean);

        // see if the config is expired (null expiration date == expired) 
        boolean isExpired = true;
        if(subscriber.getFindMeExpiration() != null)
        {
            LocalDateTime now = new LocalDateTime();
            LocalDateTime expires = DateUtil.toJoda(subscriber.getFindMeExpiration());
            if(expires.isAfter(now))
            {
                isExpired = false;
            }
        }
        
        //query local copies of black listed and explicitly allowed numbers
        List<String> denyRestrictions = extractDestinationsFromRestrictions(CallRestriction.queryEveryoneAndSubscriberSpecficByDirective(
                                                                                            session,
                                                                                            subscriber,
                                                                                            CallRestriction.Directive.DENY));

        List<String> allowRestrictions = extractDestinationsFromRestrictions(CallRestriction.queryEveryoneAndSubscriberSpecficByDirective(
                                                                                             session,
                                                                                             subscriber,
                                                                                             CallRestriction.Directive.ALLOW));

        LOG.debug("Is Find Me configuration expired? " + isExpired);

        if(groups.size() == 0 || isExpired)
        {
            groups.clear(); // in case we're expired and groups.size() > 0 [we don't want to use any of the groups]

            // No find me configuration for this subscriber, return just the number or where it's forwarded to in 
            // the expected json format.

            FindMeNumber findmeNumber = new FindMeNumber();
            //Set the destination as the number dialed, then check if it's forwarded
            findmeNumber.setNumber(destination);
            
            AccessNumber destinationAccessNumber = AccessNumber.queryByNumber(session, destination);
            if(destinationAccessNumber != null && destinationAccessNumber.isForwarded())
            {
                //Ensure that the subscriber is able to forward their number to this number, otherwise
                //destination remains what was dialed
                if(subscriber.canDial(session, destinationAccessNumber.getForwardedTo(), denyRestrictions, allowRestrictions))
                {
                    findmeNumber.setNumber(destinationAccessNumber.getForwardedTo());
                }
            }
            
            //hardcoded duration per Ladi's suggestion.  Improvements are possible.
            findmeNumber.setDialDuration(25);
            findmeNumber.setEnabled(Boolean.TRUE);
            
            List<FindMeNumber> findMeNumbers = new ArrayList<FindMeNumber>(1);
            findMeNumbers.add(findmeNumber);
            groups.put(0, findMeNumbers);
        }
        else
        {
            TreeMap<Integer, List<FindMeNumber>> updatedGroups = (TreeMap<Integer, List<FindMeNumber>>)groups.clone();
            
            //Check each find me number to see if it maps to an access number that may have been forwarded or is blacklisted
            for(Map.Entry<Integer, List<FindMeNumber>> entry : groups.entrySet())
            {
                List<FindMeNumber> updatedNumberSet = new ArrayList<FindMeNumber>();
                for(FindMeNumber number : entry.getValue())
                {
                    //make this domain have the forwarded number as it's number if appropriate
                    FindMeNumber updatedNumber = checkForForwarding(session, number, subscriber, denyRestrictions, allowRestrictions);
                    
                    //Only add this number in the tier if it isn't blacklisted
                    if(subscriber.canDial(session, updatedNumber.getNumber(), denyRestrictions, allowRestrictions))
                    {
                        updatedNumberSet.add(updatedNumber);
                    }
                }
                
                //only add a tier to the groups collection if it contains numbers to be dialed
                if(updatedNumberSet.size() > 0)
                {
                    updatedGroups.put(entry.getKey(), updatedNumberSet);
                }
                else
                {
                    //Tier was now empty (after checking for blacklisted numbers) so remove it from groups
                    updatedGroups.remove(entry.getKey());
                }
            }
            
            groups = updatedGroups;
        }
        
        //if only blacklisted numbers were configured we don't have any tiers. Add the dialed number so we return something
        if(groups.size() == 0)
        {
            FindMeNumber findmeNumber = new FindMeNumber();
            findmeNumber.setNumber(destination);
            findmeNumber.setDialDuration(25);
            findmeNumber.setEnabled(Boolean.TRUE);
            
            List<FindMeNumber> findMeNumbers = new ArrayList<FindMeNumber>(1);
            findMeNumbers.add(findmeNumber);
            groups.put(0, findMeNumbers);
        }
        
        JSONArray json = FindMeNumber.groupsToJson(groups);
        
        //Need to strip out the escaping that json simple does by default
        String returnString = StringUtils.remove(json.toJSONString(), "\\");
        
        OutputBufferFilter.append(request, returnString, "application/json");
    }
    
    private FindMeNumber checkForForwarding(Session session, FindMeNumber findMeNumber, Subscriber subscriber,
                                            List<String> denyRestrictions, List<String> allowRestrictions)
    {
        FindMeNumber updatedNumber = (FindMeNumber)findMeNumber.copy(true);
        updatedNumber.setNumber(findMeNumber.getNumber());
        
        AccessNumber accessNumber = AccessNumber.queryByNumber(session, findMeNumber.getNumber());
        if(accessNumber != null && accessNumber.isForwarded())
        {
            //if an access number is forwarded to a blacklisted number, return the original number, not the forwarded one
            if(subscriber.canDial(session, accessNumber.getForwardedTo(), denyRestrictions, allowRestrictions))
            {
                updatedNumber.setNumber(accessNumber.getForwardedTo());
            }
        }
        
        return updatedNumber;
    }
    
    private List<String> extractDestinationsFromRestrictions(List<CallRestriction> restrictions)
    {
        List<String> destinations = new ArrayList<String>();
        for(CallRestriction restriction : restrictions)
        {
            destinations.add(restriction.getDestination());
        }
        return destinations;
    }
}
