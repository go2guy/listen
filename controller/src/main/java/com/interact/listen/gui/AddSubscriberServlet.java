package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.PersistenceService;
import com.interact.listen.ServletUtil;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.history.Channel;
import com.interact.listen.resource.Conference;
import com.interact.listen.resource.Pin;
import com.interact.listen.resource.Pin.PinType;
import com.interact.listen.resource.Subscriber;
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

        Subscriber currentSubscriber = ServletUtil.currentSubscriber(request);
        if(currentSubscriber == null)
        {
            throw new UnauthorizedServletException();
        }

        if(!currentSubscriber.getIsAdministrator())
        {
            throw new UnauthorizedServletException();
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
        
        String voicemailPinString = request.getParameter("voicemailPin");
        if(voicemailPinString == null || voicemailPinString.trim().equals(""))
        {
            throw new BadRequestServletException("Please provide a Voicemail Pin Number");
        }
        
        if(voicemailPinString.length() > 10)
        {
            throw new BadRequestServletException("Please provide a Voicemail Pin number with ten digits or less");
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

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        PersistenceService persistenceService = new PersistenceService(session, currentSubscriber, Channel.GUI);

        Subscriber subscriber = new Subscriber();
        subscriber.setPassword(SecurityUtil.hashPassword(password));
        subscriber.setUsername(username);
        subscriber.setVoicemailPin(voicemailPin);
        subscriber.setIsEmailNotificationEnabled(enableEmail);
        subscriber.setIsSmsNotificationEnabled(enableSms);
        subscriber.setEmailAddress(emailAddress);
        subscriber.setSmsAddress(smsAddress);
        
        try
        {
            persistenceService.save(subscriber);
        }
        catch(Exception e)
        {
            throw new BadRequestServletException("how dare you try to do this shit?");
        }

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
