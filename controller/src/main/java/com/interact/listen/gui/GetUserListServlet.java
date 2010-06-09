package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.OutputBufferFilter;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.json.JsonMarshaller;
import com.interact.listen.resource.Resource;
import com.interact.listen.resource.ResourceList;
import com.interact.listen.resource.User;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Criteria;
import org.hibernate.Session;

/**
 * Provides a GET implementation that retrieves a list of Users.
 */
public class GetUserListServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        User user = (User)(request.getSession().getAttribute("user"));
        if(user == null)
        {
            throw new UnauthorizedServletException("Not logged in");
        }

        StatSender statSender = (StatSender)request.getSession().getServletContext().getAttribute("statSender");
        if(statSender == null)
        {
            statSender = new InsaStatSender();
        }
        statSender.send(Stat.GUI_GET_USER_LIST);

        if(!user.getIsAdministrator())
        {
            throw new UnauthorizedServletException("Unauthorized - Insufficient permissions");
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        Criteria criteria = session.createCriteria(User.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        List<Resource> users = (List<Resource>)criteria.list();

        Set<String> fields = new HashSet<String>();
        fields.add("id");
        fields.add("lastLogin");
        fields.add("username");

        ResourceList list = new ResourceList();
        list.setFields(fields);
        list.setFirst(0);
        list.setList(users);
        list.setMax(users.size());
        list.setTotal(Long.valueOf(users.size()));

        Marshaller marshaller = new JsonMarshaller();
        String content = marshaller.marshal(list, User.class);

        response.setStatus(HttpServletResponse.SC_OK);
        OutputBufferFilter.append(request, content, marshaller.getContentType());
    }
}
