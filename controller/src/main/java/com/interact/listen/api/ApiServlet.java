package com.interact.listen.api;

import com.interact.listen.*;
import com.interact.listen.ResourceListService.Builder;
import com.interact.listen.ResourceListService.SortOrder;
import com.interact.listen.api.security.AuthenticationFilter;
import com.interact.listen.api.security.AuthenticationFilter.Authentication;
import com.interact.listen.exception.*;
import com.interact.listen.history.Channel;
import com.interact.listen.marshal.MalformedContentException;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.MarshallerNotFoundException;
import com.interact.listen.marshal.xml.XmlMarshaller;
import com.interact.listen.resource.Resource;
import com.interact.listen.resource.Subscriber;

import java.io.IOException;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.hibernate.exception.ConstraintViolationException;

/**
 * Handles all API requests (DELETE, GET, POST, PUT).
 */
public class ApiServlet extends HttpServlet
{
    static
    {
        // prime the Hibernate config, bootstrap
        HibernateUtil.getSessionFactory();
        // FIXME ideally this could go somewhere else
    }

    public static final long serialVersionUID = 1L;
    public static final String XML_TAG = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    /** Class logger */
    private static final Logger LOG = Logger.getLogger(ApiServlet.class);

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        PersistenceService persistenceService = getPersistenceService(session, request);

        UriResourceAttributes attributes = getResourceAttributes(request);
        if(attributes.getResourceClass() == null)
        {
            OutputBufferFilter.append(request, "Welcome to the Listen Controller API", "text/plain");
            return;
        }

        Marshaller marshaller = getMarshaller(request.getHeader("Accept"));

