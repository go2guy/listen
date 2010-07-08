package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.OutputBufferFilter;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.license.License;
import com.interact.listen.license.ListenFeature;
import com.interact.listen.license.NotLicensedException;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.converter.FriendlyIso8601DateConverter;
import com.interact.listen.marshal.json.JsonMarshaller;
import com.interact.listen.resource.Resource;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.Voicemail;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

public class GetVoicemailListServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        if(!License.isLicensed(ListenFeature.VOICEMAIL))
        {
            throw new ServletException(new NotLicensedException(ListenFeature.VOICEMAIL));
        }

        StatSender statSender = (StatSender)request.getSession().getServletContext().getAttribute("statSender");
        if(statSender == null)
        {
            statSender = new InsaStatSender();
        }
        statSender.send(Stat.GUI_GET_VOICEMAIL_LIST);

        Subscriber subscriber = (Subscriber)(request.getSession().getAttribute("subscriber"));
        if(subscriber == null)
        {
            throw new UnauthorizedServletException("Not logged in");
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
        List<Voicemail> results = Voicemail.queryBySubscriberPaged(session, subscriber, first, max);
        long total = results.size() > 0 ? Voicemail.countBySubscriber(session, subscriber) : 0;

        Marshaller marshaller = new JsonMarshaller();
        marshaller.registerConverterClass(Date.class, FriendlyIso8601DateConverter.class);

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"newCount\":").append(Voicemail.countNewBySubscriber(session, subscriber)).append(",");
        json.append("\"first\":").append(first).append(",");
        json.append("\"max\":").append(max).append(",");
        json.append("\"count\":").append(results.size()).append(",");
        json.append("\"total\":").append(total).append(",");
        json.append("\"results\":[");
        for(Voicemail result : results)
        {
            json.append(marshalVoicemail(result, marshaller));
            json.append(",");
        }
        if(results.size() > 0)
        {
            json.deleteCharAt(json.length() - 1);
        }
        json.append("]}");
        OutputBufferFilter.append(request, json.toString(), marshaller.getContentType());
    }

    private String marshalVoicemail(Voicemail voicemail, Marshaller marshaller)
    {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"id\":").append(voicemail.getId()).append(",");
        json.append("\"isNew\":").append(voicemail.getIsNew()).append(",");

        String date = marshaller.convertAndEscape(Date.class, voicemail.getDateCreated());
        json.append("\"dateCreated\":\"").append(date).append("\",");

        String leftBy = marshaller.convertAndEscape(String.class, voicemail.getLeftBy());
        json.append("\"leftBy\":\"").append(leftBy).append("\"");

        json.append("}");
        return json.toString();
    }
}
