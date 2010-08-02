package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.PersistenceService;
import com.interact.listen.ServletUtil;
import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;
import com.interact.listen.history.Channel;
import com.interact.listen.history.HistoryService;
import com.interact.listen.resource.Conference;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.security.ActiveDirectoryAuthenticator;
import com.interact.listen.security.AuthenticationException;
import com.interact.listen.security.SecurityUtil;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

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
        StatSender statSender = (StatSender)request.getSession().getServletContext().getAttribute("statSender");
        if(statSender == null)
        {
            statSender = new InsaStatSender();
        }
        statSender.send(Stat.GUI_LOGIN);

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

        boolean adLogin = false;
        if(errors.size() == 0)
        {
            Subscriber subscriber = Subscriber.queryByUsername(hibernateSession, username);
            if(subscriber != null && !subscriber.getIsActiveDirectory())
            {
                if(!isValidPassword(subscriber, password))
                {
                    errors.put("username", "Sorry, those aren't valid credentials");
                    LOG.warn("Local Auth: Invalid credentials for [" + username + "] (invalid local password)");
                }
                else
                {
                    LOG.debug("Login successful for local account [" + username + "]");
                }
            }
            else if(!Boolean.valueOf(Configuration.get(Property.Key.ACTIVE_DIRECTORY_ENABLED)))
            {
                errors.put("username", "Sorry, those aren't valid credentials");
                LOG.warn("Local Auth: Invalid credentials for [" + username + "] (subscriber = null, AD = disabled)");
            }
            else
            {
                adLogin = true;
                String server = Configuration.get(Property.Key.ACTIVE_DIRECTORY_SERVER);
                String domain = Configuration.get(Property.Key.ACTIVE_DIRECTORY_DOMAIN);
                ActiveDirectoryAuthenticator auth = new ActiveDirectoryAuthenticator(server, domain);
                try
                {
                    if(!auth.authenticate(username, password))
                    {
                        errors.put("username", "Sorry, those aren't valid credentials");
                        LOG.warn("AD Auth: Invalid credentials for [" + username + "], (invalid AD password)");
                    }
                    else
                    {
                        LOG.debug("Login successful for Active Directory account [" + username + "]");
                        if(subscriber == null)
                        {
                            LOG.debug("Local Subscriber for AD user does not exist, creating");
                            subscriber = new Subscriber();
                            subscriber.setUsername(username);
                            subscriber.setIsActiveDirectory(true);
                            subscriber.setLastLogin(new Date());
                            // TODO can we get their real name, etc. from AD?

                            PersistenceService ps = new PersistenceService(hibernateSession, subscriber, Channel.GUI);
                            ps.save(subscriber);

                            Conference.createNew(ps, subscriber);
                        }
                    }
                }
                catch(AuthenticationException e)
                {
                    errors.put("username", "An error occurred logging in, please contact an Administrator");
                    LOG.error(e);
                }
            }

            if(subscriber != null && errors.size() == 0)
            {
                updateLastLogin(hibernateSession, subscriber);
                writeLoginHistory(hibernateSession, subscriber, adLogin);
                httpSession.setAttribute("subscriber", subscriber);
            }
        }

        if(errors.size() > 0)
        {
            httpSession.setAttribute("errors", errors);
            ServletUtil.redirect("/login", response);
        }
        else
        {
            ServletUtil.redirect("/index", response);
        }
    }

    private void updateLastLogin(Session session, Subscriber subscriber)
    {
        PersistenceService persistenceService = new PersistenceService(session, subscriber, Channel.GUI);
        Subscriber original = subscriber.copy(true);
        subscriber.setLastLogin(new Date());
        persistenceService.update(subscriber, original);
    }

    private boolean isValidPassword(Subscriber subscriber, String password)
    {
        return subscriber.getPassword().equals(SecurityUtil.hashPassword(password));
    }

    private void writeLoginHistory(Session session, Subscriber subscriber, boolean isActiveDirectory)
    {
        PersistenceService persistenceService = new PersistenceService(session, subscriber, Channel.GUI);
        HistoryService historyService = new HistoryService(persistenceService);
        historyService.writeLoggedIn(subscriber, isActiveDirectory);
    }
}
