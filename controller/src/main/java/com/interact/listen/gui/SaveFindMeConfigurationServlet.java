package com.interact.listen.gui;

import com.interact.listen.*;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.history.Channel;
import com.interact.listen.license.License;
import com.interact.listen.license.ListenFeature;
import com.interact.listen.license.NotLicensedException;
import com.interact.listen.resource.FindMeNumber;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.stats.Stat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class SaveFindMeConfigurationServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        if(!License.isLicensed(ListenFeature.FINDME))
        {
            throw new NotLicensedException(ListenFeature.FINDME);
        }

        ServletUtil.sendStat(request, Stat.GUI_SAVE_FINDME_CONFIGURATION);

        Subscriber subscriber = ServletUtil.currentSubscriber(request);
        if(subscriber == null)
        {
            throw new UnauthorizedServletException("Not logged in");
        }

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        PersistenceService persistenceService = new DefaultPersistenceService(session, subscriber, Channel.GUI);

        // save expiration date & reminder settings
        String expires = request.getParameter("expires");
        SimpleDateFormat format = new SimpleDateFormat("EEE, MMMM d, yyyy 'at' K:mm aa");
        try
        {
            Subscriber original = subscriber.copy(false);

            Date expirationDate = format.parse(expires);
            subscriber.setFindMeExpiration(expirationDate);

            Boolean reminder = ServletUtil.getNotNullBoolean("reminder", request, "reminder");
            String destination = ServletUtil.getNotNullString("reminderDestination", request, "reminderDestination");
            subscriber.setSendFindMeReminder(reminder);
            subscriber.setFindMeReminderDestination(destination);

            persistenceService.update(subscriber, original);
        }
        catch(ParseException e)
        {
            throw new BadRequestServletException("Expiration date was not formatted correctly");
        }

        // save numbers
        FindMeNumber.deleteBySubscriber(session, subscriber);

        JSONArray groups = (JSONArray)JSONValue.parse(request.getParameter("findme"));
        int priority = 0;
        for(JSONArray group : (List<JSONArray>)groups)
        {
            for(JSONObject dial : (List<JSONObject>)group)
            {
                FindMeNumber fmn = new FindMeNumber();
                fmn.setDialDuration(Integer.valueOf((String)dial.get("duration")));
                fmn.setEnabled((Boolean)dial.get("enabled"));
                fmn.setNumber((String)dial.get("number"));
                fmn.setSubscriber(subscriber);
                fmn.setPriority(priority);
                persistenceService.save(fmn);
            }
            ++priority;
        }
    }
}
