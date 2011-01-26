package com.interact.listen.api;

import com.interact.listen.HibernateUtil;
import com.interact.listen.OutputBufferFilter;
import com.interact.listen.ServletUtil;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.resource.AccessNumber;
import com.interact.listen.resource.FindMeNumber;
import com.interact.listen.resource.Subscriber;

import java.util.*;
 
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.json.simple.JSONArray;

public class FindMeNumbersServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        String href = ServletUtil.getNotNullNotEmptyString("subscriber", request, "subscriber");
        String destination = ServletUtil.getNotNullNotEmptyString("destination", request, "destination");
        Long id = Marshaller.getIdFromHref(href);
        
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Subscriber subscriber = Subscriber.queryById(session, id);
        if(subscriber == null)
        {
            throw new BadRequestServletException("Subscriber not found");
        }
        
        TreeMap<Integer, List<FindMeNumber>> groups = FindMeNumber.queryBySubscriberInPriorityGroups(session, subscriber, false);
        
        //No find me configuration for this subscriber, return just the number or where it's forwarded to in 
        //the expected json format.
        if(groups.size() == 0)
        {
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
        OutputBufferFilter.append(request, json.toJSONString(), "application/json");
    }
    
    private FindMeNumber checkForForwarding(Session session, FindMeNumber findMeNumber)
    {
        FindMeNumber updatedNumber = (FindMeNumber)findMeNumber.copy(false);
        AccessNumber accessNumber = AccessNumber.queryByNumber(session, findMeNumber.getNumber());
        if(accessNumber != null)
        {
            updatedNumber.setNumber(accessNumber.isForwarded() ? accessNumber.getForwardedTo() : accessNumber.getNumber());
        }
        
        return updatedNumber;
    }
}
