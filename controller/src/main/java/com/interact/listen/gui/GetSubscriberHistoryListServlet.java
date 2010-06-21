package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.OutputBufferFilter;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.converter.FriendlyIso8601DateConverter;
import com.interact.listen.marshal.json.JsonMarshaller;
import com.interact.listen.resource.History;
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

import org.hibernate.Criteria;
import org.hibernate.Session;

public class GetSubscriberHistoryListServlet extends HttpServlet
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
        statSender.send(Stat.GUI_GET_SUBSCRIBERHISTORY_LIST);

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

        Criteria criteria = session.createCriteria(History.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        List<History> records = (List<History>)criteria.list();

        Marshaller marshaller = new JsonMarshaller();
        marshaller.registerConverterClass(Date.class, FriendlyIso8601DateConverter.class);

        StringBuilder json = new StringBuilder();
        json.append("[");
        for(History record : records)
        {
            json.append(marshalHistory(record, marshaller));
            json.append(",");
        }
        if(records.size() > 0)
        {
            json.deleteCharAt(json.length() - 1); // last comma
        }
        json.append("]");

        response.setStatus(HttpServletResponse.SC_OK);
        OutputBufferFilter.append(request, json.toString(), marshaller.getContentType());
    }
    
    private String marshalHistory(History history, Marshaller marshaller)
    {
        StringBuilder json = new StringBuilder();

        json.append("{");
        json.append("\"id\":").append(history.getId()).append(",");

        String date = marshaller.convertAndEscape(Date.class, history.getDateCreated());
        json.append("\"date\":\"").append(date).append("\",");

        String subscriber = marshaller.convertAndEscape(String.class, history.getPerformedBySubscriber() == null ? "" : history.getPerformedBySubscriber().getUsername());
        json.append("\"subscriber\":\"").append(subscriber).append("\",");

        String action = marshaller.convertAndEscape(String.class, history.getAction());
        json.append("\"action\":\"").append(action).append("\",");

        String description = marshaller.convertAndEscape(Long.class, history.getDescription());
        json.append("\"description\":\"").append(description).append("\",");

        String onSubscriber = marshaller.convertAndEscape(String.class, history.getOnSubscriber() == null ? "" : history.getOnSubscriber().getUsername());
        json.append("\"onSubscriber\":\"").append(onSubscriber).append("\",");

        String channel = marshaller.convertAndEscape(String.class, history.getChannel().toString());
        json.append("\"channel\":\"").append(channel).append("\"");

        json.append("}");
        return json.toString();
    }
}
