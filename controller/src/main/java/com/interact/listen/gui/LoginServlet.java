package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.ServletUtil;
import com.interact.listen.resource.User;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import javax.servlet.http.*;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class LoginServlet extends HttpServlet
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
        statSender.send(Stat.GUI_LOGIN);

        Session hibernateSession = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction transaction = hibernateSession.beginTransaction();

        try
        {
            HttpSession httpSession = request.getSession(true);

            String username = request.getParameter("username");
            String password = request.getParameter("password");

            System.out.println("POST LoginServlet username=" + username + "&password=" + password);

            if(username == null || username.trim().equals(""))
            {
                ServletUtil.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Please provide a username",
                                          "text/plain");
                transaction.commit();
                return;
            }

            if(password == null)
            {
                ServletUtil.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Please provide a password",
                                          "text/plain");
                transaction.commit();
                return;
            }

            User user = findUserByUsername(username, hibernateSession);

            if(user == null)
            {
                ServletUtil.writeResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                                          "Sorry, that's not a valid username/password", "text/plain");
                transaction.commit();
                return;
            }

            if(!isValidPassword(user, password))
            {
                ServletUtil.writeResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                                          "Sorry, that's not a valid username/password", "text/plain");
                transaction.commit();
                return;
            }

            httpSession.setAttribute("user", user);

            transaction.commit();
        }
        finally
        {
            System.out.println("TIMER: LoginServlet.goPost() took " + (System.currentTimeMillis() - start) + "ms");
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
        // FIXME no plaintext passwords, do some SHA1ing
        return user.getPassword().equals(password);
    }
}
