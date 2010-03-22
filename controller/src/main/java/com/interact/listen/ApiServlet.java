package com.interact.listen;

import com.interact.listen.resource.Resource;
import com.interact.listen.xml.Marshaller;

import java.io.*;
import java.lang.InstantiationException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
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
        UriResourceAttributes attributes = getResourceAttributes(request);

        if(attributes.name == null)
        {
            writeResponse(response, HttpServletResponse.SC_OK, "Welcome to the Listen Controller API", "text/plain");
            return;
        }

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
                xml.append(XML_TAG);

                if(list.size() == 0)
                {
                    xml.append(Marshaller.marshalOpeningResourceTag(attributes.name, null, true));
                }
                else
                {
                    xml.append(Marshaller.marshalOpeningResourceTag(attributes.name, "/" + attributes.name, false));
                    for(Resource resource : list)
                    {
                        xml.append(Marshaller.marshalOpeningResourceTag(resource, true));
                    }
                    xml.append(Marshaller.marshalClosingResourceTag(attributes.name));
                }

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
                xml.append(XML_TAG);
                xml.append(Marshaller.marshal(resource));

                writeResponse(response, HttpServletResponse.SC_OK, xml.toString(), "application/xml");
            }
        }
        catch(ClassNotFoundException e)
        {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
    {
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

        try
        {
            String className = getResourceClassName(attributes.name);
            Class resourceClass = Class.forName(className);

            Resource resource = (Resource)resourceClass.newInstance();

            String requestBody = this.readInputStreamContents(request.getInputStream());
            resource.loadFromXml(requestBody, false);

            Session session = HibernateUtil.getSessionFactory().getCurrentSession();
            Transaction transaction = session.beginTransaction();
            Long id = (Long)session.save(resource);
            resource = (Resource)session.get(resourceClass, id);

            transaction.commit();

            writeResponse(response, HttpServletResponse.SC_CREATED, XML_TAG + Marshaller.marshal(resource), "application/xml");
        }
        catch(ClassNotFoundException e)
        {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        catch(IllegalAccessException e)
        {
            e.printStackTrace();
            writeResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                          "IllegalAccessException, the " + "Resource's default constructor is probably not accessible",
                          "text/plain");
            return;
        }
        catch(InstantiationException e)
        {
            e.printStackTrace();
            writeResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                          "InstantiationException, the "
                              + "Resource probably doesn't have a public default constructor", "text/plain");
            return;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            writeResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error reading request body",
                          "text/plain");
            return;
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

    private String readInputStreamContents(InputStream inputStream) throws IOException
    {
        StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer);
        return writer.toString();
    }

    private class UriResourceAttributes
    {
        private String name;
        private String id;
    }
}
