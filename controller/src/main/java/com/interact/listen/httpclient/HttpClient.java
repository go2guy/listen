package com.interact.listen.httpclient;

import java.io.IOException;
import java.util.Map;

/**
 * Interface representing a simple HTTP client for connecting to HTTP servers. Implementations should be stateful,
 * maintaining the response status and content (if available) after the transaction is complete.
 */
public interface HttpClient
{
    // Not needed yet:
    // public void put(String uri, String entityContent) throws IOException;
    // public void delete(String uri) throws IOException;

    /**
     * Sets the socket timeout for this client.
     */
    public void setSocketTimeout(int millis);

    public void get(String uri) throws IOException;
    
    /**
     * Makes an HTTP POST request to the provided URI with the provided parameters.
     * 
     * @param uri URI to send request to
     * @param request parameters to be sent in request body
     * @throws IOException if an error occurs performing the HTTP request
     */
    public void post(String uri, Map<String, String> params) throws IOException;

    /**
     * Makes an HTTP POST request to the provided URI with the provided content and content type.
     * 
     * @param uri URI to send request to
     * @param contentType Content-Type of the provided entity
     * @param entityContent entity to send in request body
     * @throws IOException if an error occurs performing the HTTP request
     */
    public void post(String uri, String contentType, String entityContent) throws IOException;

    /**
     * Retrieves the response status from the most recent HTTP request performed by this client instance.
     * 
     * @return HTTP response status
     */
    public int getResponseStatus();

    /**
     * Retrieves the response content from the most recent HTTP request performed by this client instance.
     * 
     * @return response content
     */
    public String getResponseEntity();
}
