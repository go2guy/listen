package com.interact.listen;

import com.interact.listen.marshal.MalformedContentException;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.MarshallerNotFoundException;
import com.interact.listen.marshal.converter.ConversionException;
import com.interact.listen.marshal.converter.Converter;
import com.interact.listen.marshal.xml.XmlMarshaller;
import com.interact.listen.resource.Resource;
import com.interact.listen.resource.ResourceList;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.*;
import org.hibernate.criterion.Restrictions;

public class ApiServlet extends HttpServlet
{
    public static final long serialVersionUID = 1L;

    private static final String XML_TAG = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        long start = time();

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction transaction = session.beginTransaction();

        try
        {
            UriResourceAttributes attributes = getResourceAttributes(request);
            if(attributes.name == null)
            {
                writeResponse(response, HttpServletResponse.SC_OK, "Welcome to the Listen Controller API", "text/plain");
                return;
            }

            Marshaller marshaller = getMarshaller(request.getHeader("Accept"));

            String className = getResourceClassName(attributes.name);
            Class<? extends Resource> resourceClass = (Class<? extends Resource>)Class.forName(className);

            if(attributes.id == null)
            {
                // no id, request is for a list of resources

                Map<String, String> query = getQueryParameters(request);
                Criteria criteria = createCriteria(session, resourceClass, query);

                long s = time();
                List<Resource> list = (List<Resource>)criteria.list();
                transaction.commit();
                System.out.println("TIMER: list() took " + (time() - s) + "ms");

                ResourceList resourceList = new ResourceList();
                resourceList.setList(list);
                resourceList.setMax(getMax(query));
                resourceList.setFirst(getFirst(query));
                resourceList.setSearchProperties(getSearchProperties(query));
                resourceList.setFields(getFields(query));

                // criteria.setProjection(Projections.rowCount());
                // Long total = (Long)criteria.list().get(0);
                // resourceList.setTotal(total);

                StringBuilder xml = new StringBuilder();
                if(marshaller instanceof XmlMarshaller)
                {
                    xml.append(XML_TAG);
                }

                s = time();
                xml.append(marshaller.marshal(resourceList, resourceClass));
                System.out.println("TIMER: marshal() took " + (time() - s) + "ms");

                writeResponse(response, HttpServletResponse.SC_OK, xml.toString(), "application/xml");
            }
            else
            {
                // id provided, request is looking for a specific resource

                // TODO verify that id is parseable as a Long first; if not, respond with 400 + error information
                long s = time();
                Resource resource = (Resource)session.get(resourceClass, Long.parseLong(attributes.id));
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

                writeResponse(response, HttpServletResponse.SC_OK, xml.toString(), "application/xml");
            }
        }
        catch(ClassNotFoundException e)
        {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        catch(CriteriaCreationException e)
        {
            e.printStackTrace();
            writeResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage(), "text/plain");
        }
        catch(Exception e)
        {
            e.printStackTrace();
            transaction.rollback();

            writeResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
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

        if(attributes.name == null || attributes.name.trim().length() == 0)
        {
            writeResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Cannot POST to [" + attributes.name + "]",
                          "text/plain");
            return;
        }

