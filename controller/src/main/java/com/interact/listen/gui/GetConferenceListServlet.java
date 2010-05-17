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

import org.hibernate.Criteria;
import org.hibernate.Session;

/**
 * Provides a POST implementation that retrieves a list of Conferences for the current session user. If the user is an
 * Administrator, all conferences are returned. Otherwise, only conferences associated with the user are returned.
 */
public class GetConferenceListServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        User user = (User)(request.getSession().getAttribute("user"));
        if(user == null)
        {
            ServletUtil.writeResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "text/plain");
            return;
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

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
}
