package com.interact.listen.gui;

import com.interact.listen.ListenServletTest;
import com.interact.listen.TestUtil;
import com.interact.listen.license.AlwaysTrueMockLicense;
import com.interact.listen.license.License;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;

public class TestNotificationSettingsServletTest extends ListenServletTest
{
    private TestNotificationSettingsServlet servlet = new TestNotificationSettingsServlet();

    @Before
    public void setUp()
    {
        License.setLicense(new AlwaysTrueMockLicense());
    }

    @Test
    public void test_doPost_blankMessageType_returnsError() throws IOException, ServletException
    {
        testExceptionCausingRequest("", "foo@example.com", "Please provide a message type");
    }

    @Test
    public void test_doPost_nullMessageType_returnsError() throws IOException, ServletException
    {
        testExceptionCausingRequest(null, "foo@example.com", "Please provide a message type");
    }

    @Test
    public void test_doPost_blankAddress_returnsError() throws IOException, ServletException
    {
        testExceptionCausingRequest(TestUtil.randomString(), "", "Please provide an address");
    }

    @Test
    public void test_doPost_nullAddress_returnsError() throws IOException, ServletException
    {
        testExceptionCausingRequest(TestUtil.randomString(), null, "Please provide an address");
    }

    private void testExceptionCausingRequest(String messageType, String address, String expectedContent)
        throws IOException, ServletException
    {
        TestUtil.setSessionSubscriber(request, true, session); // admin subscriber

        request.setMethod("POST");
        request.setParameter("messageType", messageType);
        request.setParameter("address", address);

        testForListenServletException(servlet, HttpServletResponse.SC_BAD_REQUEST, expectedContent);
    }
}
