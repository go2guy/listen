package com.interact.listen.gui;

import static org.junit.Assert.assertEquals;

import com.interact.listen.ListenServletTest;
import com.interact.listen.TestUtil;
import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EditPagerServletTest extends ListenServletTest
{
    private EditPagerServlet servlet = new EditPagerServlet();

    private String originalAlternateNumber;

    @Before
    public void setUp()
    {
        originalAlternateNumber = Configuration.get(Property.Key.ALTERNATE_NUMBER);
    }

    @After
    public void tearDown()
    {
        Configuration.set(Property.Key.ALTERNATE_NUMBER, originalAlternateNumber);
    }

    @Test
    public void test_doPost_withAlternateNumber_updatesAlternateNumber() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, false, session);
        request.setMethod("POST");
        request.setParameter("alternateNumber", "123456789");
        request.setParameter("alternateAddress", "example.com");

        servlet.service(request, response);
        assertEquals(Configuration.get(Property.Key.ALTERNATE_NUMBER), "123456789@example.com");
    }

    @Test
    public void test_doPost_blankAlternateNumber_updatesAlternateNumber() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, false, session);
        request.setMethod("POST");
        request.setParameter("alternateNumber", "");
        request.setParameter("alternateAddress", "example.com");

        servlet.service(request, response);
        assertEquals(Configuration.get(Property.Key.ALTERNATE_NUMBER), "");
    }
}
