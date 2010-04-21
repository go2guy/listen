package com.interact.listen.gui;

import com.interact.listen.*;
import com.interact.listen.ResourceListService.Builder;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.json.JsonMarshaller;
import com.interact.listen.resource.Conference;
import com.interact.listen.resource.Participant;
import com.interact.listen.resource.User;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class GetConferenceParticipantsServlet extends HttpServlet
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
            // TODO we probably need a specific JSON marshaller for the web UI servlets - something that
            // doesn't return hrefs, but returns only the queried and necessary (e.g. paging) information
            Marshaller marshaller = new JsonMarshaller();
            Conference conference = Conference.findByPinNumber(user.getSubscriber().getNumber(), session);

            if(conference == null)
            {
                transaction.commit();
                ServletUtil.writeResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Conference not found", "text/plain");
                return;
            }

            Builder builder = new ResourceListService.Builder(Participant.class, session, marshaller)
                                  .addSearchProperty("conference", "/conferences/" + conference.getId())
                                  .addReturnField("id")
                                  .addReturnField("isAdmin")
                                  .addReturnField("isMuted")
                                  .addReturnField("isAdminMuted")
                                  .addReturnField("number");
            ResourceListService service = builder.build();
            String content = service.list();

            transaction.commit();

            ServletUtil.writeResponse(response, HttpServletResponse.SC_OK, content, marshaller.getContentType());
        }
        catch(CriteriaCreationException e)
        {
            e.printStackTrace();
            transaction.rollback();
            ServletUtil.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage(), "text/plain");
            return;
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
//TODO drop this down to a lower logging level? it's too frequent (since it's polled) by the javascript
            System.out.println("TIMER: GetConferenceParticipantsServlet.doGet() took " +
                               (System.currentTimeMillis() - start) + "ms");
        }
    }
}
