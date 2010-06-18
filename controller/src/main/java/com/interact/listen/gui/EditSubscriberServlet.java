package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.PersistenceService;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.UnauthorizedServletException;
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

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Restrictions;

public class EditSubscriberServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    /** Class logger */
    private static final Logger LOG = Logger.getLogger(EditSubscriberServlet.class);

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

        // require number if they're not an admin
        String number = request.getParameter("number");
        if(!subscriberToEdit.getIsAdministrator()) {
            if(number == null || number.trim().equals(""))
            {
                throw new BadRequestServletException("Please provide a Number");
            }
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

        if(currentSubscriber.getIsAdministrator())
        {
            Subscriber existingSubscriber = findSubscriberByNumber(number, session);
            if(existingSubscriber != null && (subscriberToEdit == null || !subscriberToEdit.getNumber().equals(number)))
            {
                throw new BadRequestServletException("That number is currently in use");
            }

            subscriberToEdit.setNumber(number);
        }

        subscriberToEdit.setUsername(username);
        
        ArrayList<Conference> conferenceList = new ArrayList<Conference>(subscriberToEdit.getConferences());

        if(conferenceList.size() > 0)
        {
            // subscribers only have one conference at this time, so just get the first entry for update
            Conference conferenceToEdit = conferenceList.get(0);
            Conference originalConference = conferenceToEdit.copy(true);

            conferenceToEdit.setDescription(subscriberToEdit.getNumber());
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
    
    private Subscriber findSubscriberByNumber(String number, Session session)
    {
        Criteria criteria = session.createCriteria(Subscriber.class);
        criteria.add(Restrictions.eq("number", number));
        criteria.setMaxResults(1);
        return (Subscriber)criteria.uniqueResult();
    }
}
