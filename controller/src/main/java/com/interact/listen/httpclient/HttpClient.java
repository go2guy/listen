package com.interact.listen.httpclient;

import java.io.IOException;
import java.util.Map;

public interface HttpClient
{
    // Not needed yet:

    // public void get(String uri) throws IOException;
    // public void put(String uri, String entityContent) throws IOException;
    // public void delete(String uri) throws IOException;

    public void post(String uri, Map<String, String> params) throws IOException;

    public void post(String uri, String entityContent) throws IOException;

    public Integer getResponseStatus();

    public String getResponseEntity();
}
