package com.interact.listen;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class OutputBufferFilter implements Filter
{
    public static final String OUTPUT_BUFFER_KEY = "OUTPUT_BUFFER";
    public static final String OUTPUT_TYPE_KEY = "OUTPUT_TYPE";

    /**
     * Whether or not the filter should suppress its output (sometimes needed for specialized servlets); {@code Boolean}
     * value
     */
    public static final String OUTPUT_SUPPRESS_KEY = "OUTPUT_SUPPRESS";

    /** Class logger */
    private static final Logger LOG = Logger.getLogger(OutputBufferFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
        throws ServletException, IOException
    {
        request.setAttribute(OUTPUT_BUFFER_KEY, new StringBuilder());
        request.setAttribute(OUTPUT_TYPE_KEY, "text/plain");
        request.setAttribute(OUTPUT_SUPPRESS_KEY, Boolean.FALSE);

        filterChain.doFilter(request, response);

        StringBuilder buffer = (StringBuilder)request.getAttribute(OUTPUT_BUFFER_KEY);
        String type = (String)request.getAttribute(OUTPUT_TYPE_KEY);

        if(!(Boolean)request.getAttribute(OUTPUT_SUPPRESS_KEY))
        {
            setResponseContent((HttpServletResponse)response, buffer.toString(), type);
        }
        else
        {
            LOG.debug("OutputBufferFilter is suppressing output");
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

    public static void clearBuffer(ServletRequest request)
    {
        request.setAttribute(OUTPUT_BUFFER_KEY, new StringBuilder());
        request.setAttribute(OUTPUT_TYPE_KEY, "text/plain");
    }

    public static void append(ServletRequest request, String content, String contentType)
    {
        append(request, content);
        setContentType(request, contentType);
    }

    public static void append(ServletRequest request, String content)
    {
        StringBuilder builder = (StringBuilder)request.getAttribute(OUTPUT_BUFFER_KEY);
        builder.append(content);
    }

    public static void setContentType(ServletRequest request, String contentType)
    {
        request.setAttribute(OUTPUT_TYPE_KEY, contentType);
    }

    private void setResponseContent(HttpServletResponse response, String content, String contentType)
    {
        if(content.length() > 0)
        {
            response.setHeader("Cache-Control", "no-cache");
            response.setContentLength(content.length());
            response.setContentType(contentType);
            LOG.debug("Writing response content [ " + content + " ], type = [" + contentType + "], length = [" +
                      content.length() + "]");

            try
            {
                PrintWriter writer = response.getWriter();
                writer.print(content);
            }
            catch(IOException e)
            {
                LOG.error(e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentLength(0);
            }
        }
    }
}
