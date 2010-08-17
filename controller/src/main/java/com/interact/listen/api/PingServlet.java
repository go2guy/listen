package com.interact.listen.api;

import com.interact.listen.OutputBufferFilter;
import com.interact.listen.ServletUtil;
import com.interact.listen.exception.UnauthorizedServletException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PingServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        if(request.getParameter("auth") != null && Boolean.valueOf(request.getParameter("auth")))
        {
            if(ServletUtil.currentSubscriber(request) == null)
            {
                throw new UnauthorizedServletException("Not logged in");
            }
        }
        OutputBufferFilter.append(request, "pong", "text/plain");
    }
}
