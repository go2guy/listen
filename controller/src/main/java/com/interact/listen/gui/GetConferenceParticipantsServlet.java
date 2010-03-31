package com.interact.listen.gui;

import com.interact.listen.*;
import com.interact.listen.ResourceListService.Builder;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.json.JsonMarshaller;
import com.interact.listen.resource.Participant;

import java.io.PrintWriter;

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

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction transaction = session.beginTransaction();

        try
        {
            // TODO we probably need a specific JSON marshaller for the web UI servlets - something that
            // doesn't return hrefs, but returns only the queried and necessary (e.g. paging) information
            Marshaller marshaller = new JsonMarshaller();

            String conferenceId = request.getParameter("conference");
            Builder builder = new ResourceListService.Builder(Participant.class, session, marshaller)
                                  .addSearchProperty("conference", "/conferences/" + conferenceId)
                                  .addReturnField("number")
                                  .addReturnField("isAdmin")
                                  .addReturnField("isMuted")
                                  .addReturnField("isHolding");
            ResourceListService service = builder.build();
            String content = service.list();

            transaction.commit();

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(marshaller.getContentType());
            response.setContentLength(content.length());

            PrintWriter writer = response.getWriter();
            writer.print(content);
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
            System.out.println("TIMER: GetConferenceParticipantsServlet.doGet() took " +
                               (System.currentTimeMillis() - start) + "ms");
        }
    }
}
