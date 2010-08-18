package com.interact.listen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.interact.listen.exception.ListenServletException;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.junit.Before;
import org.springframework.mock.web.MockHttpServletResponse;

public abstract class ListenServletTest extends ListenTest
{
    protected InputStreamMockHttpServletRequest request;
    protected MockHttpServletResponse response;

    @Before
    public void setUpServlet()
    {
        request = new InputStreamMockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    protected void assertOutputBufferContentEquals(String expected)
    {
        assertEquals(expected, request.getOutputBufferString());
    }

    protected void assertOutputBufferContentTypeEquals(String expectedContentType)
    {
        assertEquals(expectedContentType, request.getOutputBufferType());
    }

    protected void testForListenServletException(HttpServlet servlet, int expectedStatus, String expectedContent)
        throws IOException, ServletException
    {
        try
        {
            servlet.service(request, response);
            fail("Expected ListenServletException");
        }
        catch(ListenServletException e)
        {
            assertEquals(expectedStatus, e.getStatus());
            assertEquals(expectedContent, e.getContent());
            assertEquals("text/plain", e.getContentType());
        }
    }
}
