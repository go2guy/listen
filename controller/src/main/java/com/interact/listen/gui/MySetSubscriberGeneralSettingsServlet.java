package com.interact.listen.gui;

import com.interact.listen.*;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.history.Channel;
import com.interact.listen.resource.Conference;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.security.SecurityUtil;
import com.interact.listen.stats.Stat;

import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

public class MySetSubscriberGeneralSettingsServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        ServletUtil.sendStat(request, Stat.GUI_EDIT_SUBSCRIBER);
        Subscriber subscriber = ServletUtil.requireCurrentSubscriber(request, false);
        
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        
        Subscriber original = subscriber.copy(true);
        
        // active directory users cannot change their password
        if(!subscriber.getIsActiveDirectory())
        {
            String password = request.getParameter("password");
            String confirm = request.getParameter("passwordConfirm");
            if((password != null && !password.trim().equals("")) || (confirm != null && !confirm.trim().equals("")))
            {
                if(password == null || password.trim().equals(""))
                {
                    throw new BadRequestServletException("Please provide a Password");
                }
                
                if(confirm == null || confirm.trim().equals(""))
                {
                    throw new BadRequestServletException("Please confirm your Password");
                }
                
                if(!password.equals(confirm))
                {
                    throw new BadRequestServletException("Password and Confirm do not match");
                }
                
                subscriber.setPassword(SecurityUtil.hashPassword(password));
            }
        }
        
        subscriber.setWorkEmailAddress(request.getParameter("emailAddress"));
        subscriber.setRealName(request.getParameter("realName"));
        
        PersistenceService ps = new DefaultPersistenceService(session, subscriber, Channel.GUI);
        
        if(!subscriber.validate())
        {
            String message = subscriber.errors().get(0);
            subscriber.clearErrors();
            throw new BadRequestServletException(message);
        }
        ps.update(subscriber, original);

        ArrayList<Conference> conferences = new ArrayList<Conference>(subscriber.getConferences());
        if(conferences.size() > 0)
        {
            Conference conference = conferences.get(0);
            Conference orig = conference.copy(true);
            conference.setDescription(subscriber.conferenceDescription());
            ps.update(conference, orig);
        }
    }
}
