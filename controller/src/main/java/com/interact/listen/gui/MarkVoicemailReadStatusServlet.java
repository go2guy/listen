package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.PersistenceService;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.license.License;
import com.interact.listen.license.ListenFeature;
import com.interact.listen.license.NotLicensedException;
import com.interact.listen.resource.User;
import com.interact.listen.resource.Voicemail;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

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

        StatSender statSender = (StatSender)request.getSession().getServletContext().getAttribute("statSender");
        if(statSender == null)
        {
            statSender = new InsaStatSender();
        }
        statSender.send(Stat.GUI_MARK_VOICEMAIL_READ_STATUS);

        User user = (User)(request.getSession().getAttribute("user"));
        if(user == null)
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
        PersistenceService persistenceService = new PersistenceService(session);

        Voicemail voicemail = (Voicemail)session.get(Voicemail.class, Long.valueOf(id));
        if(!(user.getIsAdministrator() || user.getSubscriber() != null || user.getSubscriber()
                                                                              .equals(voicemail.getSubscriber())))
        {
            throw new UnauthorizedServletException("Not allowed to change voicemail");
        }

        Voicemail original = voicemail.copy(false);
        voicemail.setIsNew(!Boolean.valueOf(readStatus));
        persistenceService.update(voicemail, original);
    }
}
