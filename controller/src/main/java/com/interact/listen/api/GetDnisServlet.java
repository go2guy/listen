package com.interact.listen.api;

import com.interact.listen.OutputBufferFilter;
import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.ListenServletException;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;
import com.interact.listen.util.WildcardNumberMatcher;
import com.interact.listen.util.WildcardNumberMatcherImpl;

import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GetDnisServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        StatSender statSender = (StatSender)request.getSession().getServletContext().getAttribute("statSender");
        if(statSender == null)
        {
            statSender = new InsaStatSender();
        }
        statSender.send(Stat.META_API_GET_DNIS);

        String number = request.getParameter("number");
        if(number == null || number.trim().equals(""))
        {
            throw new BadRequestServletException("Please provide a number");
        }

        String configuration = Configuration.get(Property.Key.DNIS_MAPPING);
        Map<String, String> mappings = dnisConfigurationToMap(configuration);
        WildcardNumberMatcher numberMatcher = new WildcardNumberMatcherImpl();
        String mapping = numberMatcher.findMatch(number, mappings);
        if(mapping == null)
        {
            throw new ListenServletException(HttpServletResponse.SC_NOT_FOUND);
        }

        response.setStatus(HttpServletResponse.SC_OK);
        OutputBufferFilter.append(request, mapping, "text/plain");
    }

    public static Map<String, String> dnisConfigurationToMap(String configurationValue)
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

    public static List<String> dnisConfigurationKeys(String configurationValue)
    {
        List<String> list = new ArrayList<String>();
        if(configurationValue == null || configurationValue.trim().equals(""))
        {
            return list;
        }
        for(String mapping : configurationValue.split(";"))
        {
            if(mapping.length() > 0)
            {
                String[] pair = mapping.split(":");
                list.add(pair[0]);
            }
        }
        return list;
    }
    
    public static List<String> getMappingByType(String typeOfDnis)
    {
        List<String> list = new ArrayList<String>();
        String dnisMappings = Configuration.get(Property.Key.DNIS_MAPPING);
        
        if(dnisMappings == null || dnisMappings.trim().equals(""))
        {
            return list;
        }
        
        for(String mapping : dnisMappings.split(";"))
        {
            if(mapping.length() > 0)
            {
                String[] pair = mapping.split(":");
                if(pair[1].equals(typeOfDnis))
                {
                    list.add(pair[0]);
                }
            }
        }
        
        return list;
    }
}
