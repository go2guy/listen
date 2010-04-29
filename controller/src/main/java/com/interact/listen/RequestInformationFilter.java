package com.interact.listen;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

public class RequestInformationFilter implements Filter
{
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

        System.out.println("--> [" + info + "]");

        try
        {
            chain.doFilter(request, response);
        }
        finally
        {
            System.out.println("<-- [" + info + "] took " + (System.currentTimeMillis() - start) + "ms");
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
