package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.PersistenceService;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.resource.*;
import com.interact.listen.security.SecurityUtil;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.hibernate.Criteria;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Restrictions;

public class EditUserServlet extends HttpServlet
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
        statSender.send(Stat.GUI_EDIT_USER);

        User currentUser = (User)(request.getSession().getAttribute("user"));
        if(currentUser == null)
        {
            throw new UnauthorizedServletException();
        }
        
        String id = request.getParameter("id");
        if(id == null || id.trim().equals(""))
        {
            throw new BadRequestServletException("Please provide a user id");
        }
        
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        User userToEdit = findUserById(id, session);

        if(!currentUser.getIsAdministrator() && !currentUser.getId().equals(userToEdit.getId()))
        {
            throw new UnauthorizedServletException();
        }

        // require number if they're not an admin
        String number = request.getParameter("number");
        if(!userToEdit.getIsAdministrator()) {
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

            userToEdit.setPassword(SecurityUtil.hashPassword(password));
        }

        PersistenceService persistenceService = new PersistenceService(session);

        //Only admin can change the subscriber associated with a user
        if(currentUser.getIsAdministrator() && number != null)
        {
            Subscriber currentSubscriber = findSubscriberByNumber(number, session);

            if(currentSubscriber == null)
            {
                currentSubscriber = new Subscriber();
                currentSubscriber.setNumber(number);
                persistenceService.save(currentSubscriber);
            }

            userToEdit.setSubscriber(currentSubscriber);
        }

        userToEdit.setUsername(username);
        
        ArrayList<Conference> conferenceList = new ArrayList<Conference>(userToEdit.getConferences());
        
        if(conferenceList.size() > 0)
        {
            //Users only have one conference at this time, so just get the first entry for update
            Conference conferenceToEdit = conferenceList.get(0);
            Conference originalConference = conferenceToEdit.copy(true);
            
            conferenceToEdit.setDescription(userToEdit.getSubscriber().getNumber());
            persistenceService.update(conferenceToEdit, originalConference);
        }
        
        persistenceService.save(userToEdit);
    }
    
    private User findUserById(String id, Session session)
    {
        Criteria criteria = session.createCriteria(User.class);
        criteria.add(Restrictions.eq("id", Long.valueOf(id)));
        criteria.setMaxResults(1);
        return (User)criteria.uniqueResult();
    }
    
    private Subscriber findSubscriberByNumber(String number, Session session)
    {
        Criteria criteria = session.createCriteria(Subscriber.class);
        criteria.add(Restrictions.eq("number", number));
        criteria.setMaxResults(1);
        return (Subscriber)criteria.uniqueResult();
    }
}
