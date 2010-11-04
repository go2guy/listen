package com.interact.listen.gui;

import com.interact.listen.ListenServletTest;
import com.interact.listen.ServletUtil;
import com.interact.listen.license.AlwaysTrueMockLicense;
import com.interact.listen.license.License;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;

public class GetConferenceListServletTest extends ListenServletTest
{
    private GetConferenceListServlet servlet = new GetConferenceListServlet();

    @Before
    public void setUp()
    {
        License.setLicense(new AlwaysTrueMockLicense());
    }

    @Test
    public void test_doGet_withNoSessionSubscriber_throwsListenServletExceptionWithUnauthorized() throws IOException,
        ServletException
    {
        assert ServletUtil.currentSubscriber(request) == null;

        request.setMethod("GET");
        testForListenServletException(servlet, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized - Not logged in");
    }

    // TODO test with administrator subscriber
    // TODO test with non-administrator subscriber
}
