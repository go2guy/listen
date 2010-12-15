package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.ServletUtil;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.license.License;
import com.interact.listen.license.ListenFeature;
import com.interact.listen.license.NotLicensedException;
import com.interact.listen.resource.Participant;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.spot.SpotCommunicationException;
import com.interact.listen.spot.SpotSystem;
import com.interact.listen.stats.Stat;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

public class MuteParticipantServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        if(!License.isLicensed(ListenFeature.CONFERENCING))
        {
            throw new NotLicensedException(ListenFeature.CONFERENCING);
        }

        ServletUtil.sendStat(request, Stat.GUI_MUTE_PARTICIPANT);

        Subscriber subscriber = ServletUtil.currentSubscriber(request);
        if(subscriber == null)
        {
            throw new UnauthorizedServletException();
        }

        Long id = ServletUtil.getNotNullLong("id", request, "Id");

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        Participant participant = Participant.queryById(session, id);
        if(!subscriber.canModifyParticipant(participant))
        {
            throw new UnauthorizedServletException("Not allowed to mute participant");
        }

        SpotSystem spotSystem = new SpotSystem(subscriber);
        try
        {
            spotSystem.muteParticipant(participant);
        }
        catch(SpotCommunicationException e)
        {
            throw new ServletException(e);
        }
    }
}
