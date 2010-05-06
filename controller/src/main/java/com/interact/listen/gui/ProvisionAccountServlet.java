package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.PersistenceService;
import com.interact.listen.ServletUtil;
import com.interact.listen.resource.*;
import com.interact.listen.resource.Pin.PinType;
import com.interact.listen.security.SecurityUtil;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Transaction;
import org.hibernate.classic.Session;

public class ProvisionAccountServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
    {
        long start = System.currentTimeMillis();

        StatSender statSender = (StatSender)request.getSession().getServletContext().getAttribute("statSender");
        if(statSender == null)
        {
            statSender = new InsaStatSender();
        }
        statSender.send(Stat.GUI_PROVISION_ACCOUNT);

        User currentUser = (User)(request.getSession().getAttribute("user"));
        if(currentUser == null)
        {
            ServletUtil.writeResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "text/plain");
            return;
        }

        if(!currentUser.getIsAdministrator())
        {
            ServletUtil.writeResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "text/plain");
            return;
        }

        String number = request.getParameter("number");
        if(number == null || number.trim().equals(""))
        {
            ServletUtil.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Please provide a Number",
                                      "text/plain");
            return;
        }

        String username = request.getParameter("username");
        if(username == null || username.trim().equals(""))
        {
            ServletUtil.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Please provide a Username",
                                      "text/plain");
            return;
        }

        String password = request.getParameter("password");
        if(password == null || password.trim().equals(""))
        {
            ServletUtil.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Please provide a Password",
                                      "text/plain");
            return;
        }

        String confirmPassword = request.getParameter("confirmPassword");
        if(confirmPassword == null || confirmPassword.trim().equals(""))
        {
            ServletUtil.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                                      "Please provide a Confirm Password", "text/plain");
            return;
        }

        if(!password.equals(confirmPassword))
        {
            ServletUtil.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                                      "Password and Confirm Password do not match", "text/plain");
            return;
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction transaction = session.beginTransaction();
        PersistenceService persistenceService = new PersistenceService(session);

        try
        {
            Subscriber subscriber = new Subscriber();
            subscriber.setNumber(number);
            persistenceService.save(subscriber);

            Pin activePin = Pin.newInstance("111" + subscriber.getNumber(), PinType.ACTIVE);
            Pin adminPin = Pin.newInstance("999" + subscriber.getNumber(), PinType.ADMIN);
            Pin passivePin = Pin.newInstance("000" + subscriber.getNumber(), PinType.PASSIVE);

            persistenceService.save(activePin);
            persistenceService.save(adminPin);
            persistenceService.save(passivePin);

            Conference conference = new Conference();
            conference.setDescription(subscriber.getNumber());
            conference.setIsStarted(Boolean.FALSE);
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

            transaction.commit();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            transaction.rollback();
            ServletUtil.writeResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                      "Error provisioning account", "text/plain");
            return;
        }
        finally
        {
            System.out.println("TIMER: ProvisionAccountServlet.doPost() took " + (System.currentTimeMillis() - start) +
                               "ms");
        }
    }
}
