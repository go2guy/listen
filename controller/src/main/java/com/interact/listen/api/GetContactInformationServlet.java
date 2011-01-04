package com.interact.listen.api;

import com.interact.listen.*;
import com.interact.listen.api.security.AuthenticationFilter;
import com.interact.listen.api.security.AuthenticationFilter.Authentication;
import com.interact.listen.contacts.resource.EmailContact;
import com.interact.listen.contacts.resource.NumberContact;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.ListenServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.history.Channel;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.MarshallerNotFoundException;
import com.interact.listen.marshal.json.JsonMarshaller;
import com.interact.listen.marshal.xml.XmlMarshaller;
import com.interact.listen.resource.*;
import com.interact.listen.stats.Stat;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.hibernate.Session;

public class GetContactInformationServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(GetContactInformationServlet.class);

    private enum RequestType
    {
        EMAILCONTACTS(Stat.META_API_GET_EMAIL_CONTACTS, EmailContact.class),
        NUMBERCONTACTS(Stat.META_API_GET_NUMBER_CONTACTS, NumberContact.class);
        
        private Stat stat;
        private Class<? extends Resource> rClass;
        
        private RequestType(Stat stat, Class<? extends Resource> rClass)
        {
            this.stat = stat;
            this.rClass = rClass;
        }
        
        Stat getStat()
        {
            return stat;
        }
        
        Class<? extends Resource> getResourceClass()
        {
            return rClass;
        }
    }
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        getPersistenceService(session, request);

        final Map<String, String> query = ServletUtil.getQueryParameters(request);

        final int first = getFirst(query);
        final int max = getMax(query);
        
        final Matcher matcher = createMatcher(request);
        final Long rId = getRequestId(matcher);
        final RequestType type = getRequestType(matcher);
        
        final Marshaller marshaller = getMarshaller(request.getHeader("Accept"));

        StringBuilder content;
        
        ServletUtil.sendStat(request, type.getStat());
        
        if(rId != null)
        {
            LOG.info("Searching for " + type.name() + " id: " + rId);
            Resource resource;
            switch(type)
            {
                case NUMBERCONTACTS:
                    resource = NumberContact.queryById(session, rId);
                    break;
                case EMAILCONTACTS:
                default:
                    resource = EmailContact.queryById(session, rId);
                    break;
            }
            if(resource == null)
            {
                throw new ListenServletException(HttpServletResponse.SC_NOT_FOUND);
            }
            content = createContent(marshaller, resource);
        }
        else
        {
            LOG.info("Searching for all " + type.name());
            ResourceList rList;
            switch(type)
            {
                case NUMBERCONTACTS:
                    rList = NumberContact.queryForNumbers(session, first, max);
                    break;
                case EMAILCONTACTS:
                default:
                    rList = EmailContact.queryForEmails(session, first, max);
                    break;
            }
            content = createContent(marshaller, rList, type.getResourceClass());
        }
        
        OutputBufferFilter.append(request, content.toString(), marshaller.getContentType());
    }
    
    private StringBuilder createContent(Marshaller marshaller, ResourceList rList, Class<? extends Resource> rClass)
    {
        StringBuilder content = new StringBuilder();

        if(marshaller instanceof XmlMarshaller)
        {
            content.append(ApiServlet.XML_TAG);
        }

        content.append(marshaller.marshal(rList, rClass));

        return content;
    }

    private StringBuilder createContent(Marshaller marshaller, Resource resource)
    {
        StringBuilder content = new StringBuilder();

        if(marshaller instanceof XmlMarshaller)
        {
            content.append(ApiServlet.XML_TAG);
        }

        content.append(marshaller.marshal(resource));

        return content;
    }

    private static final Pattern PATH_PATTERN = Pattern.compile("/([A-Za-z]+s)(/([0-9]+)(/([A-Za-z]+))?)?");

    private static Matcher createMatcher(HttpServletRequest request) throws BadRequestServletException
    {
        final String path = request.getPathInfo();
        
        Matcher matcher = path == null || path.length() <= 1 ? null : PATH_PATTERN.matcher(path);
        
        if(matcher == null || !matcher.matches())
        {
            throw new BadRequestServletException("Unparseable URL");
        }

        return matcher;
    }
    
    private static Long getRequestId(Matcher matcher) throws ListenServletException
    {
        Long id = null;
        if(matcher != null && matcher.group(3) != null && matcher.group(3).trim().length() > 0)
        {
            try
            {
                id = Long.parseLong(matcher.group(3));
            }
            catch(NumberFormatException e)
            {
                LOG.error("error parsing ID", e);
                throw new ListenServletException(HttpServletResponse.SC_BAD_REQUEST, "invalid ID", "text/plain");
            }
        }
        return id;
    }
    
    private RequestType getRequestType(Matcher matcher)
    {
        RequestType type = RequestType.EMAILCONTACTS;
        
        if(matcher != null && matcher.group(1).length() > 0)
        {
            String uPath = matcher.group(1).toUpperCase();
            try
            {
                type = RequestType.valueOf(uPath);
            }
            catch(Exception e)
            {
                LOG.warn("unable to parse request type", e);
            }
        }

        return type;
    }

    private int getMax(Map<String, String> queryParameters)
    {
        int max = 100;
        if(queryParameters.containsKey("_max"))
        {
            int param = Integer.parseInt(queryParameters.get("_max"));
            if(param < max && param > 0)
            {
                max = param;
            }
        }   
        return max;
    }

    private int getFirst(Map<String, String> queryParameters)
    {
        int first = 0;
        if(queryParameters.containsKey("_first"))
        {
            first = Integer.parseInt(queryParameters.get("_first"));
        }
        return first;
    }

    private Marshaller getMarshaller(String contentType)
    {
        try
        {
            LOG.debug("Creating Marshaller for 'Accept' content type of " + contentType);
            return Marshaller.createMarshaller(contentType);
        }
        catch(MarshallerNotFoundException e)
        {
            LOG.warn("Unrecognized content-type provided, assuming Json");
            return new JsonMarshaller();
        }
    }

    private static PersistenceService getPersistenceService(Session session, HttpServletRequest request) throws UnauthorizedServletException
    {
        Subscriber subscriber = null;

        Authentication auth = (Authentication)request.getAttribute(AuthenticationFilter.AUTHENTICATION_KEY);
        if(auth != null)
        {
            if(auth.getType() == AuthenticationFilter.AuthenticationType.SUBSCRIBER && auth.getSubscriber() != null)
            {
                subscriber = auth.getSubscriber();
            }
            if(subscriber == null)
            {
                throw new UnauthorizedServletException("Unathorized Subscriber");
            }
        }
        else
        {
            String subHeader = request.getHeader("X-Listen-Subscriber");
            if(subHeader == null)
            {
                subHeader = request.getHeader("X-Listen-AuthenticationUsername");
                if(subHeader != null)
                {
                    subHeader = new String(Base64.decodeBase64(subHeader));
                    subscriber = Subscriber.queryByUsername(session, subHeader);
                }
                if(subscriber == null)
                {
                    throw new UnauthorizedServletException("Unable to get subscriber from encoded header [" + subHeader + "]");
                }
            }
            else
            {
                Long id = Marshaller.getIdFromHref(subHeader);
                if(id != null)
                {
                    subscriber = Subscriber.queryById(session, id);
                }
                if(subscriber == null)
                {
                    throw new UnauthorizedServletException("X-Listen-Subscriber HTTP header contained unknown subscriber href [" + subHeader + "]");
                }
            }
        }
        
        Channel channel = (Channel)request.getAttribute(RequestInformationFilter.CHANNEL_KEY);
        return new DefaultPersistenceService(session, subscriber, channel);
    }
}
