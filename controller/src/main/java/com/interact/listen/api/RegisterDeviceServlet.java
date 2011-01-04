package com.interact.listen.api;

import com.interact.listen.*;
import com.interact.listen.api.security.AuthenticationFilter;
import com.interact.listen.api.security.AuthenticationFilter.Authentication;
import com.interact.listen.c2dm.C2DMessaging;
import com.interact.listen.config.GoogleAuth;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.history.Channel;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.resource.*;
import com.interact.listen.resource.DeviceRegistration.DeviceType;
import com.interact.listen.resource.DeviceRegistration.RegisteredType;
import com.interact.listen.stats.Stat;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class RegisterDeviceServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(RegisterDeviceServlet.class);

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        ServletUtil.sendStat(request, Stat.META_API_GET_DEVICE_REGISTER);
        
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        PersistenceService service = getPersistenceService(session, request);

        DeviceRegistration qReg = new DeviceRegistration();
        qReg.setSubscriber(service.getCurrentSubscriber());

        String dType = request.getParameter("deviceType");
        if(dType != null && dType.length() > 0)
        {
            try
            {
                qReg.setDeviceType(DeviceType.valueOf(dType));
            }
            catch(IllegalArgumentException e)
            {
                LOG.error("unknown device type '" + dType + "'");
                throw new BadRequestServletException("device type unknown");
            }
        }
        
        qReg.setDeviceId(request.getParameter("deviceId"));
        if(qReg.getDeviceId() == null || qReg.getDeviceId().length() == 0)
        {
            throw new BadRequestServletException("deviceId not provided");
        }
        
        boolean enabled = C2DMessaging.INSTANCE.isEnabled();
        String username = GoogleAuth.INSTANCE.getUsername();

        DeviceRegistration reg = DeviceRegistration.queryByInfo(session, qReg);
        
        String regId = reg == null || reg.getRegistrationToken() == null ? "" : reg.getRegistrationToken();

        LOG.info("getting C2D information enabled: " + enabled + " account: '" + username + "' reg: '" + regId + "'");

        StringBuilder json = new StringBuilder();
        json.append("{\"").append("enabled").append("\":\"").append(enabled).append("\",");
        json.append("\"").append("account").append("\":\"").append(username).append("\",");
        json.append("\"").append("registrationToken").append("\":\"").append(regId).append("\",");
        json.append("\"").append("registeredTypes").append("\":[");
        
        if(reg != null)
        {
            for(RegisteredType rType : reg.getRegisteredTypes())
            {
                json.append('\"').append(rType.name()).append("\",");
            }
            if(!reg.getRegisteredTypes().isEmpty())
            {
                json.deleteCharAt(json.length() - 1);
            }
            response.setStatus(HttpServletResponse.SC_OK);
        }

        json.append("]}");
        
        OutputBufferFilter.append(request, json.toString(), "application/json");
    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        ServletUtil.sendStat(request, Stat.META_API_PUT_DEVICE_REGISTER);
        
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        PersistenceService persistenceService = getPersistenceService(session, request);

        String requestedContentType = request.getHeader("Content-Type");
        if(requestedContentType != null && !requestedContentType.equalsIgnoreCase("application/json"))
        {
            throw new BadRequestServletException("requested content type not supported");
        }
        
        LOG.info("updating registration for " + persistenceService.getCurrentSubscriber());

        DeviceRegistration currentReg = null;
        DeviceRegistration receivedDeviceReg = new DeviceRegistration();
        String regToken = null;
        Set<RegisteredType> enableTypes = new TreeSet<RegisteredType>();
        Set<RegisteredType> disableTypes = new TreeSet<RegisteredType>();
        
        String input = IOUtils.toString(request.getInputStream());
        LOG.debug("Received JSON for unmarshalling: <<" + input + ">>");

        JSONParser parser = new JSONParser();
        try
        {
            JSONObject json = (JSONObject)parser.parse(input);
            String deviceId = (String)json.get("deviceId");
            regToken = (String)json.get("registrationToken");
            DeviceType dType = DeviceType.ANDROID;
            if(json.containsKey("deviceType"))
            {
                try
                {
                    dType = DeviceType.valueOf((String)json.get("deviceType"));
                }
                catch(Exception e)
                {
                    LOG.error("unknown device type", e);
                }
            }

            if(deviceId == null)
            {
                throw new BadRequestServletException("device ID required");
            }

            if(!json.containsKey("registerTypes") && !json.containsKey("unregisterTypes"))
            {
                // legacy
                if(regToken == null)
                {
                    throw new BadRequestServletException("registration token required if not adjusting types");
                }
                if(regToken.length() == 0)
                {
                    disableTypes.add(RegisteredType.VOICEMAIL);
                }
                else
                {
                    enableTypes.add(RegisteredType.VOICEMAIL);
                }
            }
            else
            {
                addRegisteredTypesToSet(enableTypes, json, "registerTypes");
                addRegisteredTypesToSet(disableTypes, json, "unregisterTypes");
            }
            
            receivedDeviceReg.setSubscriber(persistenceService.getCurrentSubscriber());
            receivedDeviceReg.setDeviceId(deviceId);
            receivedDeviceReg.setDeviceType(dType);
            receivedDeviceReg.setRegistrationToken(regToken == null ? "" : regToken);
            
            currentReg = DeviceRegistration.queryByInfo(session, receivedDeviceReg);

        }
        catch(ParseException e)
        {
            throw new BadRequestServletException("unable to parse JSON");
        }

        if(currentReg == null)
        {
            LOG.debug("device " + receivedDeviceReg.getDeviceId() + " not found in registration table");
            currentReg = receivedDeviceReg;
        }

        boolean isRegChange = false;
        if(regToken != null)
        {
            if(regToken.length() > 0)
            {
                if(currentReg == receivedDeviceReg)
                {
                    isRegChange = true;
                    LOG.info("Saving new registration for device " + currentReg.getDeviceId());
                }
                else if(!regToken.equals(currentReg.getRegistrationToken()))
                {
                    isRegChange = true;
                    currentReg.setRegistrationToken(receivedDeviceReg.getRegistrationToken());
                    LOG.info("Updating registration for device " + currentReg.getDeviceId());
                }
            }
            else if(currentReg == receivedDeviceReg)
            {
                isRegChange = true;
                LOG.info("Registration for device not found to delete " + receivedDeviceReg.getDeviceId());
            }
            else
            {
                isRegChange = true;
                currentReg.setRegistrationToken(regToken);
                LOG.info("Deleting registration for device " + currentReg.getDeviceId());
            }
        }
        
        enableTypes.removeAll(disableTypes);
        
        boolean isTypeChange = false;
        if(currentReg.getRegisteredTypes().addAll(enableTypes))
        {
            isTypeChange = true;
        }
        if(currentReg.getRegisteredTypes().removeAll(disableTypes))
        {
            isTypeChange = true;
        }

        if(isRegChange || isTypeChange)
        {
            if(currentReg.getRegistrationToken().length() > 0)
            {
                if(isRegChange)
                {
                    ServletUtil.sendStat(request,  Stat.C2DM_REGISTERED_DEVICE);
                }
                if(isTypeChange)
                {
                    LOG.debug("registered types: " + currentReg.getRegisteredTypes());
                }
                if(currentReg == receivedDeviceReg)
                {
                    session.save(currentReg);
                }
                else
                {
                    session.update(currentReg);
                }
            }
            else if(currentReg != receivedDeviceReg)
            {
                ServletUtil.sendStat(request, Stat.C2DM_UNREGISTERED_DEVICE);
                session.delete(currentReg);
            }
        }
        
    }

    private static void addRegisteredTypesToSet(Set<RegisteredType> types, JSONObject json, String key)
    {
        JSONArray rArray = (JSONArray)json.get(key);
        if(rArray == null)
        {
            return;
        }
        for(int i = 0; i < rArray.size(); ++i)
        {
            String rStr = (String)rArray.get(i);
            try
            {
                RegisteredType rType = RegisteredType.valueOf(rStr);
                types.add(rType);
                LOG.debug(key + "=>" + rType);
            }
            catch(Exception e)
            {
                LOG.error("unable to determine registered type", e);
            }
        }
    }
    
    private static PersistenceService getPersistenceService(Session session, HttpServletRequest request) throws UnauthorizedServletException
    {
        Subscriber subscriber = null;

        Authentication auth = (Authentication)request.getAttribute(AuthenticationFilter.AUTHENTICATION_KEY);
        if(auth != null)
        {
            if(auth.getType() == AuthenticationFilter.AuthenticationType.SUBSCRIBER && auth.getSubscriber() != null)
            {
                subscriber = auth.getSubscriber();
            }
            if(subscriber == null)
            {
                throw new UnauthorizedServletException("Unathorized Subscriber");
            }
        }
        else
        {
            String subHeader = request.getHeader("X-Listen-Subscriber");
            if(subHeader == null)
            {
                subHeader = request.getHeader("X-Listen-AuthenticationUsername");
                if(subHeader != null)
                {
                    subHeader = new String(Base64.decodeBase64(subHeader));
                    subscriber = Subscriber.queryByUsername(session, subHeader);
                }
                if(subscriber == null)
                {
                    throw new UnauthorizedServletException("Unable to get subscriber from encoded header [" + subHeader + "]");
                }
            }
            else
            {
                Long id = Marshaller.getIdFromHref(subHeader);
                if(id != null)
                {
                    subscriber = Subscriber.queryById(session, id);
                }
                if(subscriber == null)
                {
                    throw new UnauthorizedServletException("X-Listen-Subscriber HTTP header contained unknown subscriber href [" + subHeader + "]");
                }
            }
        }
        
        Channel channel = (Channel)request.getAttribute(RequestInformationFilter.CHANNEL_KEY);
        return new DefaultPersistenceService(session, subscriber, channel);
    }
}
