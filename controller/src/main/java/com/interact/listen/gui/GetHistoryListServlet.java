package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.OutputBufferFilter;
import com.interact.listen.ServletUtil;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.converter.FriendlyIso8601DateConverter;
import com.interact.listen.marshal.json.JsonMarshaller;
import com.interact.listen.resource.*;
import com.interact.listen.stats.Stat;
import com.interact.listen.util.DateUtil;

import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

public class GetHistoryListServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        ServletUtil.sendStat(request, Stat.GUI_GET_HISTORY_LIST);

        Subscriber subscriber = ServletUtil.currentSubscriber(request);
        if(subscriber == null)
        {
            throw new UnauthorizedServletException("Not logged in");
        }

        if(!subscriber.getIsAdministrator())
        {
            throw new UnauthorizedServletException("Insufficient privileges");
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
        List<History> results = History.queryAllPaged(session, first, max);

        long total = 0;
        if(results.size() > 0)
        {
            total = History.count(session);
        }

        Marshaller marshaller = new JsonMarshaller();
        marshaller.registerConverterClass(Date.class, FriendlyIso8601DateConverter.class);

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"max\":").append(max).append(",");
        json.append("\"first\":").append(first).append(",");
        json.append("\"count\":").append(results.size()).append(",");
        json.append("\"total\":").append(total).append(",");
        json.append("\"results\":[");

        for(History resource : results)
        {
            if(resource instanceof CallDetailRecord)
            {
                json.append(marshalCallDetailRecord((CallDetailRecord)resource, marshaller));
            }
            else if(resource instanceof ActionHistory)
            {
                json.append(marshalActionHistory((ActionHistory)resource, marshaller));
            }
            json.append(",");
        }
        if(results.size() > 0)
        {
            json.deleteCharAt(json.length() - 1); // last comma
        }
        json.append("]}");

        response.setStatus(HttpServletResponse.SC_OK);
        OutputBufferFilter.append(request, json.toString(), marshaller.getContentType());
    }

    private String marshalCallDetailRecord(CallDetailRecord record, Marshaller marshaller)
    {
        StringBuilder json = new StringBuilder();

        json.append("{");
        json.append("\"type\":\"Call\",");

        json.append("\"id\":\"call").append(record.getId()).append("\",");

        String date = marshaller.convertAndEscape(Date.class, record.getDate());
        json.append("\"date\":\"").append(date).append("\",");

        if(record.getSubscriber() == null || record.getSubscriber().getUsername() == null)
        {
            json.append("\"subscriber\":\"\",");
        }
        else
        {
            String subscriber = marshaller.convertAndEscape(String.class, record.getSubscriber().getUsername());
            json.append("\"subscriber\":\"").append(subscriber).append("\",");
        }

        String service = marshaller.convertAndEscape(String.class, record.getService());
        json.append("\"service\":\"").append(service == null ? "unknown" : service.toLowerCase()).append("\",");

        String durationString = "";
        if(record.getDuration() != null)
        {
            durationString = DateUtil.printDuration(record.getDuration());
        }
        json.append("\"duration\":\"").append(durationString).append("\",");

        String ani = marshaller.convertAndEscape(String.class, record.getAni());
        json.append("\"ani\":\"").append(ani).append("\",");

        String dnis = marshaller.convertAndEscape(String.class, record.getDnis());
        json.append("\"dnis\":\"").append(dnis).append("\",");

        String direction = marshaller.convertAndEscape(CallDetailRecord.CallDirection.class, record.getDirection());
        json.append("\"direction\":\"").append(direction).append("\"");

        json.append("}");
        return json.toString();
    }

    private String marshalActionHistory(ActionHistory history, Marshaller marshaller)
    {
        StringBuilder json = new StringBuilder();

        json.append("{");
        json.append("\"type\":\"Action\",");

        json.append("\"id\":\"action").append(history.getId()).append("\",");

        String date = marshaller.convertAndEscape(Date.class, history.getDate());
        json.append("\"date\":\"").append(date).append("\",");

        if(history.getSubscriber() == null || history.getSubscriber().getUsername() == null)
        {
            json.append("\"subscriber\":\"System\",");
        }
        else
        {
            String subscriber = marshaller.convertAndEscape(String.class, history.getSubscriber().getUsername());
            json.append("\"subscriber\":\"").append(subscriber).append("\",");
        }

        String service = marshaller.convertAndEscape(String.class, history.getService());
        json.append("\"service\":\"").append(service == null ? "unknown" : service.toLowerCase()).append("\",");

        String action = marshaller.convertAndEscape(String.class, history.getAction());
        json.append("\"action\":\"").append(action).append("\",");

        String description = marshaller.convertAndEscape(Long.class, history.getDescription());
        json.append("\"description\":\"").append(description).append("\",");

        String channel = marshaller.convertAndEscape(String.class, history.getChannel().toString());
        json.append("\"channel\":\"").append(channel).append("\"");

        json.append("}");
        return json.toString();
    }
}
