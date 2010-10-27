package com.interact.listen.api.security;

import com.interact.listen.HibernateUtil;
import com.interact.listen.api.util.HttpDate;
import com.interact.listen.api.util.Signature;
import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.security.AuthenticationService;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.joda.time.LocalDateTime;

public class AuthenticationFilter implements Filter
{
    public static final String AUTHENTICATION_KEY = "AUTHENTICATION";

    private static final Logger LOG = Logger.getLogger(AuthenticationFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
        throws ServletException, IOException
    {
        if(!Boolean.valueOf(Configuration.get(Property.Key.AUTHENTICATE_API)))
        {
            filterChain.doFilter(request, response);
            return;
        }

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

                String date = ((HttpServletRequest)request).getHeader("Date");
                String signature = ((HttpServletRequest)request).getHeader("X-Listen-Signature");

                if(date == null)
                {
                    LOG.warn("Received request with missing 'Date' header");
                    throw new UnauthorizedServletException("Missing authorization component(s)");
                }

                if(signature == null)
                {
                    LOG.warn("Received request with missing 'X-Listen-Signature' header");
                    throw new UnauthorizedServletException("Missing authorization component(s)");
                }

                String expected = Signature.create(date);
                if(!expected.equals(signature))
                {
                    throw new UnauthorizedServletException("Signature is invalid");
                }

                try
                {
                    Date messageDate = HttpDate.parse(date);
                    LocalDateTime local = new LocalDateTime(messageDate.getTime());
                    LocalDateTime now = new LocalDateTime();

                    if(local.isBefore(now.minusMinutes(5)) || local.isAfter(now.plusMinutes(5)))
                    {
                        throw new UnauthorizedServletException("Unauthorized, request expired");
                    }
                }
                catch(ParseException e)
                {
                    throw new BadRequestServletException("Date header is not properly formatted");
                }

                request.setAttribute(AUTHENTICATION_KEY, Authentication.systemAuthentication(request.getRemoteHost()));
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
