package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.PersistenceService;
import com.interact.listen.ServletUtil;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.history.Channel;
import com.interact.listen.resource.AccessNumber;
import com.interact.listen.resource.Conference;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.security.SecurityUtil;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.util.*;

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

        Subscriber currentSubscriber = ServletUtil.currentSubscriber(request);
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
        
        Subscriber originalSubscriber = subscriberToEdit.copy(true);

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

        PersistenceService persistenceService = new PersistenceService(session, currentSubscriber, Channel.GUI);

        String accessNumbers = request.getParameter("accessNumbers");
        if(currentSubscriber.getIsAdministrator() && accessNumbers != null)
        {
            updateSubscriberAccessNumbers(subscriberToEdit, accessNumbers, session, persistenceService);
        }
        
        String voicemailPinString = request.getParameter("voicemailPin");
        if(voicemailPinString == null || voicemailPinString.trim().equals(""))
        {
            throw new BadRequestServletException("Please provide a Voicemail Pin Number");
        }
        
        if(voicemailPinString.length() > 10)
        {
            throw new BadRequestServletException("Please provide a Voicemail Pin Number with ten digits or less");
        }
        
        Long voicemailPin = null;
        
        try
        {
            voicemailPin = Long.valueOf(voicemailPinString);
        }
        catch(NumberFormatException e)
        {
            throw new BadRequestServletException("Voicemail Pin Number can only be digits 0-9");
        }
        
        Boolean enableEmail = Boolean.valueOf(request.getParameter("enableEmail"));
        Boolean enableSms = Boolean.valueOf(request.getParameter("enableSms"));
        String emailAddress = request.getParameter("emailAddress");
        String smsAddress = request.getParameter("smsAddress");
        
        if(enableEmail && (emailAddress == null || emailAddress.equals("")))
        {
            throw new BadRequestServletException("Please provide an E-mail address");
        }
        
        if(enableSms && (smsAddress == null || smsAddress.equals("")))
        {
            throw new BadRequestServletException("Please provide an SMS address");
        }

        subscriberToEdit.setVoicemailPin(voicemailPin);
        subscriberToEdit.setUsername(username);
        subscriberToEdit.setIsEmailNotificationEnabled(enableEmail);
        subscriberToEdit.setIsSmsNotificationEnabled(enableSms);
        subscriberToEdit.setEmailAddress(emailAddress);
        subscriberToEdit.setSmsAddress(smsAddress);

        ArrayList<Conference> conferenceList = new ArrayList<Conference>(subscriberToEdit.getConferences());

        if(conferenceList.size() > 0)
        {
            // subscribers only have one conference at this time, so just get the first entry for update
            Conference conferenceToEdit = conferenceList.get(0);
            Conference originalConference = conferenceToEdit.copy(true);

            conferenceToEdit.setDescription(subscriberToEdit.getUsername() + "'s Conference");
            persistenceService.update(conferenceToEdit, originalConference);
        }

        persistenceService.update(subscriberToEdit, originalSubscriber);
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
        Map<String, AccessNumber> existingNumbers = new HashMap<String, AccessNumber>();
        for(AccessNumber accessNumber : GetSubscriberServlet.getAccessNumbers(subscriber, session))
        {
            existingNumbers.put(accessNumber.getNumber(), accessNumber);
        }

        List<String> newNumbers = new ArrayList<String>();

        String[] split = accessNumberString.split(",");
        for(String an : split)
        {
            newNumbers.add(an.trim());
        }

        for(Map.Entry<String, AccessNumber> entry : existingNumbers.entrySet())
        {
            if(!newNumbers.contains(entry.getKey()))
            {
                session.delete(entry.getValue());
            }
        }

        for(String number : newNumbers)
        {
            Criteria criteria = session.createCriteria(AccessNumber.class);
            criteria.add(Restrictions.eq("number", number));
            criteria.setMaxResults(1);
            AccessNumber result = (AccessNumber)criteria.uniqueResult();

            if(result != null && !result.getSubscriber().equals(subscriber))
            {
                throw new BadRequestServletException("Access number [" + number +
                                                     "] is already in use by another account");
            }
            else if(result == null)
            {
                AccessNumber newNumber = new AccessNumber();
                newNumber.setNumber(number);
                newNumber.setSubscriber(subscriber);

                subscriber.addToAccessNumbers(newNumber);
                persistenceService.save(subscriber);
                persistenceService.save(newNumber);
            }
        }
    }
}
