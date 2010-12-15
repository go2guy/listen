package com.interact.listen.gui;

import com.interact.listen.*;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.history.Channel;
import com.interact.listen.resource.History;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.stats.Stat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

public class DeleteSubscriberServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        ServletUtil.sendStat(request, Stat.GUI_DELETE_SUBSCRIBER);

        Subscriber currentSubscriber = ServletUtil.currentSubscriber(request);
        if(currentSubscriber == null)
        {
            throw new UnauthorizedServletException("Not logged in");
        }

        Long id = ServletUtil.getNotNullLong("id", request, "Id");
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Subscriber subscriberToDelete = Subscriber.queryById(session, id);

        if(subscriberToDelete == null)
        {
            throw new BadRequestServletException("Subscriber not found");
        }

        if(!currentSubscriber.getIsAdministrator())
        {
            throw new UnauthorizedServletException("Insufficient privileges");
        }

        if(subscriberToDelete.equals(currentSubscriber))
        {
            throw new BadRequestServletException("Cannot delete yourself");
        }

        PersistenceService persistenceService = new DefaultPersistenceService(session, currentSubscriber, Channel.GUI);
        History.deleteAllBySubscriber(session, subscriberToDelete);
        persistenceService.delete(subscriberToDelete);
    }
}
