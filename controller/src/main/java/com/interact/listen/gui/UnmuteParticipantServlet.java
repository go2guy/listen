package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.PersistenceService;
import com.interact.listen.ServletUtil;
import com.interact.listen.resource.ListenSpotSubscriber;
import com.interact.listen.resource.Participant;
import com.interact.listen.resource.User;
import com.interact.listen.spot.SpotSystem;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class UnmuteParticipantServlet extends HttpServlet
{
    private static final Logger LOG = Logger.getLogger(UnmuteParticipantServlet.class);
    private static final long serialVersionUID = 1L;

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
    {
        long start = System.currentTimeMillis();

        StatSender statSender = (StatSender)request.getSession().getServletContext().getAttribute("statSender");
        if(statSender == null)
        {
            statSender = new InsaStatSender();
        }
        statSender.send(Stat.GUI_UNMUTE_PARTICIPANT);

        User user = (User)(request.getSession().getAttribute("user"));
        if(user == null)
        {
            ServletUtil.writeResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "text/plain");
            return;
        }

        String id = request.getParameter("id");
        if(id == null || id.trim().equals(""))
        {
            ServletUtil.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Please provide an id",
                                      "text/plain");
            return;
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction transaction = session.beginTransaction();
        PersistenceService persistenceService = new PersistenceService(session);

        try
        {
            Participant participant = (Participant)persistenceService.get(Participant.class, Long.valueOf(id));
            if(!isUserAllowedToUnmute(user, participant))
            {
                ServletUtil.writeResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                                          "Not allowed to unmute participant", "text/plain");
                transaction.rollback();
                return;
            }

            // send request to all SPOT subscribers
            List<ListenSpotSubscriber> spotSubscribers = ListenSpotSubscriber.list(session);
            for(ListenSpotSubscriber spotSubscriber : spotSubscribers)
            {
                SpotSystem spotSystem = new SpotSystem(spotSubscriber.getHttpApi());
                spotSystem.unmuteParticipant(participant);
            }

            transaction.commit();
        }
        catch(Exception e)
        {
            LOG.error("Error unmuting participant", e);
            transaction.rollback();
            ServletUtil.writeResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                      "Error unmuting participant", "text/plain");
            return;
        }
        finally
        {
            LOG.debug("UnmuteParticipantServlet.doPost() took " + (System.currentTimeMillis() - start) + "ms");
        }
    }

    private boolean isUserAllowedToUnmute(User user, Participant participant)
    {
        // admins cannot be admin muted
        if(participant.getIsAdmin())
        {
            return false;
        }

        if(user.getIsAdministrator())
        {
            return true;
        }

        // does the current user own the conference?
        if(user.getConferences().contains(participant.getConference()))
        {
            return true;
        }

        return false;
    }
}
