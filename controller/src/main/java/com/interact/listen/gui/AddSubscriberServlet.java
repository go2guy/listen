package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.PersistenceService;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.UnauthorizedServletException;
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

public class AddSubscriberServlet extends HttpServlet
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
        statSender.send(Stat.GUI_ADD_SUBSCRIBER);

        Subscriber currentSubscriber = (Subscriber)(request.getSession().getAttribute("subscriber"));
        if(currentSubscriber == null)
        {
            throw new UnauthorizedServletException();
        }

        if(!currentSubscriber.getIsAdministrator())
        {
            throw new UnauthorizedServletException();
        }

        String number = request.getParameter("number");
        if(number == null || number.trim().equals(""))
        {
            throw new BadRequestServletException("Please provide a Number");
        }

        String username = request.getParameter("username");
        if(username == null || username.trim().equals(""))
        {
            throw new BadRequestServletException("Please provide a Username");
        }

        String password = request.getParameter("password");
        if(password == null || password.trim().equals(""))
        {
            throw new BadRequestServletException("Please provide a Password");
        }

        String confirmPassword = request.getParameter("confirmPassword");
        if(confirmPassword == null || confirmPassword.trim().equals(""))
        {
            throw new BadRequestServletException("Please provide a Confirm Password");
        }

        if(!password.equals(confirmPassword))
        {
            throw new BadRequestServletException("Password and Confirm Password do not match");
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        PersistenceService persistenceService = new PersistenceService(session);

        Subscriber subscriber = new Subscriber();
        subscriber.setNumber(number);
        subscriber.setPassword(SecurityUtil.hashPassword(password));
        subscriber.setUsername(username);
        persistenceService.save(subscriber);

        String accessNumbers = request.getParameter("accessNumbers");
        if(accessNumbers != null && accessNumbers.length() > 0)
        {
            EditSubscriberServlet.updateSubscriberAccessNumbers(subscriber, accessNumbers, session,
                                                                persistenceService);
        }

        Pin activePin = Pin.newRandomInstance(PinType.ACTIVE);
        Pin adminPin = Pin.newRandomInstance(PinType.ADMIN);
        Pin passivePin = Pin.newRandomInstance(PinType.PASSIVE);

        persistenceService.save(activePin);
        persistenceService.save(adminPin);
        persistenceService.save(passivePin);

        Conference conference = new Conference();
        conference.setDescription(subscriber.getUsername() + "'s Conference");
        conference.setIsStarted(Boolean.FALSE);
        conference.setIsRecording(Boolean.FALSE);
        conference.addToPins(activePin);
        conference.addToPins(adminPin);
        conference.addToPins(passivePin);
        persistenceService.save(conference);

        subscriber.addToConferences(conference);
    }
}
