package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.PersistenceService;
import com.interact.listen.ServletUtil;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.history.Channel;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.Voicemail;
import com.interact.listen.stats.Stat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

public class DeleteVoicemailServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        ServletUtil.sendStat(request, Stat.GUI_DELETE_VOICEMAIL);

        Subscriber subscriber = ServletUtil.currentSubscriber(request);
        if(subscriber == null)
        {
            throw new UnauthorizedServletException("Not logged in");
        }

        String id = request.getParameter("id");
        if(id == null || id.trim().equals(""))
        {
            throw new BadRequestServletException("Please provide an id");
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Voicemail voicemail = Voicemail.queryById(session, Long.parseLong(id));

        if(voicemail == null)
        {
            throw new BadRequestServletException("Voicemail not found");
        }

        if(!(subscriber.getIsAdministrator() || voicemail.getSubscriber().equals(subscriber)))
        {
            throw new UnauthorizedServletException("Subscriber does not own Voicemail");
        }

        PersistenceService persistenceService = new PersistenceService(session, subscriber, Channel.GUI);
        persistenceService.delete(voicemail);
    }
}
