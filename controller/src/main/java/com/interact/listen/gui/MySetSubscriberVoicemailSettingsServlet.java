package com.interact.listen.gui;

import com.interact.listen.*;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.history.Channel;
import com.interact.listen.license.ListenFeature;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.Subscriber.PlaybackOrder;
import com.interact.listen.resource.TimeRestriction;
import com.interact.listen.resource.TimeRestriction.Action;
import com.interact.listen.stats.Stat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class MySetSubscriberVoicemailSettingsServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        ServletUtil.sendStat(request, Stat.GUI_EDIT_SUBSCRIBER);
        ServletUtil.requireLicensedFeature(ListenFeature.VOICEMAIL);
        Subscriber subscriber = ServletUtil.requireCurrentSubscriber(request, false);

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        PersistenceService ps = new DefaultPersistenceService(session, subscriber, Channel.GUI);

        Subscriber original = subscriber.copy(true);

        subscriber.setVoicemailPin(request.getParameter("voicemailPasscode"));
        subscriber.setIsSubscribedToTranscription(Boolean.valueOf(request.getParameter("transcribeVoicemail")));
        subscriber.setVoicemailPlaybackOrder(PlaybackOrder.valueOf(request.getParameter("playbackOrder")));

        if(Boolean.valueOf(request.getParameter("sendEmail")))
        {
            subscriber.setIsEmailNotificationEnabled(true);
            subscriber.setEmailAddress(request.getParameter("sendEmailToAddress"));
            setTimeRestrictions(request, ps, "sendEmailTimeRestrictions", Action.NEW_VOICEMAIL_EMAIL, subscriber);
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
            setTimeRestrictions(request, ps, "sendSmsTimeRestrictions", Action.NEW_VOICEMAIL_SMS, subscriber);
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

        ps.update(subscriber, original);
    }

    private void setTimeRestrictions(HttpServletRequest request, PersistenceService ps, String parameter,
                                     Action action, Subscriber subscriber) throws BadRequestServletException
    {
        try
        {
            JSONArray restrictions = (JSONArray)JSONValue.parse(request.getParameter(parameter));
            TimeRestriction.deleteBySubscriberAndAction(ps.getSession(), subscriber, action);
            for(int i = 0; i < restrictions.size(); i++)
            {
                JSONObject restriction = (JSONObject)restrictions.get(i);
                TimeRestriction tr = new TimeRestriction();

                tr.setStartEntry((String)restriction.get("from"));
                tr.setEndEntry((String)restriction.get("to"));

                tr.setStartTime(TimeRestriction.parseTime(tr.getStartEntry()));
                tr.setEndTime(TimeRestriction.parseTime(tr.getEndEntry()));

                if(!tr.getStartTime().isBefore(tr.getEndTime()))
                {
                    throw new BadRequestServletException("Time '" + tr.getStartEntry() + "' must be before '" + tr.getEndEntry() + "'");
                }

                tr.setMonday((Boolean)restriction.get("monday"));
                tr.setTuesday((Boolean)restriction.get("tuesday"));
                tr.setWednesday((Boolean)restriction.get("wednesday"));
                tr.setThursday((Boolean)restriction.get("thursday"));
                tr.setFriday((Boolean)restriction.get("friday"));
                tr.setSaturday((Boolean)restriction.get("saturday"));
                tr.setSunday((Boolean)restriction.get("sunday"));

                tr.setAction(action);
                tr.setSubscriber(subscriber);
                ps.save(tr);
            }
        }
        catch(IllegalArgumentException e)
        {
            throw new BadRequestServletException(e.getMessage());
        }
    }
}
