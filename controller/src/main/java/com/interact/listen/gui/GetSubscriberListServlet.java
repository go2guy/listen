package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.OutputBufferFilter;
import com.interact.listen.ServletUtil;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.converter.FriendlyIso8601DateConverter;
import com.interact.listen.marshal.json.JsonMarshaller;
import com.interact.listen.resource.Resource;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.stats.Stat;

import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

/**
 * Provides a GET implementation that retrieves a list of Subscribers.
 */
public class GetSubscriberListServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        Subscriber subscriber = ServletUtil.currentSubscriber(request);
        if(subscriber == null)
        {
            throw new UnauthorizedServletException("Not logged in");
        }

        ServletUtil.sendStat(request, Stat.GUI_GET_SUBSCRIBER_LIST);

        if(!subscriber.getIsAdministrator())
        {
            throw new UnauthorizedServletException("Insufficient permissions");
        }

        int first = 0;
        int max = Resource.DEFAULT_PAGE_SIZE;
        if(request.getParameter("first") != null)
        {
            first = Integer.parseInt(request.getParameter("first"));
        }
        if(request.getParameter("max") != null)
        {
            max = Integer.parseInt(request.getParameter("max"));
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        List<Subscriber> results = Subscriber.queryAllPaged(session, first, max);
        long total = results.size() > 0 ? Subscriber.count(session) : 0;

        Marshaller marshaller = new JsonMarshaller();
        marshaller.registerConverterClass(Date.class, FriendlyIso8601DateConverter.class);
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"first\":").append(first).append(",");
        json.append("\"max\":").append(max).append(",");
        json.append("\"count\":").append(results.size()).append(",");
        json.append("\"total\":").append(total).append(",");
        json.append("\"results\":[");
        for(Subscriber result : results)
        {
            json.append(GetSubscriberServlet.marshalSubscriberToJson(result, marshaller, session, subscriber));
            json.append(",");
        }
        if(results.size() > 0)
        {
            json.deleteCharAt(json.length() - 1);
        }
        json.append("]}");
        OutputBufferFilter.append(request, json.toString(), marshaller.getContentType());
    }
}
