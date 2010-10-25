package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.PersistenceService;
import com.interact.listen.ServletUtil;
import com.interact.listen.history.Channel;
import com.interact.listen.history.HistoryService;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.security.AuthenticationService;
import com.interact.listen.stats.Stat;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import org.hibernate.Session;

public class LoginServlet extends HttpServlet
{
    private static final Logger LOG = Logger.getLogger(LoginServlet.class);
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        HttpSession session = request.getSession();
        request.setAttribute("errors", session.getAttribute("errors"));
        session.removeAttribute("errors");

        ServletUtil.forward("/WEB-INF/jsp/login.jsp", request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        ServletUtil.sendStat(request, Stat.GUI_LOGIN);

        HttpSession httpSession = request.getSession(true);
        Session hibernateSession = HibernateUtil.getSessionFactory().getCurrentSession();

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        LOG.debug("POST LoginServlet username=" + username + "&password=*");

        Map<String, String> errors = new HashMap<String, String>();

        if(username == null || username.trim().equals(""))
        {
            errors.put("username", "Please provide a username");
        }

        if(password == null || password.trim().equals(""))
        {
            errors.put("password", "Please provide a password");
        }

        if(errors.size() == 0)
        {
            AuthenticationService service = new AuthenticationService();
            AuthenticationService.Result result = service.authenticate(hibernateSession, username, password);
            
            if(!result.wasSuccessful())
            {
                errors.put("username", result.getCode().getMessage());
            }

            Subscriber subscriber = result.getSubscriber();
            if(subscriber != null && result.wasSuccessful())
            {
                updateLastLogin(hibernateSession, subscriber);

                boolean adLogin = result.getRealm() == AuthenticationService.Realm.ACTIVE_DIRECTORY;
                writeLoginHistory(hibernateSession, subscriber, adLogin);

                httpSession.setAttribute("subscriber", subscriber);
            }
        }

        if(errors.size() > 0)
        {
            httpSession.setAttribute("errors", errors);
            ServletUtil.redirect("/login", request, response);
        }
        else
        {
            ServletUtil.redirect("/index", request, response);
        }
    }

    private void updateLastLogin(Session session, Subscriber subscriber)
    {
        PersistenceService persistenceService = new PersistenceService(session, subscriber, Channel.GUI);
        Subscriber original = subscriber.copy(true);
        subscriber.setLastLogin(new Date());
        persistenceService.update(subscriber, original);
    }

    private void writeLoginHistory(Session session, Subscriber subscriber, boolean isActiveDirectory)
    {
        PersistenceService persistenceService = new PersistenceService(session, subscriber, Channel.GUI);
        HistoryService historyService = new HistoryService(persistenceService);
        historyService.writeLoggedIn(subscriber, isActiveDirectory);
    }
}
