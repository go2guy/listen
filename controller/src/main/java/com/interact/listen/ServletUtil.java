package com.interact.listen;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class ServletUtil
{
    private ServletUtil()
    {
        throw new AssertionError("Cannot instantiate utility class ServletUtil");
    }

    public static void writeResponse(HttpServletResponse response, int statusCode, String content, String contentType)
    {
        response.setStatus(statusCode);
        response.setContentType(contentType);
        response.setContentLength(content.length());

        System.out.println("Writing response content [ " + content + " ], type = [" + contentType + "], length = [" +
                           content.length() + "]");

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

    public static Map<String, String> getQueryParameters(HttpServletRequest request)
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

    public static void forward(String to, HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        System.out.println("Forwarding to [" + to + "]");
        request.getRequestDispatcher(to).forward(request, response);
    }

    public static void redirect(String to, HttpServletResponse response) throws IOException
    {
        System.out.println("Redirecting to [" + to + "]");
        response.sendRedirect(to);
    }
}
