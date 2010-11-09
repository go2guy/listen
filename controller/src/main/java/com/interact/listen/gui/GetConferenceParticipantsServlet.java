package com.interact.listen.gui;

import com.interact.listen.*;
import com.interact.listen.exception.ListenServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.history.Channel;
import com.interact.listen.license.License;
import com.interact.listen.license.ListenFeature;
import com.interact.listen.license.NotLicensedException;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.json.JsonMarshaller;
import com.interact.listen.resource.*;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

public class GetConferenceParticipantsServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        Conference conference = validateAndGetConference(request, Stat.GUI_GET_CONFERENCE_PARTICIPANTS);

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
        List<Participant> results = Participant.queryByConferencePaged(session, conference, first, max);
        long total = results.size() > 0 ? Participant.countByConference(session, conference) : 0;

        Marshaller marshaller = new JsonMarshaller();
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"first\":").append(first).append(",");
        json.append("\"max\":").append(max).append(",");
        json.append("\"count\":").append(results.size()).append(",");
        json.append("\"total\":").append(total).append(",");
        json.append("\"results\":[");
        for(Participant result : results)
        {
            json.append(marshalParticipant(result, marshaller));
            json.append(",");
        }
        if(results.size() > 0)
        {
            json.deleteCharAt(json.length() - 1);
        }
        json.append("]}");
        OutputBufferFilter.append(request, json.toString(), marshaller.getContentType());
    }

    private String marshalParticipant(Participant participant, Marshaller marshaller)
    {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"id\":").append(participant.getId()).append(",");
        json.append("\"isAdmin\":").append(participant.getIsAdmin()).append(",");
        json.append("\"isPassive\":").append(participant.getIsPassive()).append(",");
        json.append("\"isAdminMuted\":").append(participant.getIsAdminMuted()).append(",");

        String number = marshaller.convertAndEscape(String.class, participant.getNumber());
        json.append("\"number\":\"").append(number).append("\"");
        json.append("}");
        return json.toString();
    }

    public static Conference validateAndGetConference(HttpServletRequest request, Stat stat) throws ServletException
    {
        if(!License.isLicensed(ListenFeature.CONFERENCING))
        {
            throw new ServletException(new NotLicensedException(ListenFeature.CONFERENCING));
        }

        StatSender statSender = (StatSender)request.getSession().getServletContext().getAttribute("statSender");
        if(statSender == null)
        {
            statSender = new InsaStatSender();
        }
        statSender.send(stat);

        Subscriber subscriber = ServletUtil.currentSubscriber(request);
        if(subscriber == null)
        {
            throw new UnauthorizedServletException("Not logged in");
        }

        Subscriber currentSubscriber = ServletUtil.currentSubscriber(request);
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        PersistenceService persistenceService = new DefaultPersistenceService(session, currentSubscriber, Channel.GUI);

        String id = request.getParameter("id");

        Conference conference = GuiServletUtil.getConferenceFromIdOrSubscriber(id, subscriber, persistenceService);

        if(conference == null)
        {
            throw new ListenServletException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Conference not found",
                                             "text/plain");
        }

        if(!subscriber.equals(conference.getSubscriber()) && !subscriber.getIsAdministrator())
        {
            throw new UnauthorizedServletException("Conference does not belong to subscriber");
        }

        return conference;
    }
}
