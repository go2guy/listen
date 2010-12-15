package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import com.interact.listen.ListenServletTest;
import com.interact.listen.TestUtil;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.resource.Subscriber;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Test;

public class DeleteSubscriberServletTest extends ListenServletTest
{
    private DeleteSubscriberServlet servlet = new DeleteSubscriberServlet();

    @Test
    public void test_doPost_withSuccessfulConditions_deletesSubscriber() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);

        Subscriber subscriber = createSubscriber(session);
        doPost(subscriber.getId());
        assertNull(Subscriber.queryById(session, subscriber.getId()));
    }

    @Test
    public void test_doPost_withCurrentSubscriberNotAdmin_throwsException() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, false, session);

        Subscriber subscriber = createSubscriber(session);
        testDoPostForUnauthorizedServletException(subscriber.getId(), "Unauthorized - Insufficient privileges");
    }

    @Test
    public void test_doPost_withNoCurrentSubscriber_throwsException() throws ServletException, IOException
    {
        Subscriber subscriber = createSubscriber(session);
        testDoPostForUnauthorizedServletException(subscriber.getId(), "Unauthorized - Not logged in");
    }

    // TODO
    public void test_doPost_whenSubscriberDeleted_sendsStat() throws ServletException, IOException
    {}

    @Test
    public void test_doPost_withNoIdParameter_throwsException() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);
        testDoPostForBadRequestServletException(null, "Id cannot be null");
    }

    @Test
    public void test_doPost_withSubscriberNotFound_throwsException() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, false, session);

        Long id;
        do
        {
            id = TestUtil.randomNumeric(12);
        }
        while(Subscriber.queryById(session, id) != null);

        testDoPostForBadRequestServletException(id, "Subscriber not found");
    }

    @Test
    public void test_doPost_whenTryingToDeleteCurrentSubscriber_throwsException() throws ServletException, IOException
    {
        Subscriber subscriber = TestUtil.setSessionSubscriber(request, true, session);
        testDoPostForBadRequestServletException(subscriber.getId(), "Cannot delete yourself");
    }

    private void doPost(Long subscriberId) throws ServletException, IOException
    {
        request.setMethod("POST");
        if(subscriberId != null)
        {
            request.setParameter("id", String.valueOf(subscriberId));
        }
        servlet.service(request, response);
    }

    private void testDoPostForBadRequestServletException(Long subscriberId, String expectedContent)
        throws ServletException, IOException
    {
        try
        {
            doPost(subscriberId);
            fail("Expected BadRequestServletException");
        }
        catch(BadRequestServletException e)
        {
            assertEquals(expectedContent, e.getContent());
        }
    }

    private void testDoPostForUnauthorizedServletException(Long subscriberId, String expectedContent)
        throws ServletException, IOException
    {
        try
        {
            doPost(subscriberId);
            fail("Expected UnauthorizedServletException");
        }
        catch(UnauthorizedServletException e)
        {
            assertEquals(expectedContent, e.getContent());
        }
    }
}
