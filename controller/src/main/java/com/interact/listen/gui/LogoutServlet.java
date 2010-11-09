package com.interact.listen.gui;

import com.interact.listen.*;
import com.interact.listen.history.Channel;
import com.interact.listen.history.DefaultHistoryService;
import com.interact.listen.history.HistoryService;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.stats.Stat;

import java.io.IOException;

import javax.servlet.http.*;

import org.hibernate.Session;

public class LogoutServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        // TODO should this be fixed?
        // we allow GET and POST requests to logout so we can do a window.location in javascript to log them out
        logout(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        logout(request, response);
    }

    private void logout(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        ServletUtil.sendStat(request, Stat.GUI_LOGOUT);

        HttpSession session = request.getSession();
        Subscriber subscriber = ServletUtil.currentSubscriber(request);
        if(subscriber != null)
        {
            writeLogoutHistory(HibernateUtil.getSessionFactory().getCurrentSession(), subscriber);
            session.removeAttribute("subscriber");
        }

        ServletUtil.redirect("/login", request, response);
    }

    private void writeLogoutHistory(Session session, Subscriber subscriber)
    {
        PersistenceService persistenceService = new DefaultPersistenceService(session, subscriber, Channel.GUI);
        HistoryService historyService = new DefaultHistoryService(persistenceService);
        historyService.writeLoggedOut(subscriber);
    }
}
