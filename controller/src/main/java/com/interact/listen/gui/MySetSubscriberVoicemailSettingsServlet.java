package com.interact.listen.gui;

import com.interact.listen.*;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.history.Channel;
import com.interact.listen.license.ListenFeature;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.Subscriber.PlaybackOrder;
import com.interact.listen.stats.Stat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

public class MySetSubscriberVoicemailSettingsServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        ServletUtil.sendStat(request, Stat.GUI_EDIT_SUBSCRIBER);
        ServletUtil.requireLicensedFeature(ListenFeature.VOICEMAIL);
        Subscriber subscriber = ServletUtil.requireCurrentSubscriber(request, false);

        Subscriber original = subscriber.copy(true);

        subscriber.setVoicemailPin(request.getParameter("voicemailPasscode"));
        subscriber.setIsSubscribedToTranscription(Boolean.valueOf(request.getParameter("transcribeVoicemail")));
        subscriber.setVoicemailPlaybackOrder(PlaybackOrder.valueOf(request.getParameter("playbackOrder")));

        if(Boolean.valueOf(request.getParameter("sendEmail")))
        {
            subscriber.setIsEmailNotificationEnabled(true);
            subscriber.setEmailAddress(request.getParameter("sendEmailToAddress"));
        }
        else
        {
            subscriber.setIsEmailNotificationEnabled(false);
        }

        if(Boolean.valueOf(request.getParameter("sendSms")))
        {
            subscriber.setIsSmsNotificationEnabled(true);
            subscriber.setSmsAddress(request.getParameter("sendSmsToAddress"));
            subscriber.setIsSubscribedToPaging(Boolean.valueOf(request.getParameter("keepSendingSms")));
        }
        else
        {
            subscriber.setIsSmsNotificationEnabled(false);
        }
        
        if(!subscriber.validate())
        {
            String message = subscriber.errors().get(0);
            subscriber.clearErrors();
            throw new BadRequestServletException(message);
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        PersistenceService ps = new DefaultPersistenceService(session, subscriber, Channel.GUI);
        ps.update(subscriber, original);
    }
}
