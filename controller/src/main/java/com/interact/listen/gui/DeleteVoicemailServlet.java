package com.interact.listen.gui;

import com.interact.listen.*;
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

        Long id = ServletUtil.getNotNullLong("id", request, "Id");

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Voicemail voicemail = Voicemail.queryById(session, id);

        if(voicemail == null)
        {
            throw new BadRequestServletException("Voicemail not found");
        }

        if(!(subscriber.getIsAdministrator() || voicemail.getSubscriber().equals(subscriber)))
        {
            throw new UnauthorizedServletException("Subscriber does not own Voicemail");
        }

        PersistenceService persistenceService = new DefaultPersistenceService(session, subscriber, Channel.GUI);
        persistenceService.delete(voicemail);
    }
}
