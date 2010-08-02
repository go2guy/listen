package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.OutputBufferFilter;
import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;
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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.Session;

/**
 * Provides a GET implementation that retrieves a list of {@link Subscribers}.
 */
public class GetSubscriberServlet extends HttpServlet
{
    private static final Logger LOG = Logger.getLogger(GetSubscriberServlet.class);
    
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
        
        String id = request.getParameter("id");
        
        if(id == null || id.trim().equals(""))
        {
            LOG.debug("No id provided.  Returning information for subscriber in session.");
            id = String.valueOf(subscriber.getId());
        }
        
        if(!subscriber.getIsAdministrator() && !String.valueOf(subscriber.getId()).equals(id))
        {
            throw new UnauthorizedServletException("Unauthorized - Insufficient permissions");
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Subscriber s = (Subscriber)session.get(Subscriber.class, Long.parseLong(id));

        if(!(subscriber.getIsAdministrator() || s.equals(subscriber)))
        {
            throw new UnauthorizedServletException("Unauthorized - Insufficient permissions");
        }
        
        Marshaller marshaller = new JsonMarshaller();
        marshaller.registerConverterClass(Date.class, FriendlyIso8601DateConverter.class);

        String content = marshalSubscriberToJson(s, marshaller, session);

        response.setStatus(HttpServletResponse.SC_OK);
        OutputBufferFilter.append(request, content, marshaller.getContentType());
    }

    public static String marshalSubscriberToJson(Subscriber subscriber, Marshaller marshaller, Session session)
    {
        StringBuilder json = new StringBuilder();

        json.append("{");
        json.append("\"id\":").append(subscriber.getId()).append(",");

        String username = marshaller.convertAndEscape(String.class, subscriber.getUsername());
        json.append("\"username\":\"").append(username).append("\",");

        json.append("\"realName\":\"");
        if(subscriber.getRealName() != null)
        {
            String realName = marshaller.convertAndEscape(String.class, subscriber.getRealName());
            json.append(realName);
        }
        json.append("\",");

        String lastLogin = marshaller.convertAndEscape(Date.class, subscriber.getLastLogin());
        json.append("\"lastLogin\":\"").append(lastLogin).append("\"");

        json.append(",\"accessNumbers\":[");
        List<AccessNumber> accessNumbers = AccessNumber.queryBySubscriber(session, subscriber);
        for(AccessNumber accessNumber : accessNumbers)
        {
            json.append("\"").append(accessNumber.getNumber()).append("\",");
        }
        if(accessNumbers.size() > 0)
        {
            json.deleteCharAt(json.length() - 1); // last comma
        }
        json.append("]").append(",");
        
        json.append("\"voicemailPin\":").append(subscriber.getVoicemailPin()).append(",");
        
        json.append("\"enableEmail\":").append(subscriber.getIsEmailNotificationEnabled()).append(",");
        json.append("\"enableSms\":").append(subscriber.getIsSmsNotificationEnabled()).append(",");
        json.append("\"emailAddress\":\"").append(subscriber.getEmailAddress()).append("\",");
        json.append("\"smsAddress\":\"").append(subscriber.getSmsAddress()).append("\",");
        json.append("\"voicemailPlaybackOrder\":\"").append(subscriber.getVoicemailPlaybackOrder().toString()).append("\",");
        json.append("\"enablePaging\":").append(subscriber.getIsSubscribedToPaging()).append(",");

        //temporary until after-hours pager is moved to it's final location
        json.append("\"pagerNumber\":\"").append(Configuration.get(Property.Key.PAGER_NUMBER)).append("\",");
        json.append("\"pagerAlternateNumber\":\"").append(Configuration.get(Property.Key.ALTERNATE_NUMBER)).append("\",");
        json.append("\"pagePrefix\":\"").append(Configuration.get(Property.Key.PAGE_PREFIX)).append("\",");
        json.append("\"isActiveDirectory\":").append(subscriber.getIsActiveDirectory());
        json.append("}");
        return json.toString();
    }
}
