package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;

import com.interact.listen.InputStreamMockHttpServletRequest;
import com.interact.listen.resource.User;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class IndexServletTest
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
    public void test_doGet_withNoSessionUser_redirectsToLogin() throws ServletException, IOException
    {
        assert request.getSession().getAttribute("user") == null;

        request.setMethod("GET");
        servlet.service(request, response);

        assertEquals("/login", response.getRedirectedUrl());
    }

    @Test
    public void test_doGet_withSessionUser_forwardsToIndexJsp() throws ServletException, IOException
    {
        request.getSession().setAttribute("user", new User());

        request.setMethod("GET");
        servlet.service(request, response);

        assertEquals("/WEB-INF/jsp/index.jsp", response.getForwardedUrl());
    }
}
