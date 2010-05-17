package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.interact.listen.HibernateUtil;
import com.interact.listen.InputStreamMockHttpServletRequest;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.User;
import com.interact.listen.security.SecurityUtil;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class LoginServletTest
{
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private LoginServlet servlet = new LoginServlet();

    @Before
    public void setUp()
    {
        request = new InputStreamMockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    public void test_doPost_validCredentials_returns200() throws IOException, ServletException
    {
        final Long userId = System.currentTimeMillis();
        final String username = String.valueOf(System.currentTimeMillis());
        final String password = "bar";

        Subscriber subscriber = new Subscriber();
        subscriber.setNumber(String.valueOf(userId));
        User user = new User();
        user.setUsername(username);
        user.setPassword(SecurityUtil.hashPassword(password));
        user.setSubscriber(subscriber);

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        session.save(subscriber);
        session.save(user);

        request.setMethod("POST");
        request.setParameter("username", username);
        request.setParameter("password", password);
        servlet.service(request, response);

        tx.commit();

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }

    @Test
    public void test_doPost_validUserButWrongPassword_populatesSessionErrorMapAndRedirectsToGet() throws IOException,
        ServletException
    {
        final Long userId = System.currentTimeMillis();
        final String username = String.valueOf(System.currentTimeMillis());
        final String password = String.valueOf(System.currentTimeMillis());

        Subscriber subscriber = new Subscriber();
        subscriber.setNumber(String.valueOf(userId));
        User user = new User();
        user.setUsername(username);
        user.setPassword(SecurityUtil.hashPassword(password));
        user.setSubscriber(subscriber);

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        session.save(subscriber);
        session.save(user);

        request.setMethod("POST");
        request.setParameter("username", username);
        request.setParameter("password", "ninja");
        servlet.service(request, response);

        tx.commit();

        HttpSession httpSession = request.getSession();
        Map<String, String> errors = (Map<String, String>)httpSession.getAttribute("errors");

        assertTrue(errors.containsKey("username"));
        assertEquals("Sorry, those aren't valid credentials", errors.get("username"));
        assertEquals("/login", response.getRedirectedUrl());
    }

    @Test
    public void test_doPost_userNotFound_populatesSessionErrorMapAndRedirectsToGet() throws IOException,
        ServletException
    {
        request.setMethod("POST");
        request.setParameter("username", String.valueOf(System.currentTimeMillis()));
        request.setParameter("password", String.valueOf(System.currentTimeMillis()));

        Session hibernateSession = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = hibernateSession.beginTransaction();
        servlet.service(request, response);
        tx.commit();

        HttpSession session = request.getSession();
        Map<String, String> errors = (Map<String, String>)session.getAttribute("errors");

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

        HttpSession session = request.getSession();
        Map<String, String> errors = (Map<String, String>)session.getAttribute("errors");

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

        HttpSession session = request.getSession();
        Map<String, String> errors = (Map<String, String>)session.getAttribute("errors");

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

        HttpSession session = request.getSession();
        Map<String, String> errors = (Map<String, String>)session.getAttribute("errors");

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

        HttpSession session = request.getSession();
        Map<String, String> errors = (Map<String, String>)session.getAttribute("errors");

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

        HttpSession session = request.getSession();
        session.setAttribute("errors", errors);

        request.setMethod("GET");
        servlet.service(request, response);

        assertNull(session.getAttribute("errors"));
        assertEquals(errors, request.getAttribute("errors"));
    }

    @Test
    public void test_doGet_withNoSessionErrors_doesntThrowExceptionWhenTryingToTransferErrors()
        throws ServletException, IOException
    {
        HttpSession session = request.getSession();
        session.setAttribute("errors", null);

        request.setMethod("GET");
        servlet.service(request, response);

        assertNull(session.getAttribute("errors"));
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
