package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.OutputBufferFilter;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.converter.FriendlyIso8601DateConverter;
import com.interact.listen.marshal.json.JsonMarshaller;
import com.interact.listen.resource.AccessNumber;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Criteria;
import org.hibernate.Session;

/**
 * Provides a GET implementation that retrieves a list of {@link Subscribers}.
 */
public class GetSubscriberServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        Subscriber subscriber = (Subscriber)(request.getSession().getAttribute("subscriber"));
        if(subscriber == null)
        {
            throw new UnauthorizedServletException("Not logged in");
        }

        StatSender statSender = (StatSender)request.getSession().getServletContext().getAttribute("statSender");
        if(statSender == null)
        {
            statSender = new InsaStatSender();
        }
        statSender.send(Stat.GUI_GET_SUBSCRIBER);

        if(request.getParameter("id") == null || request.getParameter("id").trim().equals(""))
        {
            throw new BadRequestServletException("Please provide an id");
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        Criteria criteria = session.createCriteria(Subscriber.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        Subscriber s = (Subscriber)session.get(Subscriber.class, Long.parseLong(request.getParameter("id")));

        if(!(subscriber.getIsAdministrator() || s.equals(subscriber)))
        {
            throw new UnauthorizedServletException("Unauthorized - Insufficient permissions");
        }
        
        Marshaller marshaller = new JsonMarshaller();
        marshaller.registerConverterClass(Date.class, FriendlyIso8601DateConverter.class);

        String content = marshalSubscriberToJson(s, marshaller);

        response.setStatus(HttpServletResponse.SC_OK);
        OutputBufferFilter.append(request, content, marshaller.getContentType());
    }

    public static String marshalSubscriberToJson(Subscriber subscriber, Marshaller marshaller)
    {
        StringBuilder json = new StringBuilder();

        json.append("{");
        json.append("\"id\":").append(subscriber.getId()).append(",");

        String username = marshaller.convertAndEscape(String.class, subscriber.getUsername());
        json.append("\"username\":\"").append(username).append("\",");

        String lastLogin = marshaller.convertAndEscape(Date.class, subscriber.getLastLogin());
        json.append("\"lastLogin\":\"").append(lastLogin).append("\"");

        json.append(",");
        String number = marshaller.convertAndEscape(String.class, subscriber.getNumber());
        json.append("\"number\":\"").append(number).append("\"");

        json.append(",");
        json.append("\"accessNumbers\":[");
        for(AccessNumber accessNumber : subscriber.getAccessNumbers())
        {
            json.append("\"").append(accessNumber.getNumber()).append("\",");
        }
        if(subscriber.getAccessNumbers().size() > 0)
        {
            json.deleteCharAt(json.length() - 1); // last comma
        }
        json.append("]");
        json.append("}");
        return json.toString();
    }
}
