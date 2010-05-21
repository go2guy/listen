package com.interact.listen.api;

import com.interact.listen.ServletUtil;
import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GetDnisServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        String number = request.getParameter("number");
        if(number == null || number.trim().equals(""))
        {
            ServletUtil.writeResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Please provide a number",
                                      "text/plain");
            return;
        }

        String configuration = Configuration.get(Property.Key.DNIS_MAPPING);
        Map<String, String> mappings = dnisConfigurationToMap(configuration);
        if(mappings.containsKey(number))
        {
            ServletUtil.writeResponse(response, HttpServletResponse.SC_OK, mappings.get(number), "text/plain");
            return;
        }

        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    private Map<String, String> dnisConfigurationToMap(String configurationValue)
    {
        Map<String, String> map = new HashMap<String, String>();
        if(configurationValue == null || configurationValue.trim().equals(""))
        {
            return map;
        }
        for(String mapping : configurationValue.split(";"))
        {
            if(mapping.length() > 0)
            {
                String[] pair = mapping.split(":");
                map.put(pair[0], pair[1]);
            }
        }
        return map;
    }
}
