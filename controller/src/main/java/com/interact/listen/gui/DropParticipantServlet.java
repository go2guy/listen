package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.ServletUtil;
import com.interact.listen.resource.Participant;
import com.interact.listen.resource.User;

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
            session.delete(participant);
            transaction.commit();
        }
        finally
        {
            System.out.println("TIMER: DropParticipantServlet.doPost() took " + (System.currentTimeMillis() - start) +
                               "ms");
        }
    }

    private boolean isUserAllowedToDrop(User user, Participant participant)
    {
        // TODO i'm not sure this is "secure" enough - however, we don't have anything directly linking "user" to
        // "conference" right now
        return user.getSubscriber().getNumber().equals(participant.getConference().getNumber());
    }
}
