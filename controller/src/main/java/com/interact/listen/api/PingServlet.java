package com.interact.listen.api;

import com.interact.listen.HibernateUtil;
import com.interact.listen.OutputBufferFilter;
import com.interact.listen.ServletUtil;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.Voicemail;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

public class PingServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        boolean auth = Boolean.valueOf(request.getParameter("auth"));

        if(auth)
        {
            if(ServletUtil.currentSubscriber(request) == null)
            {
                throw new UnauthorizedServletException("Not logged in");
            }
            Subscriber subscriber = ServletUtil.currentSubscriber(request);
            Session session = HibernateUtil.getSessionFactory().getCurrentSession();
            Long newVoicemailCount = Voicemail.countNewBySubscriber(session, subscriber);
            OutputBufferFilter.append(request, "{\"pong\":true,\"newVoicemailCount\":\"" + newVoicemailCount + "\"}",
                                      "application/json");
        }
        else
        {
            OutputBufferFilter.append(request, "pong", "text/plain");
        }
    }
}
