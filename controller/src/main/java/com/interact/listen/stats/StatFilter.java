package com.interact.listen.stats;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

/**
 * Writes stats-collector stats for various HTTP requests.
 */
public class StatFilter implements Filter
{
    private StatSender statSender = new InsaStatSender();

    public void setStatSender(StatSender statSender)
    {
        this.statSender = statSender;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException,
        IOException
    {
        String resource = getResource((HttpServletRequest)request);
        String method = ((HttpServletRequest)request).getMethod();

        if(resource != null)
        {
            String enumValue = "API_" + resource.toUpperCase() + "_" + method;

            try
            {
                Stat stat = Stat.valueOf(enumValue);
                statSender.send(stat);
            }
            catch(IllegalArgumentException e)
            {
                System.out.println("Cannot write stat for [" + enumValue + "]");
            }
        }

        chain.doFilter(request, response);
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

    private String getResource(HttpServletRequest request)
    {
        String pathInfo = request.getPathInfo();
        if(pathInfo == null || pathInfo.length() <= 1)
        {
            return null;
        }

        if(pathInfo.startsWith("/"))
        {
            pathInfo = pathInfo.substring(1);
        }

        String collection = pathInfo.split("/")[0];
        return collection.substring(0, collection.length() - 1);
    }
}
