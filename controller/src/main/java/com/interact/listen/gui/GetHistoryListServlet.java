package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.OutputBufferFilter;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.converter.FriendlyIso8601DateConverter;
import com.interact.listen.marshal.json.JsonMarshaller;
import com.interact.listen.resource.*;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;

public class GetHistoryListServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        StatSender statSender = (StatSender)request.getSession().getServletContext().getAttribute("statSender");
        if(statSender == null)
        {
            statSender = new InsaStatSender();
        }
        statSender.send(Stat.GUI_GET_HISTORY_LIST);

        Subscriber subscriber = (Subscriber)request.getSession().getAttribute("subscriber");
        if(subscriber == null)
        {
            throw new UnauthorizedServletException("Not logged in");
        }

        if(!subscriber.getIsAdministrator())
        {
            throw new UnauthorizedServletException("Insufficient privileges");
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        int max = 50;
        int first = 0;

        if(request.getParameter("first") != null)
        {
            first = Integer.parseInt(request.getParameter("first"));
        }

        if(request.getParameter("max") != null)
        {
            max = Integer.parseInt(request.getParameter("max"));
        }

        Criteria criteria = session.createCriteria(History.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.setFirstResult(first);
        criteria.setMaxResults(max);
        criteria.addOrder(Order.desc("date"));
        List<History> results = (List<History>)criteria.list();

        long total = 0;
        if(results.size() > 0)
        {
            Criteria countCriteria = session.createCriteria(History.class);
            countCriteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            countCriteria.setFirstResult(0);
            countCriteria.setProjection(Projections.rowCount());
            total = (Long)countCriteria.list().get(0);
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

        String subscriber = marshaller.convertAndEscape(String.class, record.getSubscriber().getUsername());
        json.append("\"subscriber\":\"").append(subscriber).append("\",");

        String service = marshaller.convertAndEscape(String.class, record.getService());
        json.append("\"service\":\"").append(service).append("\",");

        String duration = marshaller.convertAndEscape(Long.class, record.getDuration());
        json.append("\"duration\":\"").append(duration).append("\",");

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

        String subscriber = marshaller.convertAndEscape(String.class,
                                                        history.getSubscriber() == null ? "" : history.getSubscriber().getUsername());
        json.append("\"subscriber\":\"").append(subscriber).append("\",");

        String action = marshaller.convertAndEscape(String.class, history.getAction());
        json.append("\"action\":\"").append(action).append("\",");

        String description = marshaller.convertAndEscape(Long.class, history.getDescription());
        json.append("\"description\":\"").append(description).append("\",");

        String onSubscriber = marshaller.convertAndEscape(String.class,
                                                          history.getOnSubscriber() == null ? "" : history.getOnSubscriber().getUsername());
        json.append("\"onSubscriber\":\"").append(onSubscriber).append("\",");

        String channel = marshaller.convertAndEscape(String.class, history.getChannel().toString());
        json.append("\"channel\":\"").append(channel).append("\"");

        json.append("}");
        return json.toString();
    }
}
