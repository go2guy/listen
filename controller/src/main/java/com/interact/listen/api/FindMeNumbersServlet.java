package com.interact.listen.api;

import com.interact.listen.HibernateUtil;
import com.interact.listen.OutputBufferFilter;
import com.interact.listen.ServletUtil;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.resource.FindMeNumber;
import com.interact.listen.resource.Subscriber;

import java.util.Set;
import java.util.TreeMap;

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
        Long id = Marshaller.getIdFromHref(href);
        
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Subscriber subscriber = Subscriber.queryById(session, id);
        if(subscriber == null)
        {
            throw new BadRequestServletException("Subscriber not found");
        }
        
        TreeMap<Integer, Set<FindMeNumber>> groups = FindMeNumber.queryBySubscriberInPriorityGroups(session, subscriber, false);
        JSONArray json = FindMeNumber.groupsToJson(groups);
        OutputBufferFilter.append(request, json.toJSONString(), "application/json");
    }
}
