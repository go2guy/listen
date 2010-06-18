package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.OutputBufferFilter;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.converter.FriendlyIso8601DateConverter;
import com.interact.listen.marshal.json.JsonMarshaller;
import com.interact.listen.resource.CallDetailRecord;
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

public class GetCallHistoryListServlet extends HttpServlet
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
        statSender.send(Stat.GUI_GET_CALLDETAILRECORD_LIST);

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

        Criteria criteria = session.createCriteria(CallDetailRecord.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        List<CallDetailRecord> records = (List<CallDetailRecord>)criteria.list();

        Marshaller marshaller = new JsonMarshaller();
        marshaller.registerConverterClass(Date.class, FriendlyIso8601DateConverter.class);

        StringBuilder json = new StringBuilder();
        json.append("[");
        for(CallDetailRecord record : records)
        {
            json.append(marshalCallDetailRecord(record, marshaller));
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

    private String marshalCallDetailRecord(CallDetailRecord record, Marshaller marshaller)
    {
        StringBuilder json = new StringBuilder();

        json.append("{");
        json.append("\"id\":").append(record.getId()).append(",");

        String date = marshaller.convertAndEscape(Date.class, record.getDateStarted());
        json.append("\"date\":\"").append(date).append("\",");

        String subscriber = marshaller.convertAndEscape(String.class, record.getSubscriber().getNumber());
        json.append("\"subscriber\":\"").append(subscriber).append("\",");

        String service = marshaller.convertAndEscape(String.class, record.getService());
        json.append("\"service\":\"").append(service).append("\",");

        String duration = marshaller.convertAndEscape(Long.class, record.getDuration());
        json.append("\"duration\":\"").append(duration).append("\",");

        String ani = marshaller.convertAndEscape(String.class, record.getAni());
        json.append("\"ani\":\"").append(ani).append("\",");

        String dnis = marshaller.convertAndEscape(String.class, record.getDnis());
        json.append("\"dnis\":\"").append(dnis).append("\"");

        json.append("}");
        return json.toString();
    }
}
