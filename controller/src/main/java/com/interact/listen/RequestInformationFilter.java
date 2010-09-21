package com.interact.listen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Extracts information (e.g. remote IP address, requested resource) from the {@code ServletRequest} and writes a log.
 * Also times the request and writes the elapsed time in a log.
 */
public class RequestInformationFilter implements Filter
{
    private static final Logger LOG = Logger.getLogger(RequestInformationFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException,
        IOException
    {
        long start = System.currentTimeMillis();

        HttpServletRequest req = (HttpServletRequest)request;

        StringBuilder info = new StringBuilder();
        info.append("[");
        info.append(req.getRemoteAddr()).append(" ");
        info.append(req.getMethod()).append(" ");
        info.append(req.getRequestURI());
        if(req.getQueryString() != null && !req.getQueryString().trim().equals(""))
        {
            info.append("?").append(req.getQueryString());
        }
        info.append("]");

        String headers = getHeaders((HttpServletRequest)request);
        info.append(" [").append(headers ).append("]");

        LOG.info("--> " + info);

        try
        {
            chain.doFilter(request, response);
        }
        finally
        {
            LOG.info("<-- " + info + " took " + (System.currentTimeMillis() - start) + "ms");
        }
    }

    @Override
    public void init(FilterConfig config)
    {
    // not implemented
    }

    @Override
    public void destroy()
    {
    // not implemented
    }

    private String getHeaders(HttpServletRequest request)
    {
        Enumeration<String> names = ((HttpServletRequest)request).getHeaderNames();
        List<String> headers = new ArrayList<String>();
        while(names.hasMoreElements())
        {
            String name = names.nextElement();
            String value = ((HttpServletRequest)request).getHeader(name);
            headers.add(name + ": " + value);
        }
        return StringUtils.join(headers, ", ");
    }
}
