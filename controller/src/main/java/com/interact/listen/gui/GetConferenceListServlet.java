package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.OutputBufferFilter;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.license.License;
import com.interact.listen.license.ListenFeature;
import com.interact.listen.license.NotLicensedException;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.json.JsonMarshaller;
import com.interact.listen.resource.*;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Criteria;
import org.hibernate.Session;

/**
 * Provides a GET implementation that retrieves a list of Conferences for the current session subscriber. If the
 * subscriber is an Administrator, all conferences are returned. Otherwise, only conferences associated with the
 * subscriber are returned.
 */
public class GetConferenceListServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        if(!License.isLicensed(ListenFeature.CONFERENCING))
        {
            throw new NotLicensedException(ListenFeature.CONFERENCING);
        }

        StatSender statSender = (StatSender)request.getSession().getServletContext().getAttribute("statSender");
        if(statSender == null)
        {
            statSender = new InsaStatSender();
        }
        statSender.send(Stat.GUI_GET_CONFERENCE_LIST);

        Subscriber subscriber = (Subscriber)(request.getSession().getAttribute("subscriber"));
        if(subscriber == null)
        {
            throw new UnauthorizedServletException("Not logged in");
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        List<Resource> conferences;

        if(subscriber.getIsAdministrator())
        {
            Criteria criteria = session.createCriteria(Conference.class);
            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            conferences = (List<Resource>)criteria.list();
        }
        else
        {
            conferences = new ArrayList<Resource>(subscriber.getConferences());
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

        response.setStatus(HttpServletResponse.SC_OK);
        OutputBufferFilter.append(request, content, marshaller.getContentType());
    }
}
