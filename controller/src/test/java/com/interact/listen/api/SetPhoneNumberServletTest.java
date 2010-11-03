package com.interact.listen.api;

import static org.junit.Assert.assertEquals;

import com.interact.listen.ListenServletTest;
import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Test;

public class SetPhoneNumberServletTest extends ListenServletTest
{
    private SetPhoneNumberServlet servlet = new SetPhoneNumberServlet();

    @Test
    public void test_doPost_withMissingPhoneNumber_throwsBadRequest() throws ServletException, IOException
    {
        request.setParameter("phoneNumber", (String)null);
        request.setMethod("POST");
        testForListenServletException(servlet, 400, "Missing required parameter [phoneNumber]");
    }

    @Test
    public void test_doPost_withPhoneNumberMissingDelimiter_throwsBadRequest() throws ServletException, IOException
    {
        request.setParameter("phoneNumber", "this string does not have a delimiter");
        request.setMethod("POST");
        testForListenServletException(servlet, 400,
                                      "Property [phoneNumber] must be in the format 'PROTOCOL;NUMBER', where PROTOCOL = [PSTN|VOIP]");
    }

    @Test
    public void test_doPost_withPhoneNumberHavingMultipleDelimiters_throwsBadRequest() throws ServletException,
        IOException
    {
        request.setParameter("phoneNumber", "this string; has multiple; delimiters");
        request.setMethod("POST");
        testForListenServletException(servlet, 400,
                                      "Property [phoneNumber] must be in the format 'PROTOCOL;NUMBER', where PROTOCOL = [PSTN|VOIP]");
    }

    @Test
    public void test_doPost_withPhoneNumberHavingUnrecognizedProtocol_throwsBadRequest() throws ServletException,
        IOException
    {
        request.setParameter("phoneNumber", "RANDOMPROTOCOL;1800AWESOME");
        request.setMethod("POST");
        testForListenServletException(servlet, 400,
                                      "Property [phoneNumber] must be in the format 'PROTOCOL;NUMBER', where PROTOCOL = [PSTN|VOIP]");
    }

    @Test
    public void test_doPost_withValidPSTNPhoneNumberString_setsConfigurationPhoneNumber() throws ServletException,
        IOException
    {
        request.setParameter("phoneNumber", "PSTN;1800AWESOME");
        request.setMethod("POST");

        servlet.service(request, response);

        String config = Configuration.get(Property.Key.PHONE_NUMBER);
        assertEquals("PSTN;1800AWESOME", config);
    }

    @Test
    public void test_doPost_withValidVOIPPhoneNumberString_setsConfigurationPhoneNumber() throws ServletException,
        IOException
    {
        request.setParameter("phoneNumber", "VOIP;127.0.0.1:333");
        request.setMethod("POST");

        servlet.service(request, response);

        String config = Configuration.get(Property.Key.PHONE_NUMBER);
        assertEquals("VOIP;127.0.0.1:333", config);
    }
}
