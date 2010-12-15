package com.interact.listen.gui;

import com.interact.listen.*;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.history.Channel;
import com.interact.listen.license.License;
import com.interact.listen.license.ListenFeature;
import com.interact.listen.license.NotLicensedException;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.Voicemail;
import com.interact.listen.stats.Stat;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

public class MarkVoicemailReadStatusServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        if(!License.isLicensed(ListenFeature.VOICEMAIL))
        {
            throw new NotLicensedException(ListenFeature.VOICEMAIL);
        }

        ServletUtil.sendStat(request, Stat.GUI_MARK_VOICEMAIL_READ_STATUS);

        Subscriber subscriber = ServletUtil.currentSubscriber(request);
        if(subscriber == null)
        {
            throw new UnauthorizedServletException("Not logged in");
        }

        Long id = ServletUtil.getNotNullLong("id", request, "Id");
        String readStatus = ServletUtil.getNotNullNotEmptyString("readStatus", request, "Read Status");

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        PersistenceService persistenceService = new DefaultPersistenceService(session, subscriber, Channel.GUI);

        Voicemail voicemail = Voicemail.queryById(session, id);
        if(!(subscriber.getIsAdministrator() || subscriber.equals(voicemail.getSubscriber())))
        {
            throw new UnauthorizedServletException("Not allowed to change voicemail");
        }

        Voicemail original = voicemail.copy(false);
        voicemail.setIsNew(!Boolean.valueOf(readStatus));
        persistenceService.update(voicemail, original);
    }
}
