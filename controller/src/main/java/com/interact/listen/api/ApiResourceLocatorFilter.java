package com.interact.listen.api;

import com.interact.listen.resource.Resource;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

public class ApiResourceLocatorFilter implements Filter
{
    public static final String RESOURCE_CLASS_KEY = "RESOURCE_CLASS";
    public static final String RESOURCE_ID_KEY = "RESOURCE_ID";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
        throws ServletException, IOException
    {
        request.setAttribute(RESOURCE_CLASS_KEY, null);
        request.setAttribute(RESOURCE_ID_KEY, null);

        String pathInfo = ((HttpServletRequest)request).getPathInfo();
        if(pathInfo != null && pathInfo.length() > 1)
        {
            pathInfo = stripLeadingSlash(pathInfo);
            String[] parts = pathInfo.split("/");

            String className = getResourceClassName(parts[0]);
            try
            {
                Class<? extends Resource> resourceClass = (Class<? extends Resource>)Class.forName(className);
                request.setAttribute(RESOURCE_CLASS_KEY, resourceClass);
            }
            catch(ClassNotFoundException e)
            {
                // TODO this should yield a 400 Bad Request - make sure that it does
                throw new ServletException(e);
            }

            if(parts.length > 1)
            {
                request.setAttribute(RESOURCE_ID_KEY, parts[1]);
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig)
    {
        // no default implementation
    }

    @Override
    public void destroy()
    {
        // no default implementation
    }

    /**
     * Strips a leading "/" character from the provided {@code String} if one is present.
     * 
     * @param string string from which to strip leading slash
     * @return string with leading slash removed, if necessary
     */
    private String stripLeadingSlash(String string)
    {
        if(string.startsWith("/"))
        {
            return string.substring(1);
        }
        return string;
    }

    /**
     * Given a "name" from a URL (e.g. /subscribers/5 has a "name" of "subscribers"), returns the corresponding
     * {@link Resource} implementation. Assumes that the name is at least two characters long and plural (ending with
     * "s") - the last character will be stripped.
     * 
     * @param urlName name from URL
     * @return fully-qualified {@code String} containing class name
     */
    private static String getResourceClassName(String urlName)
    {
        if(urlName == null)
        {
            throw new IllegalArgumentException("Name cannot be null");
        }

        if(urlName.trim().length() == 0)
        {
            throw new IllegalArgumentException("Name cannot be blank");
        }

        String capitalized = urlName.substring(0, 1).toUpperCase();
        if(urlName.length() > 1)
        {
            capitalized += urlName.substring(1);
        }

        // strip last character (it should be an "s")
        capitalized = capitalized.substring(0, capitalized.length() - 1);
        String qualified = "com.interact.listen.resource." + capitalized;
        return qualified;
    }
}
