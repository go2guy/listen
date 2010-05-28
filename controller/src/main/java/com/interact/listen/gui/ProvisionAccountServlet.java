package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.ListenServletException;
import com.interact.listen.PersistenceService;
import com.interact.listen.resource.*;
import com.interact.listen.resource.Pin.PinType;
import com.interact.listen.security.SecurityUtil;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.classic.Session;

public class ProvisionAccountServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        StatSender statSender = (StatSender)request.getSession().getServletContext().getAttribute("statSender");
        if(statSender == null)
        {
            statSender = new InsaStatSender();
        }
        statSender.send(Stat.GUI_PROVISION_ACCOUNT);

        User currentUser = (User)(request.getSession().getAttribute("user"));
        if(currentUser == null)
        {
            throw new ListenServletException(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "text/plain");
        }

        if(!currentUser.getIsAdministrator())
        {
            throw new ListenServletException(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "text/plain");
        }

        String number = request.getParameter("number");
        if(number == null || number.trim().equals(""))
        {
            throw new ListenServletException(HttpServletResponse.SC_BAD_REQUEST, "Please provide a Number",
                                             "text/plain");
        }

        String username = request.getParameter("username");
        if(username == null || username.trim().equals(""))
        {
            throw new ListenServletException(HttpServletResponse.SC_BAD_REQUEST, "Please provide a Username",
                                             "text/plain");
        }

        String password = request.getParameter("password");
        if(password == null || password.trim().equals(""))
        {
            throw new ListenServletException(HttpServletResponse.SC_BAD_REQUEST, "Please provide a Password",
                                             "text/plain");
        }

        String confirmPassword = request.getParameter("confirmPassword");
        if(confirmPassword == null || confirmPassword.trim().equals(""))
        {
            throw new ListenServletException(HttpServletResponse.SC_BAD_REQUEST, "Please provide a Confirm Password",
                                             "text/plain");
        }

        if(!password.equals(confirmPassword))
        {
            throw new ListenServletException(HttpServletResponse.SC_BAD_REQUEST,
                                             "Password and Confirm Password do not match", "text/plain");
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        PersistenceService persistenceService = new PersistenceService(session);

        Subscriber subscriber = new Subscriber();
        subscriber.setNumber(number);
        persistenceService.save(subscriber);

        Pin activePin = Pin.newRandomInstance(PinType.ACTIVE);
        Pin adminPin = Pin.newRandomInstance(PinType.ADMIN);
        Pin passivePin = Pin.newRandomInstance(PinType.PASSIVE);

        persistenceService.save(activePin);
        persistenceService.save(adminPin);
        persistenceService.save(passivePin);

        Conference conference = new Conference();
        conference.setDescription(subscriber.getNumber());
        conference.setIsStarted(Boolean.FALSE);
        conference.setIsRecording(Boolean.FALSE);
        conference.addToPins(activePin);
        conference.addToPins(adminPin);
        conference.addToPins(passivePin);
        persistenceService.save(conference);

        User user = new User();
        user.setPassword(SecurityUtil.hashPassword(password));
        user.setSubscriber(subscriber);
        user.setUsername(username);
        user.addToConferences(conference);
        persistenceService.save(user);
    }
}
