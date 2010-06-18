package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.OutputBufferFilter;
import com.interact.listen.ResourceListService;
import com.interact.listen.ResourceListService.Builder;
import com.interact.listen.exception.CriteriaCreationException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.license.License;
import com.interact.listen.license.ListenFeature;
import com.interact.listen.license.NotLicensedException;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.converter.FriendlyIso8601DateConverter;
import com.interact.listen.marshal.json.JsonMarshaller;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.Voicemail;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

public class GetVoicemailListServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        if(!License.isLicensed(ListenFeature.VOICEMAIL))
        {
            throw new ServletException(new NotLicensedException(ListenFeature.VOICEMAIL));
        }

        StatSender statSender = (StatSender)request.getSession().getServletContext().getAttribute("statSender");
        if(statSender == null)
        {
            statSender = new InsaStatSender();
        }
        statSender.send(Stat.GUI_GET_VOICEMAIL_LIST);

        Subscriber subscriber = (Subscriber)(request.getSession().getAttribute("subscriber"));
        if(subscriber == null)
        {
            throw new UnauthorizedServletException("Not logged in");
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Marshaller marshaller = new JsonMarshaller();
        marshaller.registerConverterClass(Date.class, FriendlyIso8601DateConverter.class);

        Builder builder = new ResourceListService.Builder(Voicemail.class, session, marshaller)
            .addSearchProperty("subscriber", "/subscribers/" + subscriber.getId())
            .addReturnField("dateCreated")
            .addReturnField("id")
            .addReturnField("isNew")
            .addReturnField("leftBy")
            .sortBy("dateCreated", ResourceListService.SortOrder.DESCENDING)
            .withMax(100);
        ResourceListService service = builder.build();

        try
        {
            StringBuilder content = new StringBuilder();
            content.append("{");
            content.append("\"newCount\":").append(getNewCount(session, subscriber)).append(",");
            content.append("\"list\":").append(service.list());
            content.append("}");
            OutputBufferFilter.append(request, content.toString(), marshaller.getContentType());
        }
        catch(CriteriaCreationException e)
        {
            throw new ServletException(e);
        }
    }
    
    private Long getNewCount(Session session, Subscriber subscriber)
    {
        Criteria criteria = session.createCriteria(Voicemail.class);

        // only new records
        criteria.add(Restrictions.eq("isNew", true));

        // belonging to this subscriber
        criteria.createAlias("subscriber", "subscriber_alias");
        criteria.add(Restrictions.eq("subscriber_alias.id", subscriber.getId()));

        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.setFirstResult(0);
        criteria.setProjection(Projections.rowCount());

        return (Long)criteria.list().get(0);
    }
}
