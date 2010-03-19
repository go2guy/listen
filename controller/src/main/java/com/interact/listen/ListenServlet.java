package com.interact.listen;

import com.interact.listen.resource.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ListenServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

    static
    {
        Bootstrap.init(); // TODO remove later
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        UriResourceAttributes attributes = getResourceAttributes(request);

        if(attributes.name == null)
        {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            // TODO content
            return;
        }

        try
        {
            if(attributes.name.equalsIgnoreCase("subscribers"))
            {
                if(attributes.id != null)
                {
                    Subscriber subscriber = Subscriber.get(Long.valueOf(attributes.id));
                    if(subscriber != null)
                    {
                        response.setStatus(HttpServletResponse.SC_OK);
                        sendXmlResponse(subscriber, response);
                    }
                    else
                    {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        response.setContentLength(0);
                    }
                }
                else
                {
                    SubscriberList subscriberList = new SubscriberList(new ArrayList<Subscriber>(0));

                    if(request.getQueryString() != null && request.getQueryString().length() > 0)
                    {
                        Map<String, List<String>> queryParameters = parseQueryString(request.getQueryString());

                        if(queryParameters.containsKey("number"))
                        {
                            List<Subscriber> list = Subscriber.find(queryParameters.get("number").get(0));
                            subscriberList = new SubscriberList(list);
                        }

                        subscriberList.setHref("/subscribers?" + request.getQueryString());
                    }
                    else
                    {
                        List<Subscriber> list = Subscriber.list();
                        subscriberList = new SubscriberList(list);
                    }
                    
                    response.setStatus(HttpServletResponse.SC_OK);
                    sendXmlResponse(subscriberList, response);
                }
            }
            else if(attributes.name.equalsIgnoreCase("voicemails"))
            {
                if(attributes.id != null)
                {
                    Voicemail voicemail = Voicemail.get(Long.valueOf(attributes.id));
                    if(voicemail != null)
                    {
                        response.setStatus(HttpServletResponse.SC_OK);
                        sendXmlResponse(voicemail, response);
                    }
                    else
                    {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        response.setContentLength(0);
                    }
                }
                else
                {
                    List<Voicemail> list = Voicemail.list();
                    VoicemailList voicemailList = new VoicemailList(list);

                    response.setStatus(HttpServletResponse.SC_OK);
                    sendXmlResponse(voicemailList, response);
                }
            }
        }
        catch(SQLException e)
        {
            e.printStackTrace();

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            // TODO content
            return;
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
    {
        UriResourceAttributes attributes = getResourceAttributes(request);

        if(attributes.name == null)
        {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            // TODO content
            return;
        }

        if(attributes.name.equalsIgnoreCase("voicemails"))
        {
            if(attributes.id != null)
            {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                // TODO content
                return;
            }

            try
            {
                Voicemail voicemail = Voicemail.fromXml(request.getInputStream());
                voicemail.setId(null);

                String href = voicemail.getSubscriber().getHref();
                Subscriber subscriber = Subscriber.get(Long.parseLong(href.substring(href.lastIndexOf("/") + 1)));
                voicemail.setSubscriber(subscriber);

                voicemail.save();

                response.setStatus(HttpServletResponse.SC_OK);
                sendXmlResponse(voicemail, response);
            }
            catch(IOException e)
            {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                // TODO content
                return;
            }
            catch(SQLException e)
            {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                // TODO content
                return;
            }
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

        System.out.println("path info: [" + pathInfo + "]");

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

        System.out.println("name: " + attributes.name);
        System.out.println("id:   " + attributes.id);

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

    private void sendXmlResponse(Resource resource, HttpServletResponse response)
    {
        String xml = resource.toXml();
        response.setContentType("application/xml");

        try
        {
            PrintWriter writer = response.getWriter();
            if(xml.length() > 0)
            {
                writer.print(XML_HEADER);
                writer.print(xml);

                response.setContentLength(XML_HEADER.length() + xml.length());
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            // TODO content
            return;
        }
    }

    private Map<String, List<String>> parseQueryString(String queryString)
    {
        Map<String, List<String>> params = new HashMap<String, List<String>>();
        for(String param : queryString.split("&"))
        {
            String[] pair = param.split("=");

            try
            {
                String key = URLDecoder.decode(pair[0], "UTF-8");
                String value = URLDecoder.decode(pair[1], "UTF-8");
                List<String> values = params.get(key);
                if(values == null)
                {
                    values = new ArrayList<String>();
                    params.put(key, values);
                }
                values.add(value);
            }
            catch(UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
        }

        return params;
    }

    private class UriResourceAttributes
    {
        private String name;
        private String id;
    }
}
