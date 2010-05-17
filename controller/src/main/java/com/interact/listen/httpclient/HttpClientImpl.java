package com.interact.listen.httpclient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

/**
 * Basic HTTP client implementation.
 * <p>
 * Developer note: Only supports POST requests, as those are all that are currently needed to make requests to the IVR.
 */
public class HttpClientImpl implements HttpClient
{
    private static final Logger LOG = Logger.getLogger(HttpClientImpl.class);
    private boolean requestMade = false;
    private int responseStatus;
    private String responseEntity;

    @Override
    public void post(String uri, Map<String, String> params) throws IOException
    {
        post(uri, "application/x-www-form-urlencoded", buildQueryString(params));
    }

    @Override
    public void post(String uri, String contentType, String entityContent) throws IOException
    {
        HttpPost request = new HttpPost(uri);
        request.setHeader("Content-Type", contentType);
        performEntityEnclosingRequest(request, entityContent);
    }

    @Override
    public int getResponseStatus()
    {
        if(!requestMade)
        {
            throw new IllegalStateException("You must make a request before getting the response status");
        }
        return responseStatus;
    }

    @Override
    public String getResponseEntity()
    {
        if(!requestMade)
        {
            throw new IllegalStateException("You must make a request before getting the response entity");
        }
        return responseEntity;
    }

    private void performEntityEnclosingRequest(HttpEntityEnclosingRequestBase request, String entityContent)
        throws IOException
    {
        LOG.debug("Making HTTP " + request.getMethod() + " request to " + request.getURI() + " with entity [" +
                  entityContent + "]");

        try
        {
            HttpEntity entity = new StringEntity(entityContent, "UTF-8");
            request.setEntity(entity);
            performRequest(request);
        }
        catch(UnsupportedEncodingException e)
        {
            throw new AssertionError(e);
        }
    }

    private void performRequest(HttpRequestBase request) throws IOException
    {
        org.apache.http.client.HttpClient client = new DefaultHttpClient();
        HttpContext context = new BasicHttpContext();

        requestMade = true;
        HttpResponse response = client.execute(request, context);

        this.responseStatus = response.getStatusLine().getStatusCode();
        this.responseEntity = getEntity(response);

        LOG.debug("Received " + responseStatus + " response with entity [" + responseEntity + "]");
    }

    private static String getEntity(HttpResponse response) throws IOException
    {
        HttpEntity entity = response.getEntity();
        if(entity == null)
        {
            return null;
        }

        String entityString = EntityUtils.toString(entity);
        entity.consumeContent(); // reads any remaining content from the stream (avoids leaving stuff on the stream)
        return entityString;
    }

    private static String buildQueryString(Map<String, String> params)
    {
        StringBuilder builder = new StringBuilder();
        try
        {
            for(Map.Entry<String, String> entry : params.entrySet())
            {
                builder.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                builder.append("=");
                builder.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                builder.append("&");
            }
            if(params.size() > 0)
            {
                builder.deleteCharAt(builder.length() - 1); // delete last '&'
            }
        }
        catch(UnsupportedEncodingException e)
        {
            throw new AssertionError(e);
        }
        return builder.toString();
    }
}
