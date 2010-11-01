package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.OutputBufferFilter;
import com.interact.listen.ServletUtil;
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
import com.interact.listen.stats.Stat;
import com.interact.listen.util.DateUtil;

import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.joda.time.Duration;

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

        ServletUtil.sendStat(request, Stat.GUI_GET_VOICEMAIL_LIST);

        Subscriber subscriber = ServletUtil.currentSubscriber(request);
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

        boolean bubbleNew = false;
        if(request.getParameter("bubbleNew") != null)
        {
            bubbleNew = Boolean.parseBoolean(request.getParameter("bubbleNew"));
        }

        String sort = "dateCreated";
        if(request.getParameter("sort") != null && request.getParameter("sort").equals("received"))
        {
            sort = "dateCreated";
        }
        boolean ascending = "ascending".equals(request.getParameter("order"));

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        List<Voicemail> results = Voicemail.queryBySubscriberPaged(session, subscriber, first, max, bubbleNew, sort, ascending);
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
        json.append("\"leftBy\":\"").append(leftBy).append("\",");

        Duration duration = new Duration(Long.parseLong(voicemail.getDuration()));
        json.append("\"duration\":\"").append(DateUtil.printDuration(duration)).append("\",");

        json.append("\"transcription\":");
        if(voicemail.getTranscription() == null || voicemail.getTranscription().trim().equals(""))
        {
            json.append("null");
        }
        else
        {
            String transcription = marshaller.convertAndEscape(String.class, voicemail.getTranscription());
            json.append("\"").append(transcription).append("\"");
        }

        json.append("}");
        return json.toString();
    }
}
