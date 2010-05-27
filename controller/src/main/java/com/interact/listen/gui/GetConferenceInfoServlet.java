package com.interact.listen.gui;

import com.interact.listen.*;
import com.interact.listen.license.License;
import com.interact.listen.license.ListenFeature;
import com.interact.listen.license.NotLicensedException;
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

        User user = (User)(request.getSession().getAttribute("user"));
        if(user == null)
        {
            throw new ListenServletException(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized - not logged in",
                                             "text/plain");
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        PersistenceService persistenceService = new PersistenceService(session);

        String id = request.getParameter("id");

        Marshaller marshaller = new JsonMarshaller();
        marshaller.registerConverterClass(Date.class, FriendlyIso8601DateConverter.class);

        Conference conference = GuiServletUtil.getConferenceFromIdOrUser(id, user, persistenceService);

        if(conference == null)
        {
            throw new ListenServletException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Conference not found",
                                             "text/plain");
        }

        if(!(user.equals(conference.getUser()) || user.getIsAdministrator()))
        {
            throw new ListenServletException(HttpServletResponse.SC_UNAUTHORIZED,
                                             "Unauthorized - conference does not belong to user", "text/plain");
        }

        StringBuilder content = new StringBuilder();
        try
        {
            content.append("{");
            content.append("\"info\":").append(getInfo(conference, marshaller)).append(",");
            content.append("\"participants\":").append(getParticipants(conference, marshaller)).append(",");
            content.append("\"pins\":").append(getPins(conference, marshaller, session)).append(",");
            content.append("\"history\":").append(getHistory(conference, marshaller, session));
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
    
    private ResourceList getResourceList(ArrayList<Resource> content)
    {
        ResourceList list = new ResourceList();
        list.setFirst(0);
        list.setMax(content.size());
        list.setTotal(Long.valueOf(content.size()));
        list.setList(content);
        return list;
    }
    
    private String getParticipants(Conference conference, Marshaller marshaller) throws CriteriaCreationException
    {
        ResourceList list = getResourceList(new ArrayList<Resource>(conference.getParticipants()));

        Set<String> fields = new HashSet<String>();
        fields.add("id");
        fields.add("isAdmin");
        fields.add("isAdminMuted");
        fields.add("isMuted");
        fields.add("isPassive");
        fields.add("number");
        list.setFields(fields);

        return marshaller.marshal(list, Participant.class);
    }

    private String getPins(Conference conference, Marshaller marshaller, Session session) throws CriteriaCreationException
    {
        ResourceList list = getResourceList(new ArrayList<Resource>(conference.getPins()));
        
        Set<String> fields = new HashSet<String>();
        fields.add("id");
        fields.add("number");
        fields.add("type");
        list.setFields(fields);
        
        return marshaller.marshal(list, Pin.class);
    }

    private String getHistory(Conference conference, Marshaller marshaller, Session session) throws CriteriaCreationException
    {
        ResourceList list = getResourceList(new ArrayList<Resource>(conference.getConferenceHistorys()));
        
        Set<String> fields = new HashSet<String>();
        fields.add("dateCreated");
        fields.add("description");
        fields.add("id");
        list.setFields(fields);

        // FIXME make ConferenceHistorys a TreeSet sorted by dateCreated DESCENDING (on Conference)
        // FIXME enforce max of 15
        return marshaller.marshal(list, ConferenceHistory.class);
    }
}
