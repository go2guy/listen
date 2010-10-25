package com.interact.listen.api.security;

import com.interact.listen.HibernateUtil;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.security.AuthenticationService;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.hibernate.Session;

public class AuthenticationFilter implements Filter
{
    public static final String AUTHENTICATION_KEY = "AUTHENTICATION";

    private static final Logger LOG = Logger.getLogger(AuthenticationFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
        throws ServletException, IOException
    {
        String typeHeader = ((HttpServletRequest)request).getHeader("X-Listen-AuthenticationType");
        if(typeHeader != null)
        {
            typeHeader = new String(Base64.decodeBase64(typeHeader));
        }
        else
        {
            typeHeader = AuthenticationType.SYSTEM.name();
        }

        AuthenticationType type;

        try
        {
            type = AuthenticationType.valueOf(typeHeader);
        }
        catch(IllegalArgumentException e)
        {
            LOG.warn("Unknown X-Listen-AuthenticationType [" + typeHeader + "]");
            throw new UnauthorizedServletException("Authentication has unknown type [" + typeHeader + "]");
        }
        catch(NullPointerException e)
        {
            LOG.warn("Received API request with missing X-Listen-AuthenticationType header");
            throw new UnauthorizedServletException("Request is missing X-Listen-AuthenticationType header");
        }

        switch(type)
        {
            case SUBSCRIBER:

                String authUser = ((HttpServletRequest)request).getHeader("X-Listen-AuthenticationUsername");
                String authPass = ((HttpServletRequest)request).getHeader("X-Listen-AuthenticationPassword");

                if(authUser == null || authPass == null)
                {
                    LOG.warn("Received API request for user authentication with null username or password");
                    throw new UnauthorizedServletException("Request is missing credentials in header");
                }

                authUser = new String(Base64.decodeBase64(authUser));
                authPass = new String(Base64.decodeBase64(authPass));

                Session session = HibernateUtil.getSessionFactory().getCurrentSession();
                AuthenticationService service = new AuthenticationService();
                AuthenticationService.Result result = service.authenticate(session, authUser, authPass);
                if(!result.wasSuccessful())
                {
                    throw new UnauthorizedServletException(result.getCode().getMessage());
                }

                LOG.debug("result [" + result.getCode() + "], [" + result.getRealm() + "], [" + result.getSubscriber() + "]");

                request.setAttribute(AUTHENTICATION_KEY,
                                     Authentication.subscriberAuthentication(result.getSubscriber()));
                break;

            case SYSTEM:

//                String authToken = ((HttpServletRequest)request).getHeader("X-Listen-AuthenticationToken");
//                if(authToken == null)
//                {
//                    LOG.warn("Received API request for system authentication with null token");
//                    throw new UnauthorizedServletException("Request is missing credentials in header");
//                }
//
//                authToken = new String(Base64.decodeBase64(authToken));

                // TODO validate the token
                // TODO set request attribute
                request.setAttribute(AUTHENTICATION_KEY, Authentication.systemAuthentication("API User")); // FIXME hard-coded
                break;

            default:
                throw new AssertionError("Unhandled authentication type [" + type + "]");
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

    public enum AuthenticationType
    {
        // "SUBSCRIBER" as base64: "U1VCU0NSSUJFUg=="
        // "SYSTEM" as base64: "U1lTVEVN"
        SUBSCRIBER, SYSTEM;
    }

    public static final class Authentication
    {
        private AuthenticationType type;
        private Subscriber subscriber; // if type is subscriber, the subscriber
        private String system; // if type is system, the remote system identifier

        private Authentication()
        { }

        public static Authentication subscriberAuthentication(Subscriber subscriber)
        {
            Authentication auth = new Authentication();
            auth.type = AuthenticationType.SUBSCRIBER;
            auth.subscriber = subscriber;
            return auth;
        }

        public static Authentication systemAuthentication(String system)
        {
            Authentication auth = new Authentication();
            auth.type = AuthenticationType.SYSTEM;
            auth.system = system;
            return auth;
        }

        public AuthenticationType getType()
        {
            return type;
        }

        public Subscriber getSubscriber()
        {
            return subscriber;
        }

        public String getSystem()
        {
            return system;
        }
    }
}
