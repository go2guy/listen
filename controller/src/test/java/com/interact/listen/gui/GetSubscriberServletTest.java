package com.interact.listen.gui;

import com.interact.listen.ListenServletTest;
import com.interact.listen.ServletUtil;
import com.interact.listen.TestUtil;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.converter.FriendlyIso8601DateConverter;
import com.interact.listen.marshal.json.JsonMarshaller;
import com.interact.listen.resource.Subscriber;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;

import org.junit.Test;

public class GetSubscriberServletTest extends ListenServletTest
{
    private GetSubscriberServlet servlet = new GetSubscriberServlet();

    @Test
    public void test_doGet_withNoCurrentSubscriber_throwsUnauthorized() throws ServletException, IOException
    {
        assert ServletUtil.currentSubscriber(request) == null;

        request.setMethod("GET");
        testForListenServletException(servlet, 401, "Unauthorized - Not logged in");
    }

    @Test
    public void test_doGet_withIdSpecifiedAndUserNotAdminAndIdNotForCurrentSubscriber_throwsUnauthorized()
        throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, false, session); // not admin
        Subscriber targetSubscriber = createSubscriber(session); // different from current

        request.setMethod("GET");
        request.setParameter("id", String.valueOf(targetSubscriber.getId()));

        testForListenServletException(servlet, 401, "Unauthorized - Insufficient permissions");
    }

    @Test
    public void test_doGet_withAdministratorUserAndDifferentTargetSubscriber_returnsSubscriberJson()
        throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);
        Subscriber targetSubscriber = createSubscriber(session);

        request.setParameter("id", String.valueOf(targetSubscriber.getId()));
        servlet.doGet(request, response);

        Marshaller marshaller = new JsonMarshaller();
        marshaller.registerConverterClass(Date.class, FriendlyIso8601DateConverter.class);
        String expected = GetSubscriberServlet.marshalSubscriberToJson(targetSubscriber, marshaller, session,
                                                                       ServletUtil.currentSubscriber(request));
        assertOutputBufferContentEquals(expected);
    }

    @Test
    public void test_doGet_withNonAdministratorUserAccesingTheirOwnSubscriber_returnsSubscriberJson()
        throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, false, session);
        Subscriber targetSubscriber = ServletUtil.currentSubscriber(request);
        request.setParameter("id", String.valueOf(targetSubscriber.getId()));
        servlet.doGet(request, response);

        Marshaller marshaller = new JsonMarshaller();
        marshaller.registerConverterClass(Date.class, FriendlyIso8601DateConverter.class);
        String expected = GetSubscriberServlet.marshalSubscriberToJson(targetSubscriber, marshaller, session,
                                                                       ServletUtil.currentSubscriber(request));
        assertOutputBufferContentEquals(expected);
    }

    @Test
    public void test_doGet_withNonAdministratorUserAndNoIdSpecified_returnsCurrentSubscriberJson()
        throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, false, session);
        Subscriber targetSubscriber = ServletUtil.currentSubscriber(request);
        request.setParameter("id", (String)null);
        servlet.doGet(request, response);

        Marshaller marshaller = new JsonMarshaller();
        marshaller.registerConverterClass(Date.class, FriendlyIso8601DateConverter.class);
        String expected = GetSubscriberServlet.marshalSubscriberToJson(targetSubscriber, marshaller, session,
                                                                       ServletUtil.currentSubscriber(request));
        assertOutputBufferContentEquals(expected);
    }
}
