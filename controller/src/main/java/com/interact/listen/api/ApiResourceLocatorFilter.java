package com.interact.listen.api;

import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.resource.Resource;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * Extracts information about an API {@link Resource} from the request path information. The extracted information is
 * put into request attributes. The following are example paths and how they would be extracted into the request
 * attributes.
 * <table>
 * <tr>
 * <th>Path Info</th>
 * <th>RESOURCE_CLASS</th>
 * <th>RESOURCE_ID</th>
 * </tr>
 * <tr>
 * <td>{@code /conferences}</td>
 * <td>{@code com.interact.listen.resource.Conference.class}</td>
 * <td>{@code null}</td>
 * </tr>
 * <tr>
 * <td>{@code /conferences/15}</td>
 * <td>{@code com.interact.listen.resource.Conference.class}</td>
 * <td>{@code 15}</td>
 * </tr>
 * </table>
 */
public class ApiResourceLocatorFilter implements Filter
{
    public static final String RESOURCE_CLASS_KEY = "RESOURCE_CLASS";
    public static final String RESOURCE_ID_KEY = "RESOURCE_ID";

    /** Class logger */
    private static final Logger LOG = Logger.getLogger(ApiResourceLocatorFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
        throws ServletException, IOException
    {
        request.setAttribute(RESOURCE_CLASS_KEY, null);
        request.setAttribute(RESOURCE_ID_KEY, null);

        final String pathRegex = "/([A-Za-z]+s)(/([0-9]+)(/([A-Za-z]+))?)?";
        String pathInfo = ((HttpServletRequest)request).getPathInfo();
        if(pathInfo != null && pathInfo.length() > 1)
        {
            Pattern pattern = Pattern.compile(pathRegex);
            Matcher matcher = pattern.matcher(pathInfo);

            if(!matcher.matches())
            {
                throw new BadRequestServletException("Unparseable URL");
            }

            if(matcher.group(3) != null && !matcher.group(3).trim().equals(""))
            {
                request.setAttribute(RESOURCE_ID_KEY, matcher.group(3));
                LOG.debug("Set [" + RESOURCE_ID_KEY + "] to [" + matcher.group(3) + "]");
            }

            if(matcher.group(1) != null && !matcher.group(1).trim().equals(""))
            {
                try
                {
                    String className = getResourceClassName(matcher.group(1));
                    Class<? extends Resource> resourceClass = (Class<? extends Resource>)Class.forName(className);
                    request.setAttribute(RESOURCE_CLASS_KEY, resourceClass);
                    LOG.debug("Set [" + RESOURCE_CLASS_KEY + "] to [" + resourceClass + "]");
                }
                catch(ClassNotFoundException e)
                {
                    throw new BadRequestServletException("Resource not found for [" + matcher.group(1) + "]");
                }
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
