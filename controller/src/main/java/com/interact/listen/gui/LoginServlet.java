package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.ServletUtil;
import com.interact.listen.resource.User;
import com.interact.listen.security.SecurityUtil;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class LoginServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        HttpSession session = request.getSession();
        request.setAttribute("errors", session.getAttribute("errors"));
        session.removeAttribute("errors");

        ServletUtil.forward("/WEB-INF/jsp/login.jsp", request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        long start = System.currentTimeMillis();

        StatSender statSender = (StatSender)request.getSession().getServletContext().getAttribute("statSender");
        if(statSender == null)
        {
            statSender = new InsaStatSender();
        }
        statSender.send(Stat.GUI_LOGIN);

        Session hibernateSession = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction transaction = hibernateSession.beginTransaction();

        try
        {
            HttpSession httpSession = request.getSession(true);

            String username = request.getParameter("username");
            String password = request.getParameter("password");

            System.out.println("POST LoginServlet username=" + username + "&password=*");

            Map<String, String> errors = new HashMap<String, String>();

            if(username == null || username.trim().equals(""))
            {
                errors.put("username", "Please provide a username");
            }

            if(password == null || password.trim().equals(""))
            {
                errors.put("password", "Please provide a password");
            }

            if(errors.size() == 0)
            {
                User user = findUserByUsername(username, hibernateSession);
                if(user == null || !isValidPassword(user, password))
                {
                    errors.put("username", "Sorry, those aren't valid credentials");
                }

                httpSession.setAttribute("user", user);
            }

            transaction.commit();

            if(errors.size() > 0)
            {
                httpSession.setAttribute("errors", errors);
                ServletUtil.redirect("/login", response);
            }
            else
            {
                ServletUtil.redirect("/index", response);
            }
        }
        finally
        {
            System.out.println("TIMER: LoginServlet.doPost() took " + (System.currentTimeMillis() - start) + "ms");
        }
    }

    private User findUserByUsername(String username, Session session)
    {
        Criteria criteria = session.createCriteria(User.class);
        criteria.add(Restrictions.eq("username", username));
        criteria.setMaxResults(1);
        return (User)criteria.uniqueResult();
    }

    private boolean isValidPassword(User user, String password)
    {
        return user.getPassword().equals(SecurityUtil.hashPassword(password));
    }
}
