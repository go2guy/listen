package com.interact.listen.gui;

import com.interact.listen.*;
import com.interact.listen.history.Channel;
import com.interact.listen.history.DefaultHistoryService;
import com.interact.listen.history.HistoryService;
import com.interact.listen.license.License;
import com.interact.listen.license.ListenFeature;
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

        Subscriber subscriber = null;
        if(errors.size() == 0)
        {
            AuthenticationService service = new AuthenticationService();
            AuthenticationService.Result result = service.authenticate(hibernateSession, username, password);
            
            if(!result.wasSuccessful())
            {
                errors.put("username", result.getCode().getMessage());
            }

            subscriber = result.getSubscriber();
            if(subscriber != null && result.wasSuccessful())
            {
                updateLastLogin(hibernateSession, subscriber);

                boolean adLogin = result.getRealm() == AuthenticationService.Realm.ACTIVE_DIRECTORY;
                writeLoginHistory(hibernateSession, subscriber, adLogin);

                httpSession.setAttribute("subscriber", subscriber.getId());
            }
        }

        if(errors.size() > 0)
        {
            httpSession.setAttribute("errors", errors);
            ServletUtil.redirect("/login", request, response);
        }
        else
        {
            if(subscriber != null && subscriber.getIsAdministrator())
            {
                ServletUtil.redirect("/configuration", request, response);
            }
            else
            {
                if(License.isLicensed(ListenFeature.VOICEMAIL))
                {
                    ServletUtil.redirect("/voicemail", request, response);
                }
                else if(License.isLicensed(ListenFeature.CONFERENCING))
                {
                    ServletUtil.redirect("/conferencing", request, response);
                }
                else if(License.isLicensed(ListenFeature.FINDME))
                {
                    ServletUtil.redirect("/findme", request, response);
                }
            }

            // FIXME need an 'else' to display some page saying that nothing is licensed
            // FIXME also conditionalize this based on user privileges - admins should go to configuration?
        }
    }

    private void updateLastLogin(Session session, Subscriber subscriber)
    {
        PersistenceService persistenceService = new DefaultPersistenceService(session, subscriber, Channel.GUI);
        Subscriber original = subscriber.copy(true);
        subscriber.setLastLogin(new Date());
        persistenceService.update(subscriber, original);
    }

    private void writeLoginHistory(Session session, Subscriber subscriber, boolean isActiveDirectory)
    {
        PersistenceService persistenceService = new DefaultPersistenceService(session, subscriber, Channel.GUI);
        HistoryService historyService = new DefaultHistoryService(persistenceService);
        historyService.writeLoggedIn(subscriber, isActiveDirectory);
    }
}
