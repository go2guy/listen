package com.interact.listen.gui;

import com.interact.listen.*;
import com.interact.listen.exception.*;
import com.interact.listen.history.Channel;
import com.interact.listen.resource.AccessNumber;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.stats.Stat;

import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class MySetSubscriberPhoneNumberSettingsServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(MySetSubscriberPhoneNumberSettingsServlet.class);

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        ServletUtil.sendStat(request, Stat.GUI_EDIT_SUBSCRIBER);
        Subscriber subscriber = ServletUtil.requireCurrentSubscriber(request, false);

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        PersistenceService ps = new DefaultPersistenceService(session, subscriber, Channel.GUI);

        List<AccessNumber> newNumbers = new ArrayList<AccessNumber>();
        Map<String, AccessNumber> existingNumbers = new HashMap<String, AccessNumber>();
        for(AccessNumber accessNumber : AccessNumber.queryBySubscriber(session, subscriber))
        {
            existingNumbers.put(accessNumber.getNumber(), accessNumber);
        }

        JSONArray numbers = (JSONArray)JSONValue.parse(request.getParameter("numbers"));
        for(JSONObject number : (List<JSONObject>)numbers)
        {
            AccessNumber newNumber = new AccessNumber();
            newNumber.setNumber((String)number.get("number"));
            newNumber.setSupportsMessageLight((Boolean)number.get("messageLight"));
            newNumber.setPublicNumber((Boolean)number.get("publicNumber"));
            try
            {
                newNumber.setNumberType(AccessNumber.NumberType.valueOf((String)number.get("category")));
            }
            catch(IllegalArgumentException e)
            {
                LOG.error("Unknown number type " + number.get("category"));
            }
            if(!newNumber.getNumberType().isSystem() && newNumber.getSupportsMessageLight().booleanValue())
            {
                LOG.error("Non system number " + newNumber.getNumber() + " - " + newNumber.getNumberType() + " can not support message light");
                newNumber.setSupportsMessageLight(false);
            }
            newNumbers.add(newNumber);
        }

        try
        {
            subscriber.updateAccessNumbers(session, ps, newNumbers, false);
        }
        catch(NumberAlreadyInUseException e)
        {
            throw new BadRequestServletException("Access number [" + e.getNumber() +
                                                 "] is already in use by another account");
        }
        catch(UnauthorizedModificationException e)
        {
            LOG.error(e);
            throw new UnauthorizedServletException();
        }
    }
}
