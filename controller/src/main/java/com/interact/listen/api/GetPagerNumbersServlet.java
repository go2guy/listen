package com.interact.listen.api;

import com.interact.listen.OutputBufferFilter;
import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GetPagerNumbersServlet extends HttpServlet
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
        statSender.send(Stat.META_API_GET_PAGER_NUMBERS);

        String pagerNumber = Configuration.get(Property.Key.PAGER_NUMBER);
        String alternateNumber = Configuration.get(Property.Key.ALTERNATE_NUMBER);
        
        String numbers = pagerNumber + (alternateNumber.equals("") ? "" : ",") + alternateNumber;

        response.setStatus(HttpServletResponse.SC_OK);
        OutputBufferFilter.append(request, numbers, "text/plain");
    }
}
