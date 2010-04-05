package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.interact.listen.InputStreamMockHttpServletRequest;
import com.interact.listen.resource.User;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class LogoutServletTest
{
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private HttpServlet servlet = new LogoutServlet();

    @Before
    public void setUp()
    {
        request = new InputStreamMockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    public void test_doPost_removesUserFromSessionAndReturns200() throws IOException, ServletException
    {
        final String sessionUserKey = "user";

        // put a user in the session first
        User user = new User();
        HttpSession session = request.getSession();
        session.setAttribute(sessionUserKey, user);

        request.setMethod("POST");
        servlet.service(request, response);

        assertNull(session.getAttribute(sessionUserKey));
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }
}
