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

    @Test
    public void test_doPost_withUnrecognizedProperty_returns200() throws ServletException, IOException
    {
        // unrecognized properties are ignored, but 200 is returned
        TestUtil.setSessionSubscriber(request, true, session);

        String name = TestUtil.randomString();
        String value = TestUtil.randomString();

        request.setParameter(name, value);
        servlet.doPost(request, response);

        assertEquals(200, response.getStatus());
    }

    @Test
    public void test_doPost_withDnisMappingContainingEntryWithWildcardNotAtTheEnd_throwsBadRequest()
        throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);

        request.setParameter(Property.Key.DNIS_MAPPING.getKey(), "1*234:foo");
        request.setMethod("POST");

        testForListenServletException(servlet, 400, "Wildcard (*) may only be at the end of mapping 1*234");
    }
    
    @Test
    public void test_doPost_withDnisMappingContainingDuplicateEntry_throwsBadRequest() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);

        request.setParameter(Property.Key.DNIS_MAPPING.getKey(), "1234:foo;1234:bar");
        request.setMethod("POST");

        testForListenServletException(servlet, 400, "Mapping [1234] cannot be defined twice");
    }
    
    @Test
    public void test_doPost_withConferencingPinLengthLessThanThree_throwsBadRequest() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);
        
        request.setParameter(Property.Key.CONFERENCING_PINLENGTH.getKey(), "2");
        request.setMethod("POST");
        
        testForListenServletException(servlet, 400, "Conferencing PIN length [2] must be between 3 and 16 (inclusive)");
    }

    @Test
    public void test_doPost_withConferencingPinLengthMoreThan16_throwsBadRequest() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);
        
        request.setParameter(Property.Key.CONFERENCING_PINLENGTH.getKey(), "17");
        request.setMethod("POST");
        
        testForListenServletException(servlet, 400, "Conferencing PIN length [17] must be between 3 and 16 (inclusive)");
    }
    
    @Test
    public void test_doPost_withConferencingPinLengthNotANumber_throwsBadRequest() throws ServletException, IOException
    {
        TestUtil.setSessionSubscriber(request, true, session);
        
        request.setParameter(Property.Key.CONFERENCING_PINLENGTH.getKey(), "asdf");
        request.setMethod("POST");
        
        testForListenServletException(servlet, 400, "Conferencing PIN length [asdf] must be a number");
    }
}
