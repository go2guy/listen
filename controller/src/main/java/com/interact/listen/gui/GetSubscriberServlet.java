package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.OutputBufferFilter;
import com.interact.listen.ServletUtil;
import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.converter.FriendlyIso8601DateConverter;
import com.interact.listen.marshal.json.JsonMarshaller;
import com.interact.listen.resource.AccessNumber;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.TimeRestriction;
import com.interact.listen.resource.TimeRestriction.Action;
import com.interact.listen.stats.Stat;

import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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
        Subscriber subscriber = ServletUtil.currentSubscriber(request);
        if(subscriber == null)
        {
            throw new UnauthorizedServletException("Not logged in");
        }

        ServletUtil.sendStat(request, Stat.GUI_GET_SUBSCRIBER);
        
        String id = request.getParameter("id");
        
        if(id == null || id.trim().equals(""))
        {
            LOG.debug("No id provided.  Returning information for subscriber in session.");
            id = String.valueOf(subscriber.getId());
        }
        
        if(!subscriber.getIsAdministrator() && !String.valueOf(subscriber.getId()).equals(id))
        {
            throw new UnauthorizedServletException("Insufficient permissions");
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Subscriber s = Subscriber.queryById(session, Long.valueOf(id));
        
        Marshaller marshaller = new JsonMarshaller();
        marshaller.registerConverterClass(Date.class, FriendlyIso8601DateConverter.class);

        String content = marshalSubscriberToJson(s, marshaller, session, ServletUtil.currentSubscriber(request));

        response.setStatus(HttpServletResponse.SC_OK);
        OutputBufferFilter.append(request, content, marshaller.getContentType());
    }

    public static String marshalSubscriberToJson(Subscriber subscriber, Marshaller marshaller, Session session,
                                                 Subscriber currentSubscriber)
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

        json.append("\"workEmailAddress\":\"");
        if(subscriber.getWorkEmailAddress() != null)
        {
            String email = marshaller.convertAndEscape(String.class, subscriber.getWorkEmailAddress());
            json.append(email);
        }
        json.append("\",");

        String lastLogin = marshaller.convertAndEscape(Date.class, subscriber.getLastLogin());
        json.append("\"lastLogin\":\"").append(lastLogin).append("\"");

        json.append(",\"accessNumbers\":[");
        List<AccessNumber> accessNumbers = AccessNumber.queryBySubscriber(session, subscriber);
        for(AccessNumber accessNumber : accessNumbers)
        {
            json.append("{");
            json.append("\"number\":\"").append(accessNumber.getNumber()).append("\",");
            json.append("\"messageLight\":").append(accessNumber.getSupportsMessageLight() ? "true" : "false").append(",");
            json.append("\"numberType\":\"").append(accessNumber.getNumberType().name()).append("\",");
            json.append("\"publicNumber\":").append(accessNumber.getPublicNumber() ? "true" : "false");
            json.append("},");
        }
        if(accessNumbers.size() > 0)
        {
            json.deleteCharAt(json.length() - 1); // last comma
        }
        json.append("]").append(",");

        json.append("\"voicemailPin\":\"").append(subscriber.getVoicemailPin()).append("\",");

        json.append("\"enableEmail\":").append(subscriber.getIsEmailNotificationEnabled()).append(",");
        json.append("\"emailAddress\":\"").append(subscriber.getEmailAddress()).append("\",");
        List<TimeRestriction> emailTimeRestrictions = TimeRestriction.queryBySubscriberAndAction(session, subscriber, Action.NEW_VOICEMAIL_EMAIL);
        json.append("\"emailTimeRestrictions\":").append(timeRestrictionsToJson(emailTimeRestrictions)).append(",");
        
        json.append("\"enableSms\":").append(subscriber.getIsSmsNotificationEnabled()).append(",");
        json.append("\"smsAddress\":\"").append(subscriber.getSmsAddress()).append("\",");
        List<TimeRestriction> smsTimeRestrictions = TimeRestriction.queryBySubscriberAndAction(session, subscriber, Action.NEW_VOICEMAIL_SMS);
        json.append("\"smsTimeRestrictions\":").append(timeRestrictionsToJson(smsTimeRestrictions)).append(",");

        json.append("\"voicemailPlaybackOrder\":\"").append(subscriber.getVoicemailPlaybackOrder().toString()).append("\",");
        json.append("\"enablePaging\":").append(subscriber.getIsSubscribedToPaging()).append(",");
        json.append("\"enableTranscription\":").append(subscriber.getIsSubscribedToTranscription()).append(",");
        json.append("\"enableAdmin\":").append(subscriber.getIsAdministrator()).append(",");

        //temporary until after-hours pager is moved to it's final location
        json.append("\"pagerNumber\":\"").append(Configuration.get(Property.Key.PAGER_NUMBER)).append("\",");
        json.append("\"pagerAlternateNumber\":\"").append(Configuration.get(Property.Key.ALTERNATE_NUMBER)).append("\",");
        json.append("\"pagePrefix\":\"").append(Configuration.get(Property.Key.PAGE_PREFIX)).append("\",");

        json.append("\"isActiveDirectory\":").append(subscriber.getIsActiveDirectory()).append(",");
        json.append("\"isCurrentSubscriber\":").append(subscriber.equals(currentSubscriber));
        json.append("}");
        return json.toString();
    }

    private static String timeRestrictionsToJson(List<TimeRestriction> restrictions)
    {
        JSONArray array = new JSONArray();
        for(TimeRestriction restriction : restrictions)
        {
            JSONObject obj = new JSONObject();
            obj.put("from", restriction.getStartEntry());
            obj.put("to", restriction.getEndEntry());

            JSONObject days = new JSONObject();
            days.put("monday", restriction.getMonday());
            days.put("tuesday", restriction.getTuesday());
            days.put("wednesday", restriction.getWednesday());
            days.put("thursday", restriction.getThursday());
            days.put("friday", restriction.getFriday());
            days.put("saturday", restriction.getSaturday());
            days.put("sunday", restriction.getSunday());
            obj.put("days", days);

            array.add(obj);
        }
        return array.toJSONString();
    }
}
