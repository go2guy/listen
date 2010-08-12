package com.interact.listen;

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
}
