package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.PersistenceService;
import com.interact.listen.ServletUtil;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.NumberAlreadyInUseException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.history.Channel;
import com.interact.listen.license.License;
import com.interact.listen.license.ListenFeature;
import com.interact.listen.resource.Conference;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.Subscriber.PlaybackOrder;
import com.interact.listen.security.SecurityUtil;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

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
        Subscriber subscriberToEdit = Subscriber.queryById(session, Long.parseLong(id));

        if(subscriberToEdit == null)
        {
            throw new BadRequestServletException("Subscriber not found");
        }
        
        Subscriber originalSubscriber = subscriberToEdit.copy(true);

        if(!currentSubscriber.getIsAdministrator() && !currentSubscriber.getId().equals(subscriberToEdit.getId()))
        {
            throw new UnauthorizedServletException();
        }

        if(!subscriberToEdit.getIsActiveDirectory())
        {
            // username can only be changed by admin, and only if it's not an AD account
            if(currentSubscriber.getIsAdministrator())
            {
                String username = request.getParameter("username");
                if(username == null || username.trim().equals(""))
                {
                    throw new BadRequestServletException("Please provide a Username");
                }
                subscriberToEdit.setUsername(username);
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
        }

        PersistenceService persistenceService = new PersistenceService(session, currentSubscriber, Channel.GUI);

        String accessNumbers = request.getParameter("accessNumbers");
        if(currentSubscriber.getIsAdministrator() && accessNumbers != null)
        {
            try
            {
                subscriberToEdit.updateAccessNumbers(session, persistenceService, accessNumbers);
            }
            catch(NumberAlreadyInUseException e)
            {
                throw new BadRequestServletException("Access number [" + e.getNumber() +
                                                     "] is already in use by another account");
            }
        }

        if(License.isLicensed(ListenFeature.VOICEMAIL))
        {
            String voicemailPin = request.getParameter("voicemailPin");
            if(voicemailPin != null && voicemailPin.length() > 10)
            {
                throw new BadRequestServletException("Please provide a Voicemail PIN with ten digits or less");
            }
            else if(voicemailPin != null && voicemailPin.trim().length() > 0)
            {
                try
                {
                    subscriberToEdit.setVoicemailPin(Long.valueOf(voicemailPin));
                }
                catch(NumberFormatException e)
                {
                    throw new BadRequestServletException("Voicemail PIN must be a number");
                }
            }

            Boolean enableEmail = Boolean.valueOf(request.getParameter("enableEmail"));
            Boolean enableSms = Boolean.valueOf(request.getParameter("enableSms"));
            String emailAddress = request.getParameter("emailAddress");
            String smsAddress = request.getParameter("smsAddress");
            Boolean enablePaging = Boolean.valueOf(request.getParameter("enablePaging"));
            PlaybackOrder playbackOrder = PlaybackOrder.valueOf(request.getParameter("voicemailPlaybackOrder"));
            
            if(enableEmail && (emailAddress == null || emailAddress.equals("")))
            {
                throw new BadRequestServletException("Please provide an E-mail address");
            }
            
            if(enableSms && (smsAddress == null || smsAddress.equals("")))
            {
                throw new BadRequestServletException("Please provide an SMS address");
            }
            
            if(enablePaging && (smsAddress == null || smsAddress.equals("")))
            {
                throw new BadRequestServletException("Please provide an SMS address for paging");
            }

            subscriberToEdit.setIsEmailNotificationEnabled(enableEmail);
            subscriberToEdit.setIsSmsNotificationEnabled(enableSms);
            subscriberToEdit.setEmailAddress(emailAddress);
            subscriberToEdit.setSmsAddress(smsAddress);
            subscriberToEdit.setIsSubscribedToPaging(enablePaging);
            subscriberToEdit.setVoicemailPlaybackOrder(playbackOrder);
        }

        subscriberToEdit.setRealName(request.getParameter("realName"));

        ArrayList<Conference> conferenceList = new ArrayList<Conference>(subscriberToEdit.getConferences());

        if(conferenceList.size() > 0)
        {
            // subscribers only have one conference at this time, so just get the first entry for update
            Conference conferenceToEdit = conferenceList.get(0);
            Conference originalConference = conferenceToEdit.copy(true);

            conferenceToEdit.setDescription(subscriberToEdit.conferenceDescription());
            persistenceService.update(conferenceToEdit, originalConference);
        }

        persistenceService.update(subscriberToEdit, originalSubscriber);
    }
}
