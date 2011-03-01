package com.interact.listen.api;

import com.interact.listen.HibernateUtil;
import com.interact.listen.OutputBufferFilter;
import com.interact.listen.ServletUtil;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.resource.AccessNumber;
import com.interact.listen.resource.FindMeNumber;
import com.interact.listen.resource.Subscriber;
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

        LOG.debug("Is Find Me configuration expired? " + isExpired);

        if(groups.size() == 0 || isExpired)
        {
            groups = new TreeMap<Integer, List<FindMeNumber>>(); // if expired, we want to clear out the groups

            // No find me configuration for this subscriber, return just the number or where it's forwarded to in 
            // the expected json format.

            FindMeNumber findmeNumber = new FindMeNumber();
            AccessNumber destinationAccessNumber = AccessNumber.queryByNumber(session, destination);
            if(destinationAccessNumber != null)
            {
                findmeNumber.setNumber(destinationAccessNumber.isForwarded() ? destinationAccessNumber.getForwardedTo()
                                                                             : destinationAccessNumber.getNumber());
            }
            else
            {
                findmeNumber.setNumber(destination);
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
            //Check each find me number to see if it maps to an access number that may have been forwarded
            for(Map.Entry<Integer, List<FindMeNumber>> entry : groups.entrySet())
            {
                List<FindMeNumber> updatedNumberSet = new ArrayList<FindMeNumber>();
                for(FindMeNumber number : entry.getValue())
                {
                    updatedNumberSet.add(checkForForwarding(session, number));
                }
                groups.put(entry.getKey(), updatedNumberSet);
            }
        }
        
        JSONArray json = FindMeNumber.groupsToJson(groups);
        
        //Need to strip out the escaping that json simple does by default
        String returnString = StringUtils.remove(json.toJSONString(), "\\");
        
        OutputBufferFilter.append(request, returnString, "application/json");
    }
    
    private FindMeNumber checkForForwarding(Session session, FindMeNumber findMeNumber)
    {
        FindMeNumber updatedNumber = (FindMeNumber)findMeNumber.copy(true);
        AccessNumber accessNumber = AccessNumber.queryByNumber(session, findMeNumber.getNumber());
        if(accessNumber != null)
        {
            updatedNumber.setNumber(accessNumber.isForwarded() ? accessNumber.getForwardedTo() : accessNumber.getNumber());
        }
        
        return updatedNumber;
    }
}
