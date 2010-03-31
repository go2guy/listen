package com.interact.listen;

import com.interact.listen.ResourceListService.Builder;
import com.interact.listen.marshal.MalformedContentException;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.MarshallerNotFoundException;
import com.interact.listen.marshal.xml.XmlMarshaller;
import com.interact.listen.resource.Resource;

import java.io.IOException;
import java.util.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.hibernate.Transaction;

public class ApiServlet extends HttpServlet
{
    public static final long serialVersionUID = 1L;
    public static final String XML_TAG = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        long start = time();

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction transaction = session.beginTransaction();

        try
        {
            UriResourceAttributes attributes = getResourceAttributes(request);
            if(attributes.getName() == null)
            {
                ServletUtil.writeResponse(response, HttpServletResponse.SC_OK, "Welcome to the Listen Controller API",
                                          "text/plain");
                return;
            }

            Marshaller marshaller = getMarshaller(request.getHeader("Accept"));

            String className = getResourceClassName(attributes.getName());
            Class<? extends Resource> resourceClass = (Class<? extends Resource>)Class.forName(className);

            if(attributes.getId() == null)
            {
                // no id, request is for a list of resources

                Map<String, String> query = ServletUtil.getQueryParameters(request);

                Builder builder = new ResourceListService.Builder(resourceClass, session, marshaller);
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
                ResourceListService listService = builder.build();

                long s = time();
                String content = listService.list();
                System.out.println("TIMER: list() took " + (time() - s) + "ms");

                transaction.commit();
                ServletUtil.writeResponse(response, HttpServletResponse.SC_OK, content, marshaller.getContentType());
            }
            else
            {
                // id provided, request is looking for a specific resource

                // TODO verify that id is parseable as a Long first; if not, respond with 400 + error information
                long s = time();
                Resource resource = (Resource)session.get(resourceClass, Long.parseLong(attributes.getId()));
                transaction.commit();
                System.out.println("TIMER: list() took " + (time() - s) + "ms");

                if(resource == null)
                {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }

                StringBuilder xml = new StringBuilder();
                if(marshaller instanceof XmlMarshaller)
                {
                    xml.append(XML_TAG);
                }

                s = time();
                xml.append(marshaller.marshal(resource));
                System.out.println("TIMER: marshal() took " + (time() - s) + "ms");

                ServletUtil.writeResponse(response, HttpServletResponse.SC_OK, xml.toString(),
                                          marshaller.getContentType());
            }
        }
        catch(ClassNotFoundException e)
        {
            e.printStackTrace();
            transaction.rollback();
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        catch(CriteriaCreationException e)
        {
            e.printStackTrace();
            transaction.rollback();
            ServletUtil.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage(), "text/plain");
        }
        catch(Exception e)
        {
            e.printStackTrace();
            transaction.rollback();
            ServletUtil.writeResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                      "RUH ROH! Please contact the System Administrator", "text/plain");
            return;
        }
        finally
        {
            System.out.println("TIMER: GET " + request.getRequestURL() + " took " + (time() - start) + "ms");
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
    {
        long start = time();

        UriResourceAttributes attributes = getResourceAttributes(request);

        if(attributes.getName() == null || attributes.getName().trim().length() == 0)
        {
            ServletUtil.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Cannot POST to [" +
                                                                                    attributes.getName() + "]",
                                      "text/plain");
            return;
        }

        if(attributes.getId() != null)
        {
            ServletUtil.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                                      "Cannot POST to specific resource [" + request.getPathInfo() + "]", "text/plain");
            return;
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction transaction = session.beginTransaction();

        try
        {
            Marshaller marshaller = getMarshaller(request.getHeader("Content-Type"));

            String className = getResourceClassName(attributes.getName());
            Class<? extends Resource> resourceClass = (Class<? extends Resource>)Class.forName(className);

            long s = time();
            Resource resource = marshaller.unmarshal(request.getInputStream(), resourceClass);
            System.out.println("TIMER: unmarshal() took " + (time() - s) + "ms");

            // Resource resource = (Resource)resourceClass.newInstance();
            // String requestBody = this.readInputStreamContents(request.getInputStream());
            // resource.loadFromXml(requestBody, false);

            if(!resource.validate())
            {
                ServletUtil.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                                          "The resource you sent was invalid", "text/plain");
                return;
            }

            s = time();
            Long id = (Long)session.save(resource);
            System.out.println("TIMER: save() took " + (time() - s) + "ms");

            s = time();
            resource = (Resource)session.get(resourceClass, id);
            System.out.println("TIMER: get() took " + (time() - s) + "ms");

            transaction.commit();

            StringBuilder xml = new StringBuilder();
            if(marshaller instanceof XmlMarshaller)
            {
                xml.append(XML_TAG);
            }

            s = time();
            xml.append(marshaller.marshal(resource));
            System.out.println("TIMER: marshal() took " + (time() - s) + "ms");

            ServletUtil.writeResponse(response, HttpServletResponse.SC_CREATED, xml.toString(),
                                      marshaller.getContentType());
        }
        catch(ClassNotFoundException e)
        {
            e.printStackTrace();
            transaction.rollback();
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            transaction.rollback();
            ServletUtil.writeResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                      "Error reading request body", "text/plain");
            return;
        }
        catch(MalformedContentException e)
        {
            e.printStackTrace();
            transaction.rollback();
            ServletUtil.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                                      "The content you provided was malformed, please fix it: " + e.getMessage(),
                                      "text/plain");
            return;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            transaction.rollback();
            ServletUtil.writeResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                      "RUH ROH! Please contact the System Administrator", "text/plain");
            return;
        }
        finally
        {
            System.out.println("TIMER: POST " + request.getRequestURL() + " took " + (time() - start) + "ms");
        }
    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response)
    {
        long start = time();

        UriResourceAttributes attributes = getResourceAttributes(request);

        if(attributes.getId() == null)
        {
            ServletUtil.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                                      "PUT must be to a specific resource, not the list [" + request.getPathInfo() +
                                          "]", "text/plain");
            return;
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction transaction = session.beginTransaction();

        try
        {
            Marshaller marshaller = getMarshaller(request.getHeader("Content-Type"));

            String className = getResourceClassName(attributes.getName());
            Class<? extends Resource> resourceClass = (Class<? extends Resource>)Class.forName(className);

            // TODO verify that id is parseable as a Long first; if not, respond with 400 + error information
            Resource currentResource = (Resource)session.get(resourceClass, Long.parseLong(attributes.getId()));

            if(currentResource == null)
            {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            session.evict(currentResource);

            long s = time();
            Resource updatedResource = marshaller.unmarshal(request.getInputStream(), resourceClass);
            System.out.println("TIMER: unmarshal() took " + (time() - s) + "ms");

            updatedResource.setId(Long.parseLong(attributes.getId()));
            boolean isValid = updatedResource.validate();

            if(isValid)
            {
                s = time();
                session.update(updatedResource);
                System.out.println("TIMER: list() took " + (time() - s) + "ms");
            }

            transaction.commit();

            if(isValid)
            {
                StringBuilder xml = new StringBuilder();
                if(marshaller instanceof XmlMarshaller)
                {
                    xml.append(XML_TAG);
                }

                s = time();
                xml.append(marshaller.marshal(updatedResource));
                System.out.println("TIMER: marshal() took " + (time() - s) + "ms");

                ServletUtil.writeResponse(response, HttpServletResponse.SC_OK, xml.toString(),
                                          marshaller.getContentType());
            }
            else
            {
                ServletUtil
                           .writeResponse(
                                          response,
                                          HttpServletResponse.SC_BAD_REQUEST,
                                          "Resource failed validation.  Re-query resource before modifying/sending again",
                                          "text/plain");
            }
        }
        catch(ClassNotFoundException e)
        {
            e.printStackTrace();
            transaction.rollback();
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            transaction.rollback();
            ServletUtil.writeResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                      "Error reading request body", "text/plain");
            return;
        }
        catch(StaleObjectStateException e)
        {
            transaction.rollback();
            ServletUtil.writeResponse(response, HttpServletResponse.SC_CONFLICT,
                                      "Data in the reqest was stale.  Re-query resource before sending again",
                                      "text/plain");
        }
        catch(MalformedContentException e)
        {
            e.printStackTrace();
            transaction.rollback();
            ServletUtil.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                                      "The content you provided was malformed, please fix it", "text/plain");
            return;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            transaction.rollback();
            ServletUtil.writeResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                      "RUH ROH! Please contact the System Administrator", "text/plain");
            return;
        }
        finally
        {
            System.out.println("TIMER: PUT " + request.getRequestURL() + " took " + (time() - start) + "ms");
        }
    }

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response)
    {
        long start = time();

        UriResourceAttributes attributes = getResourceAttributes(request);

        if(attributes.getName() == null || attributes.getName().trim().length() == 0)
        {
            ServletUtil.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Cannot DELETE [" +
                                                                                    attributes.getName() + "]",
                                      "text/plain");
            return;
        }

        if(attributes.getId() == null)
        {
            ServletUtil.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                                      "DELETE must be on a specific resource, not the list [" + request.getPathInfo() +
                                          "]", "text/plain");
            return;
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction transaction = session.beginTransaction();

        try
        {
            String className = getResourceClassName(attributes.getName());
            Class<? extends Resource> resourceClass = (Class<? extends Resource>)Class.forName(className);

            // TODO verify that id is parseable as a Long first; if not, respond with 400 + error information
            long s = time();
            Resource resource = (Resource)session.get(resourceClass, Long.parseLong(attributes.getId()));
            System.out.println("TIMER: get() took " + (time() - s) + "ms");

            if(resource == null)
            {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            s = time();
            session.delete(resource);
            System.out.println("TIMER: delete() took " + (time() - s) + "ms");

            transaction.commit();
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
        catch(ClassNotFoundException e)
        {
            e.printStackTrace();
            transaction.rollback();
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            transaction.rollback();
            ServletUtil.writeResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                      "RUH ROH! Please contact the System Administrator", "text/plain");
            return;
        }
        finally
        {
            System.out.println("TIMER: DELETE took " + (time() - start) + "ms");
        }
    }

    /**
     * Given an {@link HttpServletRequest}, parses the path info from the URL and retrieves the relevant resource
     * information.
     * 
     * @param request request containing path information
     * @return
     */
    private UriResourceAttributes getResourceAttributes(HttpServletRequest request)
    {
        String pathInfo = request.getPathInfo();
        if(pathInfo == null || pathInfo.length() <= 1)
        {
            return new UriResourceAttributes();
        }

        pathInfo = stripLeadingSlash(pathInfo);
        String[] parts = pathInfo.split("/");

        UriResourceAttributes attributes = new UriResourceAttributes();
        attributes.setName(parts[0].toLowerCase());
        if(parts.length > 1)
        {
            attributes.setId(parts[1].toLowerCase());
        }
        return attributes;
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
            System.out.println("Creating Marshaller for 'Accept' content type of " + contentType);
            return Marshaller.createMarshaller(contentType);
        }
        catch(MarshallerNotFoundException e)
        {
            System.out.println("Unrecognized content-type provided, assuming XML");
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

            searchProperties.put(entry.getKey(), entry.getValue());
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
                    fields.add(field);
                }
            }
        }
        return fields;
    }

    private long time()
    {
        return System.currentTimeMillis();
    }

    private static class UriResourceAttributes
    {
        private String name;
        private String id;

        public String getName()
        {
            return name;
        }

        public String getId()
        {
            return id;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public void setId(String id)
        {
            this.id = id;
        }
    }
}
