package com.interact.listen.gui;

import com.interact.listen.*;
import com.interact.listen.ResourceListService.Builder;
import com.interact.listen.exception.CriteriaCreationException;
import com.interact.listen.exception.ListenServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.history.Channel;
import com.interact.listen.license.License;
import com.interact.listen.license.ListenFeature;
import com.interact.listen.license.NotLicensedException;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.converter.FriendlyIso8601DateConverter;
import com.interact.listen.marshal.converter.FriendlyPinTypeConverter;
import com.interact.listen.marshal.json.JsonMarshaller;
import com.interact.listen.resource.*;
import com.interact.listen.resource.Pin.PinType;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

public class GetConferenceInfoServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException
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
        statSender.send(Stat.GUI_GET_CONFERENCE_INFO);

        Subscriber subscriber = (Subscriber)(request.getSession().getAttribute("subscriber"));
        if(subscriber == null)
        {
            throw new UnauthorizedServletException("Not logged in");
        }

        Subscriber currentSubscriber = ServletUtil.currentSubscriber(request);
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        PersistenceService persistenceService = new PersistenceService(session, currentSubscriber, Channel.GUI);

        String id = request.getParameter("id");

        Marshaller marshaller = new JsonMarshaller();
        marshaller.registerConverterClass(Date.class, FriendlyIso8601DateConverter.class);
        marshaller.registerConverterClass(PinType.class, FriendlyPinTypeConverter.class);

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

        StringBuilder content = new StringBuilder();
        try
        {
            content.append("{");
            content.append("\"info\":").append(getInfo(conference, marshaller)).append(",");
            content.append("\"participants\":").append(getParticipants(conference, marshaller, session)).append(",");
            content.append("\"pins\":").append(getPins(conference, marshaller, session)).append(",");
            content.append("\"history\":").append(getHistory(conference, marshaller, session)).append(",");
            content.append("\"recordings\":").append(getRecordings(conference, marshaller, session));
            content.append("}");
        }
        catch(CriteriaCreationException e)
        {
            throw new ServletException(e);
        }

        response.setStatus(HttpServletResponse.SC_OK);
        OutputBufferFilter.append(request, content.toString(), marshaller.getContentType());
    }

    private String getInfo(Conference conference, Marshaller marshaller)
    {
        return marshaller.marshal(conference);
    }

    private String getParticipants(Conference conference, Marshaller marshaller, Session session) throws CriteriaCreationException
    {
        Builder builder = new ResourceListService.Builder(Participant.class, session, marshaller)
            .addSearchProperty("conference", "/conferences/" + conference.getId())
            .addReturnField("id")
            .addReturnField("isAdmin")
            .addReturnField("isAdminMuted")
            .addReturnField("isMuted")
            .addReturnField("isPassive")
            .addReturnField("number");
        ResourceListService service = builder.build();
        return service.list();
    }

    private String getPins(Conference conference, Marshaller marshaller, Session session) throws CriteriaCreationException
    {
        Builder builder = new ResourceListService.Builder(Pin.class, session, marshaller)
            .addSearchProperty("conference", "/conferences/" + conference.getId())
            .addReturnField("id")
            .addReturnField("number")
            .addReturnField("type")
            .sortBy("type", ResourceListService.SortOrder.ASCENDING);
        ResourceListService service = builder.build();
        return service.list();
    }

    private String getHistory(Conference conference, Marshaller marshaller, Session session) throws CriteriaCreationException
    {
        Builder builder = new ResourceListService.Builder(ConferenceHistory.class, session, marshaller)
            .addSearchProperty("conference", "/conferences/" + conference.getId())
            .addReturnField("dateCreated")
            .addReturnField("description")
            .addReturnField("id")
            .sortBy("dateCreated", ResourceListService.SortOrder.DESCENDING)
            .withMax(15);
        ResourceListService service = builder.build();
        return service.list();
    }

    private String getRecordings(Conference conference, Marshaller marshaller, Session session) throws CriteriaCreationException
    {
        Builder builder = new ResourceListService.Builder(ConferenceRecording.class, session, marshaller)
            .addSearchProperty("conference", "/conferences/" + conference.getId())
            .addReturnField("dateCreated")
            .addReturnField("description")
            .addReturnField("duration")
            .addReturnField("fileSize")
            .addReturnField("id")
            .sortBy("dateCreated", ResourceListService.SortOrder.ASCENDING)
            .withMax(15);
        ResourceListService service = builder.build();
        return service.list();
    }
}
