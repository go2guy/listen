package com.interact.listen.api.security;

import com.interact.listen.HibernateUtil;
import com.interact.listen.api.ApiResourceLocatorFilter;
import com.interact.listen.api.security.AuthenticationFilter.Authentication;
import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.resource.Resource;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.Voicemail;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.hibernate.Session;

/**
 * Verifies that the current authenticated user has permission to access the resources they're trying to access.
 * Unauthorized access will probably result in a 401 Unauthorized.
 * <p/>
 * This filter depends on the following filters having executed before it (in no particular order):
 * <ul>
 * <li>com.interact.listen.api.ApiResourceLocatorFilter</li>
 * <li>com.interact.listen.api.security.AuthenticationFilter</li>
 * <li>com.interact.listen.OpenSessionInViewFilter</li>
 * <li>com.interact.listen.RequestInformationFilter</li>
 * </ul>
 */
public class AuthorizationFilter implements Filter
{
    private static final Logger LOG = Logger.getLogger(AuthorizationFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
        throws ServletException, IOException
    {
        if(!Boolean.valueOf(Configuration.get(Property.Key.AUTHENTICATE_API)))
        {
            filterChain.doFilter(request, response);
            return;
        }

        // if the client is not authenticated, they can't access anything  
        Authentication authentication = (Authentication)request.getAttribute(AuthenticationFilter.AUTHENTICATION_KEY);
        if(authentication == null)
        {
            LOG.warn("Authentication is null, access is unauthorized");
            throw new UnauthorizedServletException("Not authenticated");
        }

        Class<? extends Resource> resourceClass = (Class<? extends Resource>)request.getAttribute(ApiResourceLocatorFilter.RESOURCE_CLASS_KEY);
        String resourceId = (String)request.getAttribute(ApiResourceLocatorFilter.RESOURCE_ID_KEY);

        boolean authorized = false;
        switch(authentication.getType())
        {
            case SUBSCRIBER:

                Subscriber subscriber = authentication.getSubscriber();
                Session session = HibernateUtil.getSessionFactory().getCurrentSession();

                if(resourceClass == null)
                {
                    if(((HttpServletRequest)request).getRequestURI().startsWith("/meta/audio/file"))
                    {
                        Long id = Long.valueOf(((HttpServletRequest)request).getPathInfo().substring(1));
                        Voicemail v = Voicemail.queryById(session, id);
                        
                        if(v != null && v.getSubscriber().equals(subscriber))
                        {
                            authorized = true;
                        }
                    }
                    
                    break;
                }

                if(resourceClass.equals(Subscriber.class))
                {
                    String username = request.getParameter("username");
                    
                    if(username != null && subscriber.getUsername().equals(username))
                    {
                        authorized = true;
                    }
                }
                
                if(resourceClass.equals(Voicemail.class))
                {
                    if(resourceId == null) // list
                    {
                        String s = request.getParameter("subscriber");
                        if(s != null && s.equals("/subscribers/" + subscriber.getId()))
                        {
                            authorized = true;
                        }
                    }
                    else // specific voicemail
                    {
                        Long id = Long.parseLong(resourceId);

                        // TODO possibly verify that it's a PUT request, and if so, limit them to updating
                        // [isNew, hasNotified]
                        Voicemail v = Voicemail.queryById(session, id);
                        if(v != null && v.getSubscriber().equals(subscriber))
                        {
                            authorized = true;
                        }
                    }
                }
                break;

            case SYSTEM:

                authorized = true;
                break;

            default:
                throw new IllegalStateException("Authentication has unknown type [" + authentication.getType() + "]");
        }

        if(!authorized)
        {
            throw new UnauthorizedServletException("Access to requested resource is not authorized for authenticated client");
        }

        filterChain.doFilter(request, response);
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
