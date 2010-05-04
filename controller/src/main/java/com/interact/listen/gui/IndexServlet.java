package com.interact.listen.gui;

import com.interact.listen.ServletUtil;
import com.interact.listen.resource.User;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.*;

public class IndexServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        HttpSession session = request.getSession();
        User user = (User)session.getAttribute("user");

        if(user == null)
        {
            ServletUtil.redirect("/login", response);
            return;
        }

        ServletUtil.forward("/WEB-INF/jsp/index.jsp", request, response);
    }
}
