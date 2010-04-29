package com.interact.listen.gui;

import com.interact.listen.ServletUtil;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.io.IOException;

import javax.servlet.http.*;

public class LogoutServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        // TODO should this be fixed?
        // we allow GET and POST requests to logout so we can do a window.location in javascript to log them out
        logout(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        logout(request, response);
    }

    private void logout(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        StatSender statSender = (StatSender)request.getSession().getServletContext().getAttribute("statSender");
        if(statSender == null)
        {
            statSender = new InsaStatSender();
        }
        statSender.send(Stat.GUI_LOGOUT);

        HttpSession session = request.getSession();
        session.removeAttribute("user");

        ServletUtil.redirect("/login", response);
    }
}
