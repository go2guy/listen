package com.interact.listen;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Provides top-level {@code Exception} handling for servlet requests/responses. This allows for consistent responses
 * when the application encounters an error and eliminates the need for the {@code Servlet}s themselves to control the
 * response content when errors occur.
 */
public class ExceptionHandlerFilter implements Filter
{
    /** Class logger */
    private static final Logger LOG = Logger.getLogger(ExceptionHandlerFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
    {
        HttpServletResponse resp = (HttpServletResponse)response;

        try
        {
            filterChain.doFilter(request, response);
        }
        catch(Throwable t) // SUPPRESS CHECKSTYLE IllegalCatchCheck
        {
            LOG.error("Handling throwable", t);
            if(t.getCause() instanceof ListenServletException)
            {
                ListenServletException e = (ListenServletException)t.getCause();
                resp.setStatus(e.getStatus());
                OutputBufferFilter.append(request, e.getContent(), e.getContentType());
            }
            else
            {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                OutputBufferFilter.clearBuffer(request);
                OutputBufferFilter.append(request,
                                          "An unknown error occurred, please contact the system administrator",
                                          "text/plain");
            }
        }
    }

    @Override
    public void init(FilterConfig filterConfig)
    {
        // no implementation
    }

    @Override
    public void destroy()
    {
        // no implementation
    }
}
