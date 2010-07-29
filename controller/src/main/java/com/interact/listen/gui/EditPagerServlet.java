package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.PersistenceService;
import com.interact.listen.ServletUtil;
import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;
import com.interact.listen.config.Property.Key;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.history.Channel;
import com.interact.listen.history.HistoryService;
import com.interact.listen.httpclient.HttpClient;
import com.interact.listen.httpclient.HttpClientImpl;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.stats.InsaStatSender;
import com.interact.listen.stats.Stat;
import com.interact.listen.stats.StatSender;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.Session;

public class EditPagerServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(EditPagerServlet.class);

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        StatSender statSender = (StatSender)request.getSession().getServletContext().getAttribute("statSender");
        if(statSender == null)
        {
            statSender = new InsaStatSender();
        }
        statSender.send(Stat.GUI_EDIT_PAGER);

        Subscriber currentSubscriber = ServletUtil.currentSubscriber(request);
        if(currentSubscriber == null)
        {
            throw new UnauthorizedServletException();
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        PersistenceService persistenceService = new PersistenceService(session, currentSubscriber, Channel.GUI);
        HistoryService historyService = new HistoryService(persistenceService);

        String alternateNumber = request.getParameter("alternateNumber");

        if(alternateNumber == null)
        {
            alternateNumber = "";
        }

        alternateNumber = alternateNumber.replaceAll("[-\\.]", "");

        if(alternateNumber.length() > 10)
        {
            throw new BadRequestServletException("Please provide an Alternate Number with ten digits or less");
        }

        if(!alternateNumber.matches("^[0-9]*$"))
        {
            throw new BadRequestServletException("Alternate number can only contain digits 0-9");
        }

        String originalAlternateNumber = Configuration.get(Key.ALTERNATE_NUMBER);
        String newAlternateNumber = alternateNumber;
        Configuration.set(Property.Key.ALTERNATE_NUMBER, alternateNumber);
        updateRealizeAlert(Configuration.get(Key.REALIZE_URL), Configuration.get(Key.REALIZE_ALERT_NAME),
                           originalAlternateNumber, newAlternateNumber);

        historyService.writeChangedAlternatePagerNumber(alternateNumber);
    }

    public static void updateRealizeAlert(String realizeUrl, String realizeAlertName, String originalAlternateNumber, String newAlternateNumber)
    {
        LOG.debug("Sending alternate number update to Realize; url = [" + realizeUrl + "], alert name = [" +
                  realizeAlertName + "], current alternate number = [" + originalAlternateNumber +
                  "], new alternate number = [" + newAlternateNumber + "]");

        if(realizeUrl != null && realizeAlertName != null)
        {
            String uri = realizeUrl;
            if(!uri.endsWith("/"))
            {
                uri += "/";
            }
            uri += "alert/replaceEmailAddress";
            HttpClient client = new HttpClientImpl();

            // if the Realize system can't connect to its ActiveMQ instance, it'll hang indefinitely;
            // this is about the best we can do (time out and hope it works)
            client.setSocketTimeout(3000);

            Map<String, String> params = new HashMap<String, String>();
            params.put("name", realizeAlertName);
            if(originalAlternateNumber != null && !originalAlternateNumber.trim().equals(""))
            {
                params.put("remove", originalAlternateNumber);
            }
            if(newAlternateNumber != null && !newAlternateNumber.trim().equals(""))
            {
                params.put("add", newAlternateNumber);
            }

            try
            {
                client.post(uri, params);
            }
            catch(IOException e)
            {
                LOG.warn(e);
            }
        }
    }
}
