package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.PersistenceService;
import com.interact.listen.ServletUtil;
import com.interact.listen.history.Channel;
import com.interact.listen.history.HistoryService;
import com.interact.listen.resource.Subscriber;
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
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class LoginServlet extends HttpServlet
{
    private static final Logger LOG = Logger.getLogger(LoginServlet.class);
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        if(ServletUtil.currentSubscriber(request) != null)
        {
            ServletUtil.redirect("/index", response);
            return;
        }

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

        Session hibernateSession = HibernateUtil.getSessionFactory().getCurrentSession();

        HttpSession httpSession = request.getSession(true);

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
            Subscriber subscriber = findSubscriberByUsername(username, hibernateSession);
            if(subscriber == null || !isValidPassword(subscriber, password))
            {
                errors.put("username", "Sorry, those aren't valid credentials");
            }

            if(subscriber != null)
            {
                Session session = HibernateUtil.getSessionFactory().getCurrentSession();
                PersistenceService persistenceService = new PersistenceService(session, subscriber, Channel.GUI);
                Subscriber original = subscriber.copy(true);
                subscriber.setLastLogin(new Date());
                persistenceService.update(subscriber, original);
            }

            writeLoginHistory(hibernateSession, subscriber);
            httpSession.setAttribute("subscriber", subscriber);
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

    private Subscriber findSubscriberByUsername(String username, Session session)
    {
        // FIXME this query is not eagerly fetching associations, (e.g. the accessNumbers collection)
        // need to figure out why
        Criteria criteria = session.createCriteria(Subscriber.class);
        criteria.add(Restrictions.eq("username", username));
        criteria.setMaxResults(1);
        return (Subscriber)criteria.uniqueResult();
    }

    private boolean isValidPassword(Subscriber subscriber, String password)
    {
        return subscriber.getPassword().equals(SecurityUtil.hashPassword(password));
    }

    private void writeLoginHistory(Session session, Subscriber subscriber)
    {
        PersistenceService persistenceService = new PersistenceService(session, subscriber, Channel.GUI);
        HistoryService historyService = new HistoryService(persistenceService);
        historyService.writeLoggedIn(subscriber);
    }
}
