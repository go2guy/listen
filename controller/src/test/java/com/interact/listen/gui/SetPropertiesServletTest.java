package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;

import com.interact.listen.ListenServletTest;
import com.interact.listen.TestUtil;
import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

public class SetPropertiesServletTest extends ListenServletTest
{
    private SetPropertiesServlet servlet = new SetPropertiesServlet();

    @Test
    public void test_doPost_withNoSessionSubscriber_throwsListenServletExceptionWithUnauthorized()
        throws ServletException, IOException
    {
        assert request.getSession().getAttribute("subscriber") == null;

        request.setMethod("POST");
        testForListenServletException(servlet, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }

    @Test
    public void test_doPost_withNonAdministratorSessionSubscriber_throwsListenServletExceptionWithUnauthorized()
        throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, false, session);

        request.setMethod("POST");
        testForListenServletException(servlet, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }

    @Test
    public void test_doPost_withProperty_setsProperty() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);

        final String value = String.valueOf(System.currentTimeMillis());

        request.setMethod("POST");
        request.setParameter(Property.Key.MAIL_FROMADDRESS.getKey(), value);
        servlet.service(request, response);

        assertEquals(value, Configuration.get(Property.Key.MAIL_FROMADDRESS));
    }
}
