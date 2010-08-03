package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;

import com.interact.listen.InputStreamMockHttpServletRequest;
import com.interact.listen.ListenTest;
import com.interact.listen.TestUtil;
import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class EditPagerServletTest extends ListenTest
{
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private EditPagerServlet servlet;

    @Before
    public void setUp()
    {
        request = new InputStreamMockHttpServletRequest();
        response = new MockHttpServletResponse();
        servlet = new EditPagerServlet();
    }

    @Test
    public void test_doPost_withAlternateNumber_updatesAlternateNumber() throws ServletException, IOException
    {
        final String alternatePagerNumber = Configuration.get(Property.Key.ALTERNATE_NUMBER);

        TestUtil.setSessionSubscriber(request, false, session);

        try
        {
            request.setMethod("POST");
            request.setParameter("alternateNumber", "123456789");
            request.setParameter("alternateAddress", "example.com");

            servlet.service(request, response);
            assertEquals(Configuration.get(Property.Key.ALTERNATE_NUMBER), "123456789@example.com");
        }
        finally
        {
            Configuration.set(Property.Key.ALTERNATE_NUMBER, alternatePagerNumber);
        }
    }

    @Test
    public void test_doPost_blankAlternateNumber_updatesAlternateNumber() throws ServletException, IOException
    {
        final String alternatePagerNumber = Configuration.get(Property.Key.ALTERNATE_NUMBER);

        TestUtil.setSessionSubscriber(request, false, session);

        try
        {
            request.setMethod("POST");
            request.setParameter("alternateNumber", "");
            request.setParameter("alternateAddress", "example.com");

            servlet.service(request, response);
            assertEquals(Configuration.get(Property.Key.ALTERNATE_NUMBER), "");
        }
        finally
        {
            Configuration.set(Property.Key.ALTERNATE_NUMBER, alternatePagerNumber);
        }
    }
}
