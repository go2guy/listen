package com.interact.listen.gui;

import com.interact.listen.*;
import com.interact.listen.exception.BadRequestServletException;
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

        String id = request.getParameter("id");
        if(id == null || id.trim().equals(""))
        {
            throw new BadRequestServletException("Please provide an id");
        }

        String readStatus = request.getParameter("readStatus");
        if(readStatus == null || readStatus.trim().equals(""))
        {
            throw new BadRequestServletException("Please provide a readStatus");
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        PersistenceService persistenceService = new DefaultPersistenceService(session, subscriber, Channel.GUI);

        Voicemail voicemail = (Voicemail)session.get(Voicemail.class, Long.valueOf(id));
        if(!(subscriber.getIsAdministrator() || subscriber.equals(voicemail.getSubscriber())))
        {
            throw new UnauthorizedServletException("Not allowed to change voicemail");
        }

        Voicemail original = voicemail.copy(false);
        voicemail.setIsNew(!Boolean.valueOf(readStatus));
        persistenceService.update(voicemail, original);
    }
}
