package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
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

import org.hibernate.Session;
import org.hibernate.Transaction;

public class DropParticipantServlet extends HttpServlet
{
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
        statSender.send(Stat.GUI_DROP_PARTICIPANT);

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

        try
        {
            Participant participant = (Participant)session.get(Participant.class, Long.valueOf(id));
            if(!isUserAllowedToDrop(user, participant))
            {
                ServletUtil.writeResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                                          "Not allowed to drop participant", "text/plain");
                transaction.rollback();
                return;
            }

            // FIXME what happens when the first one succeeds and the second one fails? do we "rollback" the first one?
            // there's no way we can do it with 100% reliability (because the "rollback" might fail, too)
            // - in all likelihood there will only be one Spot subscriber, but we should accommodate many
            List<ListenSpotSubscriber> spotSubscribers = ListenSpotSubscriber.list(session);
            for(ListenSpotSubscriber spotSubscriber : spotSubscribers)
            {
                SpotSystem spotSystem = new SpotSystem(spotSubscriber.getHttpApi());
                spotSystem.dropParticipant(participant);
            }

            transaction.commit();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            transaction.rollback();
            ServletUtil.writeResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                      "Error dropping participant", "text/plain");
            return;
        }
        finally
        {
            System.out.println("TIMER: DropParticipantServlet.doPost() took " + (System.currentTimeMillis() - start) +
                               "ms");
        }
    }

    private boolean isUserAllowedToDrop(User user, Participant participant)
    {
        // does the current user own the conference?
        if(!user.getConferences().contains(participant.getConference()))
        {
            return false;
        }

        // admin users cannot be dropped
        if(participant.getIsAdmin())
        {
            return false;
        }

        return true;
    }
}
