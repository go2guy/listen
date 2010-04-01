package com.interact.listen.gui;

import javax.servlet.http.*;

public class LogoutServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
    {
        long start = System.currentTimeMillis();

        HttpSession httpSession = request.getSession();
        httpSession.removeAttribute("user");

        System.out.println("TIMER: LogoutServlet.doPost() took " + (System.currentTimeMillis() - start) + "ms");
    }
}
