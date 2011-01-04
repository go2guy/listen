package com.interact.listen.gui;

import com.interact.listen.*;
import com.interact.listen.exception.*;
import com.interact.listen.history.Channel;
import com.interact.listen.license.License;
import com.interact.listen.license.ListenFeature;
import com.interact.listen.resource.Conference;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.Subscriber.PlaybackOrder;
import com.interact.listen.security.SecurityUtil;
import com.interact.listen.stats.Stat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.classic.Session;
import org.hibernate.exception.ConstraintViolationException;

public class AddSubscriberServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    
    private static final Logger LOG = Logger.getLogger(AddSubscriberServlet.class);

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        ServletUtil.sendStat(request, Stat.GUI_ADD_SUBSCRIBER);

        Subscriber currentSubscriber = ServletUtil.currentSubscriber(request);
        if(currentSubscriber == null)
        {
            throw new UnauthorizedServletException();
        }

        if(!currentSubscriber.getIsAdministrator())
        {
            throw new UnauthorizedServletException();
        }

        Subscriber subscriber = new Subscriber();

        String username = ServletUtil.getNotNullNotEmptyString("username", request, "Username");
        String password = ServletUtil.getNotNullNotEmptyString("password", request, "Password");
        String confirmPassword = ServletUtil.getNotNullNotEmptyString("confirmPassword", request, "Confirm Password");

        if(!password.equals(confirmPassword))
        {
            throw new BadRequestServletException("Password and Confirm Password do not match");
        }

        subscriber.setUsername(username);
        subscriber.setPassword(SecurityUtil.hashPassword(password));

        if(License.isLicensed(ListenFeature.VOICEMAIL))
        {
            String voicemailPin = request.getParameter("voicemailPin");
            subscriber.setVoicemailPin(voicemailPin);

            Boolean enableEmail = Boolean.valueOf(request.getParameter("enableEmail"));
            Boolean enableSms = Boolean.valueOf(request.getParameter("enableSms"));
            String emailAddress = request.getParameter("emailAddress");
            String smsAddress = request.getParameter("smsAddress");
            Boolean enablePaging = Boolean.valueOf(request.getParameter("enablePaging"));
            Boolean enableTranscription = Boolean.valueOf(request.getParameter("enableTranscription"));
            PlaybackOrder playbackOrder = PlaybackOrder.valueOf(request.getParameter("voicemailPlaybackOrder"));

            subscriber.setIsEmailNotificationEnabled(enableEmail);
            subscriber.setIsSmsNotificationEnabled(enableSms);
            subscriber.setEmailAddress(emailAddress);
            subscriber.setSmsAddress(smsAddress);
            subscriber.setIsSubscribedToPaging(enablePaging);
            subscriber.setIsSubscribedToTranscription(enableTranscription);
            subscriber.setVoicemailPlaybackOrder(playbackOrder);
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        PersistenceService persistenceService = new DefaultPersistenceService(session, currentSubscriber, Channel.GUI);

        subscriber.setRealName(request.getParameter("realName"));

        Boolean enableAdmin = Boolean.valueOf(request.getParameter("enableAdmin"));
        subscriber.setIsAdministrator(enableAdmin);

        try
        {
            if(!subscriber.validate())
            {
                throw new BadRequestServletException(subscriber.errors().get(0));
            }
            persistenceService.save(subscriber);
        }
        catch(ConstraintViolationException e)
        {
            throw new BadRequestServletException("A subscriber with that username already exists");
        }

        String accessNumbers = request.getParameter("accessNumbers");
        if(accessNumbers != null && accessNumbers.length() > 0)
        {
            try
            {
                subscriber.updateAccessNumbers(session, persistenceService, accessNumbers, true);
            }
            catch(NumberAlreadyInUseException e)
            {
                throw new BadRequestServletException("Access number [" + e.getNumber() +
                                                     "] is already in use by another account");
            }
            catch(UnauthorizedModificationException e)
            {
                LOG.error(e.getMessage());
                throw new UnauthorizedServletException();
            }
        }

        Conference.createNew(persistenceService, subscriber);
    }
}
