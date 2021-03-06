package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.interact.listen.ListenServletTest;
import com.interact.listen.TestUtil;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.security.SecurityUtil;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Test;

// TODO this test class has quite a bit of duplication, and is a good target for some refactoring
public class LoginServletTest extends ListenServletTest
{
    private LoginServlet servlet = new LoginServlet();

    @Test
    public void test_doPost_validCredentials_returns200() throws IOException, ServletException
    {
        final String username = String.valueOf(System.currentTimeMillis());
        final String password = "bar";

        Subscriber subscriber = new Subscriber();
        subscriber.setPassword(SecurityUtil.hashPassword(password));
        subscriber.setUsername(username);
        subscriber.setVoicemailPin(TestUtil.randomNumeric(4).toString());

        session.save(subscriber);

        request.setMethod("POST");
        request.setParameter("username", username);
        request.setParameter("password", password);
        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }

    @Test
    public void test_doPost_validSubscriberButWrongPassword_populatesSessionErrorMapAndRedirectsToGet()
        throws IOException, ServletException
    {
        final String username = String.valueOf(System.currentTimeMillis());
        final String password = String.valueOf(System.currentTimeMillis());

        Subscriber subscriber = new Subscriber();
        subscriber.setPassword(SecurityUtil.hashPassword(password));
        subscriber.setUsername(username);
        subscriber.setVoicemailPin(TestUtil.randomNumeric(4).toString());

        session.save(subscriber);

        request.setMethod("POST");
        request.setParameter("username", username);
        request.setParameter("password", "ninja");
        servlet.service(request, response);

        HttpSession httpSession = request.getSession();
        Map<String, String> errors = (Map<String, String>)httpSession.getAttribute("errors");

        assertTrue(errors.containsKey("username"));
        assertEquals("Sorry, those aren't valid credentials", errors.get("username"));
        assertEquals("/login", response.getRedirectedUrl());
    }

    @Test
    public void test_doPost_subscriberNotFound_populatesSessionErrorMapAndRedirectsToGet() throws IOException,
        ServletException
    {
        request.setMethod("POST");
        request.setParameter("username", String.valueOf(System.currentTimeMillis()));
        request.setParameter("password", String.valueOf(System.currentTimeMillis()));

        servlet.service(request, response);

        HttpSession httpSession = request.getSession();
        Map<String, String> errors = (Map<String, String>)httpSession.getAttribute("errors");

        assertTrue(errors.containsKey("username"));
        assertEquals("Sorry, those aren't valid credentials", errors.get("username"));
        assertEquals("/login", response.getRedirectedUrl());
    }

    @Test
    public void test_doPost_withNullUsername_populatesSessionErrorMapAndForwardsToGet() throws IOException,
        ServletException
    {
        request.setAttribute("username", null);
        request.setAttribute("password", String.valueOf(System.currentTimeMillis()));

        request.setMethod("POST");
        servlet.service(request, response);

        HttpSession httpSession = request.getSession();
        Map<String, String> errors = (Map<String, String>)httpSession.getAttribute("errors");

        assertTrue(errors.containsKey("username"));
        assertEquals("Please provide a username", errors.get("username"));
        assertEquals("/login", response.getRedirectedUrl());
    }

    @Test
    public void test_doPost_withBlankUsername_populatesSessionErrorMapAndForwardsToGet() throws IOException,
        ServletException
    {
        request.setAttribute("username", " ");
        request.setAttribute("password", String.valueOf(System.currentTimeMillis()));

        request.setMethod("POST");
        servlet.service(request, response);

        HttpSession httpSession = request.getSession();
        Map<String, String> errors = (Map<String, String>)httpSession.getAttribute("errors");

        assertTrue(errors.containsKey("username"));
        assertEquals("Please provide a username", errors.get("username"));
        assertEquals("/login", response.getRedirectedUrl());
    }

    @Test
    public void test_doPost_withNullPassword_populatesSessionErrorMapAndForwardsToGet() throws IOException,
        ServletException
    {
        request.setAttribute("username", String.valueOf(System.currentTimeMillis()));
        request.setAttribute("password", null);

        request.setMethod("POST");
        servlet.service(request, response);

        HttpSession httpSession = request.getSession();
        Map<String, String> errors = (Map<String, String>)httpSession.getAttribute("errors");

        assertTrue(errors.containsKey("password"));
        assertEquals("Please provide a password", errors.get("password"));
        assertEquals("/login", response.getRedirectedUrl());
    }

    @Test
    public void test_doPost_withBlankPassword_populatesSessionErrorMapAndForwardsToGet() throws IOException,
        ServletException
    {
        request.setAttribute("username", String.valueOf(System.currentTimeMillis()));
        request.setAttribute("password", " ");

        request.setMethod("POST");
        servlet.service(request, response);

        HttpSession httpSession = request.getSession();
        Map<String, String> errors = (Map<String, String>)httpSession.getAttribute("errors");

        assertTrue(errors.containsKey("password"));
        assertEquals("Please provide a password", errors.get("password"));
        assertEquals("/login", response.getRedirectedUrl());
    }

    @Test
    public void test_doPost_sendsStat() throws IOException, ServletException
    {
        StatSender statSender = mock(StatSender.class);
        request.getSession().getServletContext().setAttribute("statSender", statSender);

        request.setMethod("POST");
        servlet.service(request, response);

        verify(statSender).send(Stat.GUI_LOGIN);
    }

    @Test
    public void test_doGet_withPopulatedSessionErrors_transfersSessionErrorsToRequestAndClearsSessionErrors()
        throws IOException, ServletException
    {
        final Map<String, String> errors = new HashMap<String, String>();

        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("errors", errors);

        request.setMethod("GET");
        servlet.service(request, response);

        assertNull(httpSession.getAttribute("errors"));
        assertEquals(errors, request.getAttribute("errors"));
    }

    @Test
    public void test_doGet_withNoSessionErrors_doesntThrowExceptionWhenTryingToTransferErrors()
        throws ServletException, IOException
    {
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("errors", null);

        request.setMethod("GET");
        servlet.service(request, response);

        assertNull(httpSession.getAttribute("errors"));
        assertNull(request.getAttribute("errors"));
    }

    @Test
    public void test_doGet_forwardsToLoginJsp() throws ServletException, IOException
    {
        request.setMethod("GET");
        servlet.service(request, response);

        assertEquals("/WEB-INF/jsp/login.jsp", response.getForwardedUrl());
    }
}
