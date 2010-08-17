package com.interact.listen;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public abstract class ListenServletTest extends ListenTest
{
    protected MockHttpServletRequest request;
    protected MockHttpServletResponse response;

    @Before
    public void setUp()
    {
        request = new InputStreamMockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    protected void assertOutputBufferContentEquals(String expected)
    {
        assertEquals(expected, request.getAttribute(OutputBufferFilter.OUTPUT_BUFFER_KEY).toString());
    }

    protected void assertOutputBufferContentTypeEquals(String expectedContentType)
    {
        assertEquals(expectedContentType, request.getAttribute(OutputBufferFilter.OUTPUT_TYPE_KEY));
    }
}