        if(attributes.getId() == null)
        {
            // no id, request is for a list of resources
            Map<String, String> query = ServletUtil.getQueryParameters(request);

            Builder builder = new ResourceListService.Builder(attributes.getResourceClass(), session, marshaller);
            Map<String, String> searchProperties = getSearchProperties(query);
            for(Map.Entry<String, String> entry : searchProperties.entrySet())
            {
                builder.addSearchProperty(entry.getKey(), entry.getValue());
            }
            Set<String> returnFields = getFields(query);
            for(String field : returnFields)
            {
                builder.addReturnField(field);
            }
            builder.withFirst(getFirst(query));
            builder.withMax(getMax(query));
            if(query.containsKey("_uniqueResult"))
            {
                builder.uniqueResult(Boolean.valueOf(query.get("_uniqueResult")));
            }
            if(query.containsKey("_or"))
            {
                builder.or(Boolean.valueOf(query.get("_or")));
            }
            if(query.containsKey("_sortBy") && query.containsKey("_sortOrder"))
            {
                builder.sortBy(query.get("_sortBy"), SortOrder.valueOf(query.get("_sortOrder")));
            }

            ResourceListService listService = builder.build();
            try
            {
                String content = listService.list();
                OutputBufferFilter.append(request, content, marshaller.getContentType());
            }
            catch(UniqueResultNotFoundException e)
            {
                throw new ListenServletException(HttpServletResponse.SC_NOT_FOUND, e);
            }
            catch(CriteriaCreationException e)
            {
                throw new ListenServletException(HttpServletResponse.SC_BAD_REQUEST, e.getMessage(), "text/plain", e);
            }
        }
        else
        {
            // id provided, request is looking for a specific resource
            if(!isValidResourceId(attributes.getId()))
            {
                throw new ListenServletException(HttpServletResponse.SC_NOT_FOUND);
            }

            Resource resource = persistenceService.get(attributes.getResourceClass(),
                                                       Long.parseLong(attributes.getId()));
            if(resource == null)
            {
                throw new ListenServletException(HttpServletResponse.SC_NOT_FOUND);
            }

            StringBuilder content = new StringBuilder();
            if(marshaller instanceof XmlMarshaller)
            {
                content.append(XML_TAG);
            }

            content.append(marshaller.marshal(resource));
            OutputBufferFilter.append(request, content.toString(), marshaller.getContentType());
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        UriResourceAttributes attributes = getResourceAttributes(request);
        if(attributes.getResourceClass() == null)
        {
            throw new BadRequestServletException("Cannot POST to [" + attributes.getResourceClass() + "]");
        }

        if(attributes.getId() != null)
        {
            throw new BadRequestServletException("Cannot POST to specific resource [" +
                                                 attributes.getResourceClass().getSimpleName() + "]");
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        PersistenceService persistenceService = getPersistenceService(session, request);

        Marshaller marshaller = getMarshaller(request.getHeader("Content-Type"));
        try
        {
            Resource resource = marshaller.unmarshal(request.getInputStream(), attributes.getResourceClass()
                                                                                         .newInstance(), false);
            if(!resource.validate() && resource.hasErrors())
            {
                throw new BadRequestServletException("The resource you sent was invalid: " +
                                                     resource.errors().toString());
            }

            try
            {
                Long id = persistenceService.save(resource);
                resource = persistenceService.get(attributes.getResourceClass(), id);
            }
            catch(ConstraintViolationException e)
            {
                throw new ListenServletException(HttpServletResponse.SC_BAD_REQUEST,
                                                 "The content you provided causes a constraint violation, please fix it",
                                                 "text/plain", e);
            }

            StringBuilder content = new StringBuilder();
            if(marshaller instanceof XmlMarshaller)
            {
                content.append(XML_TAG);
            }

            content.append(marshaller.marshal(resource));
            response.setStatus(HttpServletResponse.SC_CREATED);
            OutputBufferFilter.append(request, content.toString(), marshaller.getContentType());
        }
        catch(IllegalAccessException e)
        {
            throw new AssertionError(e);
        }
        catch(InstantiationException e)
        {
            throw new AssertionError(e);
        }
        catch(MalformedContentException e)
        {
            throw new ListenServletException(HttpServletResponse.SC_BAD_REQUEST,
                                             "The content you provided was malformed, please fix it: " + e.getMessage(),
                                             "text/plain", e);
        }
    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        UriResourceAttributes attributes = getResourceAttributes(request);
        if(attributes.getId() == null)
        {
            throw new BadRequestServletException("PUT must be to a specific resource, not the list [" +
                                                 attributes.getResourceClass().getSimpleName() + "]");
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        PersistenceService persistenceService = getPersistenceService(session, request);

        Marshaller marshaller = getMarshaller(request.getHeader("Content-Type"));
        if(!isValidResourceId(attributes.getId()))
        {
            throw new ListenServletException(HttpServletResponse.SC_NOT_FOUND);
        }

        Resource resource = persistenceService.get(attributes.getResourceClass(), Long.parseLong(attributes.getId()));
        if(resource == null)
        {
            throw new ListenServletException(HttpServletResponse.SC_NOT_FOUND);
        }

        Resource original = resource.copy(false);
        try
        {
            resource = marshaller.unmarshal(request.getInputStream(), resource, false);
        }
        catch(MalformedContentException e)
        {
            throw new ListenServletException(HttpServletResponse.SC_BAD_REQUEST,
                                             "The content you provided was malformed, please fix it", "text/plain", e);
        }

        if(!resource.validate())
        {
            throw new ListenServletException(HttpServletResponse.SC_BAD_REQUEST, "Resource failed validation: " +
                                                                                 resource.errors().toString(),
                                             "text/plain");
        }

        try
        {
            persistenceService.update(resource, original);
        }
        catch(StaleObjectStateException e)
        {
            throw new ListenServletException(HttpServletResponse.SC_CONFLICT,
                                             "Data in the reqest was stale.  Re-query resource before sending again",
                                             "text/plain", e);
        }
        catch(ConstraintViolationException e)
        {
            throw new ListenServletException(HttpServletResponse.SC_BAD_REQUEST,
                                             "The content you provided causes a constraint violation, please fix it: " +
                                                 e.getConstraintName(), "text/plain", e);
        }

        StringBuilder content = new StringBuilder();
        if(marshaller instanceof XmlMarshaller)
        {
            content.append(XML_TAG);
        }

        content.append(marshaller.marshal(resource));
        OutputBufferFilter.append(request, content.toString(), marshaller.getContentType());
    }

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        UriResourceAttributes attributes = getResourceAttributes(request);
        if(attributes.getResourceClass() == null)
        {
            throw new BadRequestServletException("Cannot DELETE [" + attributes.getResourceClass() + "]");
        }

        if(attributes.getId() == null)
        {
            throw new BadRequestServletException("DELETE must be on a specific resource, not the list [" +
                                                 attributes.getResourceClass().getSimpleName() + "]");
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        PersistenceService persistenceService = getPersistenceService(session, request);

        if(!isValidResourceId(attributes.getId()))
        {
            throw new ListenServletException(HttpServletResponse.SC_NOT_FOUND);
        }

        Resource resource = persistenceService.get(attributes.getResourceClass(), Long.parseLong(attributes.getId()));
        if(resource == null)
        {
            throw new ListenServletException(HttpServletResponse.SC_NOT_FOUND);
        }

        persistenceService.delete(resource);
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    /**
     * Given an {@link HttpServletRequest}, retrieves the relevant resource information.
     * 
     * @param request request containing attributes set by {@link ApiResourceLocatorFilter}
     * @return
     */
    private UriResourceAttributes getResourceAttributes(HttpServletRequest request)
    {
        UriResourceAttributes attributes = new UriResourceAttributes();
        Class<? extends Resource> resourceClass = (Class<? extends Resource>)request.getAttribute(ApiResourceLocatorFilter.RESOURCE_CLASS_KEY);
        String id = (String)request.getAttribute(ApiResourceLocatorFilter.RESOURCE_ID_KEY);

        attributes.setResourceClass(resourceClass);
        attributes.setId(id);
        return attributes;
    }

    /**
     * Retrieves a {@link Marshaller} for the request.
     * 
     * @param request request
     * @return {@code Marshaller}
     */
    private Marshaller getMarshaller(String contentType)
    {
        try
        {
            LOG.debug("Creating Marshaller for 'Accept' content type of " + contentType);
            return Marshaller.createMarshaller(contentType);
        }
        catch(MarshallerNotFoundException e)
        {
            LOG.warn("Unrecognized content-type provided, assuming XML");
            return new XmlMarshaller();
        }
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

    private Map<String, String> getSearchProperties(Map<String, String> queryParameters)
    {
        Map<String, String> searchProperties = new HashMap<String, String>();
        for(Map.Entry<String, String> entry : queryParameters.entrySet())
        {
            if(entry.getKey().startsWith("_"))
            {
                continue;
            }

            searchProperties.put(Marshaller.decodeUrl(entry.getKey()), Marshaller.decodeUrl(entry.getValue()));
        }
        return searchProperties;
    }

    private Set<String> getFields(Map<String, String> queryParameters)
    {
        Set<String> fields = new HashSet<String>();
        if(queryParameters.containsKey("_fields"))
        {
            String[] split = queryParameters.get("_fields").split(",");
            for(String field : split)
            {
                if(!field.trim().equals(""))
                {
                    fields.add(Marshaller.decodeUrl(field));
                }
            }
        }
        return fields;
    }

    private static boolean isValidResourceId(String id)
    {
        try
        {
            Long.parseLong(id);
            return true;
        }
        catch(NumberFormatException e)
        {
            return false;
        }
    }

    private static PersistenceService getPersistenceService(Session session, HttpServletRequest request)
    {
        Subscriber subscriber = null;

        Authentication auth = (Authentication)request.getAttribute(AuthenticationFilter.AUTHENTICATION_KEY);
        if(auth != null)
        {
            if(auth.getType() == AuthenticationFilter.AuthenticationType.SUBSCRIBER && auth.getSubscriber() != null)
            {
                subscriber = auth.getSubscriber();
            }
        }
        else
        {
            String subscriberHeader = request.getHeader("X-Listen-Subscriber");
            if(subscriberHeader != null)
            {
                Long id = Marshaller.getIdFromHref(subscriberHeader);
                if(id != null)
                {
                    subscriber = Subscriber.queryById(session, id);
                }
    
                if(subscriber == null || id == null)
                {
                    LOG.warn("X-Listen-Subscriber HTTP header contained unknown subscriber href [" + subscriberHeader + "]");
                }
            }
        }

        Channel channel = (Channel)request.getAttribute(RequestInformationFilter.CHANNEL_KEY);
        return new DefaultPersistenceService(session, subscriber, channel);
    }

    private static class UriResourceAttributes
    {
        private Class<? extends Resource> resourceClass;
        private String id;

        public Class<? extends Resource> getResourceClass()
        {
            return resourceClass;
        }

        public String getId()
        {
            return id;
        }

        public void setResourceClass(Class<? extends Resource> resourceClass)
        {
            this.resourceClass = resourceClass;
        }

        public void setId(String id)
        {
            this.id = id;
        }
    }
}
