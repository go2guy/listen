package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.interact.listen.HibernateUtil;
import com.interact.listen.InputStreamMockHttpServletRequest;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.User;
import com.interact.listen.security.SecurityUtils;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

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
    public void test_doPost_missingUsername_returns400WithMessage() throws IOException, ServletException
    {
        request.setMethod("POST");
        request.setParameter("username", (String)null);
        request.setParameter("password", "foo");
        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        assertEquals("Please provide a username", response.getContentAsString());
    }

    @Test
    public void test_doPost_blankUsername_returns400WithMessage() throws IOException, ServletException
    {
        request.setMethod("POST");
        request.setParameter("username", "  ");
        request.setParameter("password", "foo");
        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        assertEquals("Please provide a username", response.getContentAsString());
    }

    @Test
    public void test_doPost_missingPassword_returns400WithMessage() throws IOException, ServletException
    {
        request.setMethod("POST");
        request.setParameter("username", "foo");
        request.setParameter("password", (String)null);
        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        assertEquals("Please provide a password", response.getContentAsString());
    }

    @Test
    public void test_doPost_userNotFound_returns401WithMessage() throws IOException, ServletException
    {
        request.setMethod("POST");
        request.setParameter("username", "foo");
        request.setParameter("password", "bar");
        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("Sorry, that's not a valid username/password", response.getContentAsString());
    }

    @Test
    public void test_doPost_userFoundButInvalidPassword_returns401WithMessage() throws IOException, ServletException
    {
        final Long userId = System.currentTimeMillis();
        final String username = "foo";
        final String password = "bar";

        Subscriber subscriber = new Subscriber();
        subscriber.setNumber(String.valueOf(userId));
        User user = new User();
        user.setUsername(username);
        user.setPassword(SecurityUtils.hashPassword(password));
        user.setSubscriber(subscriber);

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        session.save(subscriber);
        session.save(user);
        tx.commit();

        request.setMethod("POST");
        request.setParameter("username", username);
        request.setParameter("password", password + "invalid");
        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("Sorry, that's not a valid username/password", response.getContentAsString());
    }

    @Test
    public void test_doPost_validCredentials_returns200() throws IOException, ServletException
    {
        final Long userId = System.currentTimeMillis();
        final String username = "foo";
        final String password = "bar";

        Subscriber subscriber = new Subscriber();
        subscriber.setNumber(String.valueOf(userId));
        User user = new User();
        user.setUsername(username);
        user.setPassword(SecurityUtils.hashPassword(password));
        user.setSubscriber(subscriber);

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tx = session.beginTransaction();
        session.save(subscriber);
        session.save(user);
        tx.commit();

        request.setMethod("POST");
        request.setParameter("username", username);
        request.setParameter("password", password);
        servlet.service(request, response);

        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
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
}