        if(attributes.id != null)
        {
            writeResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Cannot POST to specific resource [" +
                                                                        request.getPathInfo() + "]", "text/plain");
            return;
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction transaction = session.beginTransaction();

        try
        {
            Marshaller marshaller = getMarshaller(request.getHeader("Content-Type"));

            String className = getResourceClassName(attributes.name);
            Class<? extends Resource> resourceClass = (Class<? extends Resource>)Class.forName(className);

            long s = time();
            Resource resource = marshaller.unmarshal(request.getInputStream(), resourceClass);
            System.out.println("TIMER: unmarshal() took " + (time() - s) + "ms");

            // Resource resource = (Resource)resourceClass.newInstance();
            // String requestBody = this.readInputStreamContents(request.getInputStream());
            // resource.loadFromXml(requestBody, false);

            if(!resource.validate())
            {
                writeResponse(response, HttpServletResponse.SC_BAD_REQUEST, "The resource you sent was invalid",
                              "text/plain");
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

            writeResponse(response, HttpServletResponse.SC_CREATED, xml.toString(), "application/xml");
        }
        catch(ClassNotFoundException e)
        {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            writeResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error reading request body",
                          "text/plain");
            return;
        }
        catch(MalformedContentException e)
        {
            e.printStackTrace();
            writeResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                          "The content you provided was malformed, please fix it: " + e.getMessage(), "text/plain");
            return;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            transaction.rollback();

            writeResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
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

        if(attributes.id == null)
        {
            writeResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                          "PUT must be to a specific resource, not the list [" + request.getPathInfo() + "]",
                          "text/plain");
            return;
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction transaction = session.beginTransaction();

        try
        {
            Marshaller marshaller = getMarshaller(request.getHeader("Content-Type"));

            String className = getResourceClassName(attributes.name);
            Class<? extends Resource> resourceClass = (Class<? extends Resource>)Class.forName(className);

            // TODO verify that id is parseable as a Long first; if not, respond with 400 + error information
            Resource currentResource = (Resource)session.get(resourceClass, Long.parseLong(attributes.id));

            if(currentResource == null)
            {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            session.evict(currentResource);

            long s = time();
            Resource updatedResource = marshaller.unmarshal(request.getInputStream(), resourceClass);
            System.out.println("TIMER: unmarshal() took " + (time() - s) + "ms");

            updatedResource.setId(Long.parseLong(attributes.id));
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

                writeResponse(response, HttpServletResponse.SC_OK, xml.toString(), "application/xml");
            }
            else
            {
                writeResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                              "Resource failed validation.  Re-query resource before modifying/sending again",
                              "text/plain");
            }
        }
        catch(ClassNotFoundException e)
        {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            writeResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error reading request body",
                          "text/plain");
            return;
        }
        catch(StaleObjectStateException e)
        {
            transaction.rollback();
            writeResponse(response, HttpServletResponse.SC_CONFLICT,
                          "Data in the reqest was stale.  Re-query resource before sending again", "text/plain");
        }
        catch(MalformedContentException e)
        {
            e.printStackTrace();
            writeResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                          "The content you provided was malformed, please fix it", "text/plain");
            return;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            transaction.rollback();

            writeResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
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
        writeResponse(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "DELETE requests are not allowed",
                      "text/plain");
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
        attributes.name = parts[0].toLowerCase();
        if(parts.length > 1)
        {
            attributes.id = parts[1].toLowerCase();
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

    private void writeResponse(HttpServletResponse response, int statusCode, String content, String contentType)
    {
        response.setStatus(statusCode);
        response.setContentType(contentType);
        response.setContentLength(content.length());

        try
        {
            PrintWriter writer = response.getWriter();
            writer.print(content);
        }
        catch(IOException e)
        {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentLength(0);
        }
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

    private Map<String, String> getQueryParameters(HttpServletRequest request)
    {
        Map<String, String> map = new HashMap<String, String>();
        String query = request.getQueryString();

        if(query == null || query.trim().length() == 0)
        {
            return map;
        }

        String[] params = query.split("&", 0);
        if(params.length == 0)
        {
            return map;
        }

        for(String param : params)
        {
            String[] pair = param.split("=", 0);
            if(pair.length != 2)
            {
                System.out.println("Warning, pair [" + Arrays.toString(pair) + "] had a length of one, skipping");
                continue;
            }

            try
            {
                String key = URLDecoder.decode(pair[0], "UTF-8");
                String value = URLDecoder.decode(pair[1], "UTF-8");
                map.put(key, value);
            }
            catch(UnsupportedEncodingException e)
            {
                throw new AssertionError(e);
            }
        }

        return map;
    }

    /**
     * Creates a {@link Criteria} from the provided query parameters.
     * <p>
     * Some query parameters are special and control the result list:
     * <p>
     * <ul>
     * <li>"_first" - The index of the first result that should be retrieved</li>
     * <li>"_max" - The maximum number of results</li>
     * <li>"_fields" - Which fields to include in the embedded list elements</li>
     * </ul>
     * 
     * @param session Hibernate session
     * @param resourceClass class to create criteria for
     * @param queryParameters query parameters from URL
     * @return criteria that can be used to list results
     */
    private Criteria createCriteria(Session session, Class<? extends Resource> resourceClass,
                                    Map<String, String> queryParameters) throws CriteriaCreationException
    {
        Criteria criteria = session.createCriteria(resourceClass);

        int first = getFirst(queryParameters);
        criteria.setFirstResult(first);

        int max = getMax(queryParameters);
        criteria.setMaxResults(max);

        Map<String, String> searchProperties = getSearchProperties(queryParameters);
        for(Map.Entry<String, String> entry : searchProperties.entrySet())
        {
            String key = entry.getKey();
            String value = entry.getValue();

            String getMethod = "get" + key.substring(0, 1).toUpperCase() + key.substring(1);
            Method method = Marshaller.findMethod(getMethod, resourceClass);
            if(method == null)
            {
                System.out.println("Resource [" + resourceClass + "] does not have getter for [" + key +
                                   "], continuing");
                continue;
            }

            try
            {
                if(Resource.class.isAssignableFrom(method.getReturnType()))
                {
                    Long id = Marshaller.getIdFromHref(value);
                    criteria.createCriteria(key).add(Restrictions.idEq(id));
                }
                else
                {
                    Class<? extends Converter> converterClass = Marshaller.getConverterClass(method.getReturnType());
                    Converter converter = converterClass.newInstance();
                    Object convertedValue = converter.unmarshal(value);
                    criteria.add(Restrictions.eq(key, convertedValue));
                }
            }
            catch(IllegalAccessException e)
            {
                throw new AssertionError(e);
            }
            catch(java.lang.InstantiationException e)
            {
                throw new AssertionError(e);
            }
            catch(ConversionException e)
            {
                throw new CriteriaCreationException("Could not convert value [" + value + "] to type [" +
                                                    method.getReturnType() + "] for finding by [" + key + "]");
            }
        }

        return criteria;
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
        for(String key : queryParameters.keySet())
        {
            if(key.startsWith("_"))
            {
                continue;
            }

            searchProperties.put(key, queryParameters.get(key));
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

    private class UriResourceAttributes
    {
        private String name;
        private String id;
    }
}
