package com.interact.listen.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.interact.listen.*;
import com.interact.listen.exception.UnauthorizedServletException;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Test;

public class PingServletTest extends ListenServletTest
{
    private PingServlet servlet = new PingServlet();

    @Test
    public void test_doGet_withNoAuthParameter_returnsPong() throws ServletException, IOException
    {
        performRequest(null);
        assertOutputBufferContentEquals("pong");
    }

    @Test
    public void test_doGet_withAuthParameterFalse_returnsPong() throws ServletException, IOException
    {
        performRequest("false");
        assertOutputBufferContentEquals("pong");
    }

    @Test
    public void test_doGet_withAuthParameterTrueAndNoCurrentUser_throwsException() throws ServletException, IOException
    {
        assert ServletUtil.currentSubscriber(request) == null;

        try
        {
            performRequest("true");
            fail("Expected UnauthorizedServletException");
        }
        catch(UnauthorizedServletException e)
        {
            assertEquals("Unauthorized - Not logged in", e.getContent());
        }
    }

    @Test
    public void test_doGet_withAuthParameterTrueAndCurrentUserWithNoVoicemails_returnsPong() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, false, session);
        performRequest("true");
        assertOutputBufferContentEquals("{\"pong\":true,\"newVoicemailCount\":\"0\"}");
    }

    private void performRequest(String authParameter) throws ServletException, IOException
    {
        request.setParameter("auth", authParameter);
        request.setMethod("GET");
        servlet.service(request, response);
    }
}
