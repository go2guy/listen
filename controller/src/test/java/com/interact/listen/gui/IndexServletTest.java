package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;

import com.interact.listen.InputStreamMockHttpServletRequest;
import com.interact.listen.ListenTest;
import com.interact.listen.resource.Subscriber;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class IndexServletTest extends ListenTest
{
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private IndexServlet servlet = new IndexServlet();;

    @Before
    public void setUp()
    {
        request = new InputStreamMockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    public void test_doGet_withNoSessionSubscriber_redirectsToLogin() throws ServletException, IOException
    {
        assert request.getSession().getAttribute("subscriber") == null;

        request.setMethod("GET");
        servlet.service(request, response);

        assertEquals("/login", response.getRedirectedUrl());
    }

    @Test
    public void test_doGet_withSessionSubscriber_forwardsToIndexJsp() throws ServletException, IOException
    {
        request.getSession().setAttribute("subscriber", new Subscriber());

        request.setMethod("GET");
        servlet.service(request, response);

        assertEquals("/WEB-INF/jsp/index.jsp", response.getForwardedUrl());
    }
}
