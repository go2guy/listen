package com.interact.listen.gui;

import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import javax.servlet.http.*;

public class LogoutServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
    {
        long start = System.currentTimeMillis();

        StatSender statSender = (StatSender)request.getSession().getServletContext().getAttribute("statSender");
        if(statSender == null)
        {
            statSender = new InsaStatSender();
        }
        statSender.send(Stat.GUI_LOGOUT);

        HttpSession httpSession = request.getSession();
        httpSession.removeAttribute("user");

        System.out.println("TIMER: LogoutServlet.doPost() took " + (System.currentTimeMillis() - start) + "ms");
    }
}
