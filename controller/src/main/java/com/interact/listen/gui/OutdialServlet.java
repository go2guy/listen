package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.PersistenceService;
import com.interact.listen.ServletUtil;
import com.interact.listen.resource.*;
import com.interact.listen.spot.SpotCommunicationException;
import com.interact.listen.spot.SpotSystem;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class OutdialServlet extends HttpServlet
{
    private static final Logger LOG = Logger.getLogger(OutdialServlet.class);
    private static final long serialVersionUID = 1L;

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        StatSender statSender = (StatSender)request.getSession().getServletContext().getAttribute("statSender");
        if(statSender == null)
        {
            statSender = new InsaStatSender();
        }
        statSender.send(Stat.GUI_OUTDIAL);

        User user = (User)(request.getSession().getAttribute("user"));
        if(user == null)
        {
            ServletUtil.writeResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "text/plain");
            return;
        }

        String conferenceId = request.getParameter("conferenceId");
        if(conferenceId == null || conferenceId.trim().equals(""))
        {
            ServletUtil.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Please provide a conferenceId",
                                      "text/plain");
            return;
        }

        String number = request.getParameter("number");
        if(number == null || number.trim().equals(""))
        {
            ServletUtil.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Please provide a number",
                                      "text/plain");
            return;
        }

        LOG.debug("Outdialing to [" + number + "] for conference id [" + conferenceId + "]");

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        PersistenceService persistenceService = new PersistenceService(session);

        Conference conference = (Conference)session.get(Conference.class, Long.valueOf(conferenceId));

        if(conference == null)
        {
            ServletUtil.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Conference not found",
                                      "text/plain");
            return;
        }

        if(!isUserAllowedToOutdial(user, conference))
        {
            ServletUtil.writeResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Not allowed to outdial",
                                      "text/plain");
            return;
        }

        String adminSessionId = getConferenceAdminSessionId(session, conference);

        // send request to all SPOT subscribers
        List<ListenSpotSubscriber> spotSubscribers = ListenSpotSubscriber.list(session);
        for(ListenSpotSubscriber spotSubscriber : spotSubscribers)
        {
            SpotSystem spotSystem = new SpotSystem(spotSubscriber.getHttpApi());
            try
            {
                spotSystem.outdial(number, adminSessionId);
            }
            catch(SpotCommunicationException e)
            {
                throw new ServletException(e);
            }
        }

        ConferenceHistory history = new ConferenceHistory();
        history.setConference(conference);
        history.setDescription("Outdialed " + number);
        history.setUser("Current user");
        persistenceService.save(history);
    }

    private String getConferenceAdminSessionId(Session session, Conference conference)
    {
        Criteria criteria = session.createCriteria(Participant.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.createAlias("conference", "conference_alias");
        criteria.add(Restrictions.eq("conference_alias.id", conference.getId()));
        List<Participant> participants = (List<Participant>)criteria.list();
        for(Participant participant : participants)
        {
            if(participant.getIsAdmin())
            {
                return participant.getSessionID();
            }
        }
        throw new IllegalStateException("Could not find Admin participant for Conference"); // FIXME maybe use a checked
        // exception here
    }

    private boolean isUserAllowedToOutdial(User user, Conference conference)
    {
        if(user.getIsAdministrator())
        {
            return true;
        }

        // does the current user own the conference?
        if(user.getConferences().contains(conference))
        {
            return true;
        }

        return false;
    }
}
