package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;

import com.interact.listen.ListenServletTest;
import com.interact.listen.resource.Subscriber;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Test;

public class IndexServletTest extends ListenServletTest
{
    private IndexServlet servlet = new IndexServlet();;

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
