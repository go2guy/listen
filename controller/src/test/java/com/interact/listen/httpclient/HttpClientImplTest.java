package com.interact.listen.httpclient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

public class HttpClientImplTest
{
    private HttpClient httpClient;

    @Before
    public void setUp()
    {
        httpClient = new HttpClientImpl();
    }

    @Test
    public void test_getResponseStatus_invokedBeforeSendingRequest_throwsIllegalStateExceptionWithMessage()
    {
        try
        {
            httpClient.getResponseStatus();
            fail("Expected IllegalStateException from getResponseStatus when invoked before sending request");
        }
        catch(IllegalStateException e)
        {
            assertEquals("You must make a request before getting the response status", e.getMessage());
        }
    }

    @Test
    public void test_getResponseEntity_invokedBeforeSendingRequest_throwsIllegalStateExceptionWithMessage()
    {
        try
        {
            httpClient.getResponseEntity();
            fail("Expected IllegalStateException from getResponseEntity when invoked before sending request");
        }
        catch(IllegalStateException e)
        {
            assertEquals("You must make a request before getting the response entity", e.getMessage());
        }
    }

    @Test
    public void test_encode_usesSpaceInsteadOfPlus()
    {
        String encoded = HttpClientImpl.encode("This Is A Test");
        assertEquals("This%20Is%20A%20Test", encoded);
    }
}
