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

import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Criteria;
import org.hibernate.Session;
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

        Criteria cdrCriteria = session.createCriteria(CallDetailRecord.class);
        cdrCriteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        cdrCriteria.setFirstResult(first);
        cdrCriteria.setMaxResults(max);
        List<CallDetailRecord> cdrs = (List<CallDetailRecord>)cdrCriteria.list();

        Criteria actionCriteria = session.createCriteria(ActionHistory.class);
        actionCriteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        actionCriteria.setFirstResult(first);
        actionCriteria.setMaxResults(max);
        List<ActionHistory> actions = (List<ActionHistory>)actionCriteria.list();

        long cdrTotal = 0;
        long actionTotal = 0;

        if(cdrs.size() > 0)
        {
            Criteria cdrCountCriteria = session.createCriteria(CallDetailRecord.class);
            cdrCountCriteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            cdrCountCriteria.setFirstResult(0);
            cdrCountCriteria.setProjection(Projections.rowCount());
            cdrTotal = (Long)cdrCountCriteria.list().get(0);
        }

        if(actions.size() > 0)
        {
            Criteria actionCountCriteria = session.createCriteria(ActionHistory.class);
            actionCountCriteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            actionCountCriteria.setFirstResult(0);
            actionCountCriteria.setProjection(Projections.rowCount());
            actionTotal = (Long)actionCountCriteria.list().get(0);
        }

        List<Resource> combined = new ArrayList<Resource>();
        combined.addAll(cdrs);
        combined.addAll(actions);

        Collections.sort(combined, new Comparator<Resource>()
        {
            public final int compare(Resource a, Resource b)
            {
                Date dateA = getDate(a);
                Date dateB = getDate(b);
                return dateA.compareTo(dateB) * -1;
            }

            private final Date getDate(Resource resource)
            {
                if(resource instanceof CallDetailRecord)
                {
                    return ((CallDetailRecord)resource).getDateStarted();
                }
                else if(resource instanceof ActionHistory)
                {
                    return ((ActionHistory)resource).getDateCreated();
                }
                throw new AssertionError("Collection contained an unexpected Resource type");
            }
        });
        combined = combined.subList(0, Math.min(combined.size(), max));

        Marshaller marshaller = new JsonMarshaller();
        marshaller.registerConverterClass(Date.class, FriendlyIso8601DateConverter.class);

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"max\":").append(max).append(",");
        json.append("\"first\":").append(first).append(",");
        json.append("\"count\":").append(combined.size()).append(",");
        json.append("\"total\":").append(cdrTotal + actionTotal).append(",");
        json.append("\"results\":[");

        for(Resource resource : combined)
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
        if(combined.size() > 0)
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

        String date = marshaller.convertAndEscape(Date.class, record.getDateStarted());
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

        String date = marshaller.convertAndEscape(Date.class, history.getDateCreated());
        json.append("\"date\":\"").append(date).append("\",");

        String subscriber = marshaller.convertAndEscape(String.class,
                                                        history.getPerformedBySubscriber() == null ? "" : history.getPerformedBySubscriber().getUsername());
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
