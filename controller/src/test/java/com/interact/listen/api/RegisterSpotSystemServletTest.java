package com.interact.listen.api;

import static org.junit.Assert.assertTrue;

import com.interact.listen.ListenServletTest;
import com.interact.listen.TestUtil;
import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RegisterSpotSystemServletTest extends ListenServletTest
{
    private RegisterSpotSystemServlet servlet = new RegisterSpotSystemServlet();
    private String originalConfiguration;

    @Before
    public void setUp()
    {
        originalConfiguration = Configuration.get(Property.Key.SPOT_SYSTEMS);
    }

    @After
    public void tearDown()
    {
        Configuration.set(Property.Key.SPOT_SYSTEMS, originalConfiguration);
    }

    @Test
    public void test_doPost_withMissingSystemParameter_throwsBadRequest() throws ServletException, IOException
    {
        request.setMethod("POST");
        request.setParameter("system", (String)null);
        testForListenServletException(servlet, 400, "Missing required parameter [system]");
    }

    @Test
    public void test_doPost_withSystemParameter_addsSystemToConfiguration() throws ServletException, IOException
    {
        String system = TestUtil.randomString();

        request.setMethod("POST");
        request.setParameter("system", system);
        servlet.service(request, response);

        assertTrue(Property.delimitedStringToSet(Configuration.get(Property.Key.SPOT_SYSTEMS), ",").contains(system));
    }

    @Test
    public void test_addSystem_addsSystemToConfiguration()
    {
        String system = TestUtil.randomString();
        RegisterSpotSystemServlet.addSystem(system);

        assertTrue(Property.delimitedStringToSet(Configuration.get(Property.Key.SPOT_SYSTEMS), ",").contains(system));
    }
}
