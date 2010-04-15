package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.ServletUtil;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.json.JsonMarshaller;
import com.interact.listen.resource.Conference;
import com.interact.listen.resource.User;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class GetConferenceInfoServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        long start = System.currentTimeMillis();

        User user = (User)(request.getSession().getAttribute("user"));
        if(user == null)
        {
            ServletUtil.writeResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "text/plain");
            return;
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction transaction = session.beginTransaction();

        try
        {
            Marshaller marshaller = new JsonMarshaller();

            // TODO factor this out into a general "find" method on a service/utility class
            Criteria criteria = session.createCriteria(Conference.class);
            criteria.add(Restrictions.eq("number", user.getSubscriber().getNumber()));
            criteria.setMaxResults(1);
            Conference conference = (Conference)criteria.uniqueResult();

            transaction.commit();

            if(conference == null)
            {
                ServletUtil.writeResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                          "Conference not found", "text/plain");
                return;
            }

            String content = marshaller.marshal(conference);
            ServletUtil.writeResponse(response, HttpServletResponse.SC_OK, content, marshaller.getContentType());
        }
        catch(Exception e)
        {
            e.printStackTrace();
            transaction.rollback();
            ServletUtil.writeResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage(),
                                      "text/plain");
            return;
        }
        finally
        {
            System.out.println("TIMER: GetConferenceInfoServlet.doGet() took " + (System.currentTimeMillis() - start) +
                               "ms");
        }
    }
}
