package com.interact.listen;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

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
        info.append(req.getRemoteAddr()).append(" ");
        info.append(req.getMethod()).append(" ");
        info.append(req.getRequestURI());
        if(req.getQueryString() != null && !req.getQueryString().trim().equals(""))
        {
            info.append("?").append(req.getQueryString());
        }

        LOG.info("--> [" + info + "]");

        try
        {
            chain.doFilter(request, response);
        }
        finally
        {
            LOG.info("<-- [" + info + "] took " + (System.currentTimeMillis() - start) + "ms");
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
}
