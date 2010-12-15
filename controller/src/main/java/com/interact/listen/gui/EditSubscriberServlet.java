package com.interact.listen.gui;

import com.interact.listen.*;
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
import com.interact.listen.stats.Stat;

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
        ServletUtil.sendStat(request, Stat.GUI_EDIT_SUBSCRIBER);

        Subscriber currentSubscriber = ServletUtil.currentSubscriber(request);
        if(currentSubscriber == null)
        {
            throw new UnauthorizedServletException();
        }
        
        Long id = ServletUtil.getNotNullLong("id", request, "Id");
        
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Subscriber subscriberToEdit = Subscriber.queryById(session, id);

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
                String username = ServletUtil.getNotNullNotEmptyString("username", request, "Username");
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

        PersistenceService persistenceService = new DefaultPersistenceService(session, currentSubscriber, Channel.GUI);

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
            subscriberToEdit.setVoicemailPin(voicemailPin);

            Boolean enableEmail = Boolean.valueOf(request.getParameter("enableEmail"));
            Boolean enableSms = Boolean.valueOf(request.getParameter("enableSms"));
            String emailAddress = request.getParameter("emailAddress");
            String smsAddress = request.getParameter("smsAddress");
            Boolean enablePaging = Boolean.valueOf(request.getParameter("enablePaging"));
            Boolean enableTranscription = Boolean.valueOf(request.getParameter("enableTranscription"));
            
            PlaybackOrder playbackOrder = PlaybackOrder.valueOf(request.getParameter("voicemailPlaybackOrder"));

            subscriberToEdit.setIsEmailNotificationEnabled(enableEmail);
            subscriberToEdit.setIsSmsNotificationEnabled(enableSms);
            subscriberToEdit.setEmailAddress(emailAddress);
            subscriberToEdit.setSmsAddress(smsAddress);
            subscriberToEdit.setIsSubscribedToPaging(enablePaging);
            subscriberToEdit.setIsSubscribedToTranscription(enableTranscription);
            subscriberToEdit.setVoicemailPlaybackOrder(playbackOrder);
        }

        subscriberToEdit.setRealName(request.getParameter("realName"));

        if(request.getParameter("enableAdmin") != null)
        {
            Boolean enableAdmin = Boolean.valueOf(request.getParameter("enableAdmin"));
            subscriberToEdit.setIsAdministrator(enableAdmin);
        }

        ArrayList<Conference> conferenceList = new ArrayList<Conference>(subscriberToEdit.getConferences());

        if(conferenceList.size() > 0)
        {
            // subscribers only have one conference at this time, so just get the first entry for update
            Conference conferenceToEdit = conferenceList.get(0);
            Conference originalConference = conferenceToEdit.copy(true);

            conferenceToEdit.setDescription(subscriberToEdit.conferenceDescription());
            persistenceService.update(conferenceToEdit, originalConference);
        }

        if(!subscriberToEdit.validate())
        {
            throw new BadRequestServletException(subscriberToEdit.errors().get(0));
        }
        persistenceService.update(subscriberToEdit, originalSubscriber);
    }
}
