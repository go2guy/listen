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

public class OutdialServletTest extends ListenServletTest
{
    private OutdialServlet servlet = new OutdialServlet();

    @Before
    public void setUp()
    {
        License.setLicense(new AlwaysTrueMockLicense());
    }

    @Test
    public void test_doPost_withNoSessionSubscriber_throwsListenServletExceptionWithUnauthorized()
        throws ServletException, IOException
    {
        assert request.getSession().getAttribute("subscriber") == null;

        request.setMethod("POST");
        request.setParameter("conferenceId", TestUtil.randomString());
        request.setParameter("number", TestUtil.randomString() + "foo");

        testForListenServletException(servlet, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }

    @Test
    public void test_doPost_withNullConferenceId_throwsListenServletExceptionWithBadRequest() throws ServletException,
        IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);

        request.setMethod("POST");
        request.setParameter("conferenceId", (String)null);
        request.setParameter("number", TestUtil.randomString() + "foo");

        testForListenServletException(servlet, HttpServletResponse.SC_BAD_REQUEST, "Please provide a conferenceId");
    }

    @Test
    public void test_doPost_withBlankConferenceId_throwsListenServletExceptionWithBadRequest() throws ServletException,
        IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);

        request.setMethod("POST");
        request.setParameter("conferenceId", " ");
        request.setParameter("number", TestUtil.randomString() + "foo");

        testForListenServletException(servlet, HttpServletResponse.SC_BAD_REQUEST, "Please provide a conferenceId");
    }

    @Test
    public void test_doPost_withNullNumber_throwsListenServletExceptionWithBadRequest() throws ServletException,
        IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);

        request.setMethod("POST");
        request.setParameter("conferenceId", TestUtil.randomString());
        request.setParameter("number", (String)null);

        testForListenServletException(servlet, HttpServletResponse.SC_BAD_REQUEST, "Please provide a number");
    }

    @Test
    public void test_doPost_withBlankNumber_throwsListenServletExceptionWithBadRequest() throws ServletException,
        IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);

        request.setMethod("POST");
        request.setParameter("conferenceId", TestUtil.randomString());
        request.setParameter("number", " ");

        testForListenServletException(servlet, HttpServletResponse.SC_BAD_REQUEST, "Please provide a number");
    }

    @Test
    public void test_doPost_withConferenceNotFound_throwsListenServletExceptionWithBadRequest()
        throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);

        request.setMethod("POST");
        request.setParameter("conferenceId", String.valueOf(TestUtil.randomNumeric(9))); // hopefully doesn't exist
        request.setParameter("number", TestUtil.randomString() + "foo");
        request.setParameter("interrupt", "false");

        testForListenServletException(servlet, HttpServletResponse.SC_BAD_REQUEST, "Conference not found");
    }
}
