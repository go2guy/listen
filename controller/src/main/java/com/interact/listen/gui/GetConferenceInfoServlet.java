package com.interact.listen.gui;

import com.interact.listen.*;
import com.interact.listen.ResourceListService.Builder;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.converter.FriendlyIso8601DateConverter;
import com.interact.listen.marshal.json.JsonMarshaller;
import com.interact.listen.resource.*;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class GetConferenceInfoServlet extends HttpServlet
{
    private static final Logger LOG = Logger.getLogger(GetConferenceInfoServlet.class);
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        long start = System.currentTimeMillis();

        StatSender statSender = (StatSender)request.getSession().getServletContext().getAttribute("statSender");
        if(statSender == null)
        {
            statSender = new InsaStatSender();
        }
        statSender.send(Stat.GUI_GET_CONFERENCE_INFO);

        User user = (User)(request.getSession().getAttribute("user"));
        if(user == null)
        {
            ServletUtil.writeResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized - not logged in", "text/plain");
            return;
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction transaction = session.beginTransaction();
        PersistenceService persistenceService = new PersistenceService(session);

        try
        {
            String id = request.getParameter("id");

            Marshaller marshaller = new JsonMarshaller();
            marshaller.registerConverterClass(Date.class, FriendlyIso8601DateConverter.class);

            Conference conference = GuiServletUtil.getConferenceFromIdOrUser(id, user, persistenceService);

            if(conference == null)
            {
                transaction.rollback();
                ServletUtil.writeResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                          "Conference not found", "text/plain");
                return;
            }

            if(!user.equals(conference.getUser()) && !user.getIsAdministrator())
            {
                transaction.rollback();
                ServletUtil.writeResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized - conference does not belong to user", "text/plain");
                return;
            }

            StringBuilder content = new StringBuilder();
            content.append("{");
            content.append("\"info\":").append(getInfo(conference, marshaller)).append(",");
            content.append("\"participants\":").append(getParticipants(conference, marshaller, session)).append(",");
            content.append("\"pins\":").append(getPins(conference, marshaller, session)).append(",");
            content.append("\"history\":").append(getHistory(conference, marshaller, session));
            content.append("}");

            transaction.commit();

            ServletUtil.writeResponse(response, HttpServletResponse.SC_OK, content.toString(),
                                      marshaller.getContentType());
        }
        catch(Exception e)
        {
            LOG.error("Error getting conference info", e);
            transaction.rollback();
            ServletUtil.writeResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage(),
                                      "text/plain");
            return;
        }
        finally
        {
            LOG.debug("GetConferenceInfoServlet.doGet() took " + (System.currentTimeMillis() - start) + "ms");
        }
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
            .addReturnField("type");
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
}
