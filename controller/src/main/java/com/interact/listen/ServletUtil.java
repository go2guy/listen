package com.interact.listen;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public final class ServletUtil
{
    private static final Logger LOG = Logger.getLogger(ServletUtil.class);

    private ServletUtil()
    {
        throw new AssertionError("Cannot instantiate utility class ServletUtil");
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
                LOG.warn("Pair [" + Arrays.toString(pair) + "] had a length of one, skipping");
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
        LOG.debug("Forwarding to [" + to + "]");
        request.getRequestDispatcher(to).forward(request, response);
    }

    public static void redirect(String to, HttpServletResponse response) throws IOException
    {
        LOG.debug("Redirecting to [" + to + "]");
        response.sendRedirect(to);
    }
}
