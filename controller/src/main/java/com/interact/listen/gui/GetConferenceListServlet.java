package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.ServletUtil;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.json.JsonMarshaller;
import com.interact.listen.resource.*;

import java.util.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * Provides a POST implementation that retrieves a list of Conferences for the current session user. If the user is an
 * Administrator, all conferences are returned. Otherwise, only conferences associated with the user are returned.
 */
public class GetConferenceListServlet extends HttpServlet
{
    private static final Logger LOG = Logger.getLogger(GetConferenceListServlet.class);
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
            List<Resource> conferences;

            if(user.getIsAdministrator())
            {
                Criteria criteria = session.createCriteria(Conference.class);
                criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
                conferences = (List<Resource>)criteria.list();
            }
            else
            {
                conferences = new ArrayList<Resource>(user.getConferences());
            }

            transaction.commit();

            Set<String> fields = new HashSet<String>();
            fields.add("description");
            fields.add("id");
            fields.add("isStarted");

            ResourceList list = new ResourceList();
            list.setFields(fields);
            list.setFirst(0);
            list.setList(conferences);
            list.setMax(conferences.size());
            list.setTotal(Long.valueOf(conferences.size()));

            Marshaller marshaller = new JsonMarshaller();
            String content = marshaller.marshal(list, Conference.class);
            ServletUtil.writeResponse(response, HttpServletResponse.SC_OK, content, marshaller.getContentType());
        }
        catch(Exception e)
        {
            LOG.error("Error getting conference list", e);
            transaction.rollback();
            ServletUtil.writeResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                      "Error retrieving conference list", "text/plain");
        }
        finally
        {
            LOG.debug("GetConferenceListServlet.toGet() took " + (System.currentTimeMillis() - start) + "ms");
        }
    }
}
