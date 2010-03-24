package com.interact.listen;

import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.MarshallerNotFoundException;
import com.interact.listen.marshal.xml.XmlMarshaller;
import com.interact.listen.resource.Resource;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class ApiServlet extends HttpServlet
{
    public static final long serialVersionUID = 1L;

    private static final String XML_TAG = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        long start = System.currentTimeMillis();

        UriResourceAttributes attributes = getResourceAttributes(request);

        if(attributes.name == null)
        {
            writeResponse(response, HttpServletResponse.SC_OK, "Welcome to the Listen Controller API", "text/plain");
            return;
        }

        Marshaller marshaller = getMarshaller(request.getHeader("Accept"));

        try
        {
            String className = getResourceClassName(attributes.name);
            Class resourceClass = Class.forName(className);

            if(attributes.id == null)
            {
                // no id, request is for a list of resources
                Session session = HibernateUtil.getSessionFactory().getCurrentSession();
                Transaction transaction = session.beginTransaction();

                Criteria criteria = session.createCriteria(resourceClass);
                criteria.setMaxResults(50);
                List<Resource> list = criteria.list();
                transaction.commit();

                StringBuilder xml = new StringBuilder();
                if(marshaller instanceof XmlMarshaller)
                {
                    xml.append(XML_TAG);
                }
                xml.append(marshaller.marshal(list, resourceClass));

                writeResponse(response, HttpServletResponse.SC_OK, xml.toString(), "application/xml");
            }
            else
            {
                // id provided, request is looking for a specific resource
                Session session = HibernateUtil.getSessionFactory().getCurrentSession();
                Transaction transaction = session.beginTransaction();

                // TODO verify that id is parseable as a Long first; if not, respond with 400 + error information
                Resource resource = (Resource)session.get(resourceClass, Long.parseLong(attributes.id));
                transaction.commit();

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
                xml.append(marshaller.marshal(resource));

                writeResponse(response, HttpServletResponse.SC_OK, xml.toString(), "application/xml");
            }
        }
        catch(ClassNotFoundException e)
        {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        finally
        {
            System.out.println("GET " + request.getRequestURL() + " took " + (System.currentTimeMillis() - start) +
                               "ms");
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
    {
        long start = System.currentTimeMillis();

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

        Marshaller marshaller = getMarshaller(request.getHeader("Content-Type"));

        try
        {
            String className = getResourceClassName(attributes.name);
            Class<? extends Resource> resourceClass = (Class<? extends Resource>)Class.forName(className);

            Resource resource = marshaller.unmarshal(request.getInputStream(), resourceClass);
            // Resource resource = (Resource)resourceClass.newInstance();
            // String requestBody = this.readInputStreamContents(request.getInputStream());
            // resource.loadFromXml(requestBody, false);

            if(resourceClass != resource.getClass())
            {
                writeResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                              "Resource in request body did not match type in URI", "text/plain");
            }

            Session session = HibernateUtil.getSessionFactory().getCurrentSession();
            Transaction transaction = session.beginTransaction();
            Long id = (Long)session.save(resource);
            resource = (Resource)session.get(resourceClass, id);

            transaction.commit();

            StringBuilder xml = new StringBuilder();
            if(marshaller instanceof XmlMarshaller)
            {
                xml.append(XML_TAG);
            }
            xml.append(marshaller.marshal(resource));

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
        finally
        {
            System.out.println("POST " + request.getRequestURL() + " took " + (System.currentTimeMillis() - start) +
                               "ms");
        }
    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response)
    {
        long start = System.currentTimeMillis();

        UriResourceAttributes attributes = getResourceAttributes(request);

        Marshaller marshaller = getMarshaller(request.getHeader("Content-Type"));

        try
        {
            String className = getResourceClassName(attributes.name);
            Class<? extends Resource> resourceClass = (Class<? extends Resource>)Class.forName(className);

            // id provided, request is looking for a specific resource
            Session session = HibernateUtil.getSessionFactory().getCurrentSession();
            Transaction transaction = session.beginTransaction();

            // TODO verify that id is parseable as a Long first; if not, respond with 400 + error information
            Resource currentResource = (Resource)session.get(resourceClass, Long.parseLong(attributes.id));
            transaction.commit();

            if(currentResource == null)
            {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            Resource updatedResource = marshaller.unmarshal(request.getInputStream(), resourceClass);
            
            updatedResource.setId(Long.parseLong(attributes.id));

            if(updatedResource.validate())
            {
                session = HibernateUtil.getSessionFactory().getCurrentSession();
                transaction = session.beginTransaction();
                session.update(updatedResource);

                transaction.commit();

                StringBuilder xml = new StringBuilder();
                if(marshaller instanceof XmlMarshaller)
                {
                    xml.append(XML_TAG);
                }
                xml.append(marshaller.marshal(updatedResource));
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
        catch(org.hibernate.StaleObjectStateException e)
        {
            writeResponse(response, HttpServletResponse.SC_CONFLICT,
                          "Data in the reqest was stale.  Re-query resource before sending again", "text/plain");
        }
        finally
        {
            System.out.println("PUT " + request.getRequestURL() + " took " + (System.currentTimeMillis() - start) +
                               "ms");
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
            System.out.println("Creatign Marshaller for 'Accept' content type of " + contentType);
            return Marshaller.createMarshaller(contentType);
        }
        catch(MarshallerNotFoundException e)
        {
            System.out.println("Unrecognized content-type provided, assuming XML");
            return new XmlMarshaller();
        }
    }

    private class UriResourceAttributes
    {
        private String name;
        private String id;
    }
}
