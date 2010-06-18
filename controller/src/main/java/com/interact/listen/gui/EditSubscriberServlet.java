package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.PersistenceService;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.resource.AccessNumber;
import com.interact.listen.resource.Conference;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.security.SecurityUtil;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Criteria;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Restrictions;

public class EditSubscriberServlet extends HttpServlet
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
        statSender.send(Stat.GUI_EDIT_SUBSCRIBER);

        Subscriber currentSubscriber = (Subscriber)(request.getSession().getAttribute("subscriber"));
        if(currentSubscriber == null)
        {
            throw new UnauthorizedServletException();
        }
        
        String id = request.getParameter("id");
        if(id == null || id.trim().equals(""))
        {
            throw new BadRequestServletException("Please provide an id");
        }
        
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Subscriber subscriberToEdit = findSubscriberById(id, session);
        
        if(subscriberToEdit == null)
        {
            throw new BadRequestServletException("Subscriber not found");
        }

        if(!currentSubscriber.getIsAdministrator() && !currentSubscriber.getId().equals(subscriberToEdit.getId()))
        {
            throw new UnauthorizedServletException();
        }

        String username = request.getParameter("username");
        if(username == null || username.trim().equals(""))
        {
            throw new BadRequestServletException("Please provide a Username");
        }

        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        if((password != null && !password.trim().equals("")) ||
           (confirmPassword != null && !confirmPassword.trim().equals("")))
        {
            if(password == null || password.trim().equals(""))
            {
                throw new BadRequestServletException("Please provide a Password");
            }

            if(confirmPassword == null || confirmPassword.trim().equals(""))
            {
                throw new BadRequestServletException("Please provide a Confirm Password");
            }

            if(!password.equals(confirmPassword))
            {
                throw new BadRequestServletException("Password and Confirm Password do not match");
            }

            subscriberToEdit.setPassword(SecurityUtil.hashPassword(password));
        }

        PersistenceService persistenceService = new PersistenceService(session);

        String accessNumbers = request.getParameter("accessNumbers");
        if(currentSubscriber.getIsAdministrator() && accessNumbers != null && accessNumbers.trim().length() > 0)
        {
            updateSubscriberAccessNumbers(subscriberToEdit, accessNumbers, session, persistenceService);
        }

        subscriberToEdit.setUsername(username);

        ArrayList<Conference> conferenceList = new ArrayList<Conference>(subscriberToEdit.getConferences());

        if(conferenceList.size() > 0)
        {
            // subscribers only have one conference at this time, so just get the first entry for update
            Conference conferenceToEdit = conferenceList.get(0);
            Conference originalConference = conferenceToEdit.copy(true);

            conferenceToEdit.setDescription(subscriberToEdit.getUsername() + "'s Conference");
            persistenceService.update(conferenceToEdit, originalConference);
        }
        
        persistenceService.save(subscriberToEdit);
    }
    
    private Subscriber findSubscriberById(String id, Session session)
    {
        Criteria criteria = session.createCriteria(Subscriber.class);
        criteria.add(Restrictions.eq("id", Long.valueOf(id)));
        criteria.setMaxResults(1);
        return (Subscriber)criteria.uniqueResult();
    }

    public static void updateSubscriberAccessNumbers(Subscriber subscriber, String accessNumberString, Session session,
                                                     PersistenceService persistenceService)
        throws BadRequestServletException
    {
        String[] split = accessNumberString.split(",");
        for(String an : split)
        {
            Criteria criteria = session.createCriteria(AccessNumber.class);
            criteria.add(Restrictions.eq("number", an));
            criteria.setMaxResults(1);
            AccessNumber result = (AccessNumber)criteria.uniqueResult();

            if(result != null && !result.getSubscriber().equals(subscriber))
            {
                throw new BadRequestServletException("Access number [" + an + "] is already in use by another account");
            }
            else if(result == null)
            {
                AccessNumber newNumber = new AccessNumber();
                newNumber.setNumber(an);
                newNumber.setSubscriber(subscriber);

                persistenceService.save(newNumber);
                subscriber.addToAccessNumbers(newNumber);
            }
        }
    }
}
