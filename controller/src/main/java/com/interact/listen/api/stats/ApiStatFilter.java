package com.interact.listen.api.stats;

import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * Writes stat-collector statistics for various HTTP requests.
 */
public class ApiStatFilter implements Filter
{
    private static final Logger LOG = Logger.getLogger(ApiStatFilter.class);

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
                LOG.error("Cannot write stat for [" + enumValue + "]", e);
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

    // TODO this code is similar to ApiServlet#getResourceAttributes() - perhaps they can be combined?
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
