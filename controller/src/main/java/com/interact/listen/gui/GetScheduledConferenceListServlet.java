package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.OutputBufferFilter;
import com.interact.listen.ServletUtil;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.license.License;
import com.interact.listen.license.ListenFeature;
import com.interact.listen.license.NotLicensedException;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.converter.FriendlyIso8601DateConverter;
import com.interact.listen.marshal.json.JsonMarshaller;
import com.interact.listen.resource.*;
import com.interact.listen.stats.Stat;

import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

public class GetScheduledConferenceListServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        if(!License.isLicensed(ListenFeature.CONFERENCING))
        {
            throw new ServletException(new NotLicensedException(ListenFeature.CONFERENCING));
        }

        ServletUtil.sendStat(request, Stat.GUI_GET_SCHEDULED_CONFERENCE_LIST);

        Subscriber subscriber = ServletUtil.currentSubscriber(request);
        if(subscriber == null)
        {
            throw new UnauthorizedServletException("Not logged in");
        }

        String id = request.getParameter("id");
        if(id == null || id.trim().length() == 0)
        {
            throw new BadRequestServletException("Please provide an id");
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        Conference conference = (Conference)session.get(Conference.class, Long.parseLong(id));
        if(conference == null)
        {
            throw new BadRequestServletException("Conference not found");
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

        List<ScheduledConference> results = ScheduledConference.queryByConferencePaged(session, conference, first, max);
        long total = results.size() > 0 ? ScheduledConference.countByConference(session, conference) : 0;

        Marshaller marshaller = new JsonMarshaller();
        marshaller.registerConverterClass(Date.class, FriendlyIso8601DateConverter.class);
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"first\":").append(first).append(",");
        json.append("\"max\":").append(max).append(",");
        json.append("\"count\":").append(results.size()).append(",");
        json.append("\"total\":").append(total).append(",");
        json.append("\"results\":[");
        for(ScheduledConference result : results)
        {
            json.append(marshalScheduledConference(result, marshaller));
            json.append(",");
        }
        if(results.size() > 0)
        {
            json.deleteCharAt(json.length() - 1); // last comma
        }
        json.append("]}");
        OutputBufferFilter.append(request, json.toString(), marshaller.getContentType());
    }

    private String marshalScheduledConference(ScheduledConference scheduledConference, Marshaller marshaller)
    {
        StringBuilder json = new StringBuilder();
        json.append("{");

        json.append("\"id\":").append(scheduledConference.getId()).append(",");

        String startDate = marshaller.convertAndEscape(Date.class, scheduledConference.getStartDate());
        json.append("\"startDate\":\"").append(startDate).append("\",");

        String endDate = marshaller.convertAndEscape(Date.class, scheduledConference.getEndDate());
        json.append("\"endDate\":\"").append(endDate).append("\",");

        int activeCallers = scheduledConference.getActiveCallers().size();
        int passiveCallers = scheduledConference.getPassiveCallers().size();
        String callers = "";
        if(activeCallers > 0)
        {
            callers += activeCallers + " Active";
        }
        if(activeCallers > 0 && passiveCallers > 0)
        {
            callers += " / ";
        }
        if(passiveCallers > 0)
        {
            callers += passiveCallers + " Passive";
        }
        json.append("\"callers\":\"").append(callers).append("\",");
        json.append("\"topic\":\"").append(scheduledConference.getTopic()).append("\",");

        boolean future = scheduledConference.getStartDate().after(new Date());
        json.append("\"isFuture\":").append(future);

        json.append("}");
        return json.toString();
    }
}
