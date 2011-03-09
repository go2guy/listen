package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.OutputBufferFilter;
import com.interact.listen.ServletUtil;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.resource.Subscriber;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.json.simple.JSONObject;

public class IsBlacklistedServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        Subscriber subscriber = ServletUtil.currentSubscriber(request);
        if(subscriber == null)
        {
            throw new UnauthorizedServletException("Not logged in");
        }

        String destination = ServletUtil.getNotNullNotEmptyString("destination", request, "Destination");
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        JSONObject result = new JSONObject();
        result.put("isBlacklisted", !subscriber.canDial(session, destination));

        OutputBufferFilter.append(request, result.toJSONString(), "application/json");
    }
}
