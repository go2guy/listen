package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.interact.listen.InputStreamMockHttpServletRequest;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.User;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class GetConferenceListServletTest
{
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private GetConferenceListServlet servlet = new GetConferenceListServlet();

    @Before
    public void setUp()
    {
        request = new InputStreamMockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    public void test_doGet_withNoSessionUser_returnsUnauthorized() throws IOException, ServletException
    {
        request.setMethod("GET");
        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("Unauthorized - not logged in", response.getContentAsString());
    }

    @Test
    public void test_doGet_returnsListWithNecessaryFields() throws IOException, ServletException
    {
        setSessionUser(request);

        request.setMethod("GET");
        servlet.service(request, response);

        assertTrue(response.getContentAsString().contains("_fields=description,id,isStarted"));
    }

    // TODO test with administrator user
    // TODO test with non-administrator user

    private void setSessionUser(HttpServletRequest request)
    {
        Subscriber subscriber = new Subscriber();
        subscriber.setNumber(String.valueOf(System.currentTimeMillis()));
        User user = new User();
        user.setSubscriber(subscriber);

        HttpSession session = request.getSession();
        session.setAttribute("user", user);
    }
}
