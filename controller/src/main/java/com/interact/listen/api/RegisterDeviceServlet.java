package com.interact.listen.api;

import com.interact.listen.*;
import com.interact.listen.api.security.AuthenticationFilter;
import com.interact.listen.api.security.AuthenticationFilter.Authentication;
import com.interact.listen.c2dm.C2DMessaging;
import com.interact.listen.config.GoogleAuth;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.history.Channel;
import com.interact.listen.marshal.MalformedContentException;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.MarshallerNotFoundException;
import com.interact.listen.marshal.xml.XmlMarshaller;
import com.interact.listen.resource.DeviceRegistration;
import com.interact.listen.resource.DeviceRegistration.DeviceType;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.stats.Stat;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.hibernate.Session;

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
        json.append("\"").append("registrationToken").append("\":\"").append(regId).append("\"}");

        response.setStatus(HttpServletResponse.SC_OK);
        
        OutputBufferFilter.append(request, json.toString(), "application/json");
    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        ServletUtil.sendStat(request, Stat.META_API_PUT_DEVICE_REGISTER);
        
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        PersistenceService persistenceService = getPersistenceService(session, request);

        LOG.info("updating registration for " + persistenceService.getCurrentSubscriber());

        DeviceRegistration receivedDeviceReg = new DeviceRegistration();
        
        Marshaller marshaller = getMarshaller(request.getHeader("Content-Type"));
        try
        {
            marshaller.unmarshal(request.getInputStream(), receivedDeviceReg, false);
        }
        catch(MalformedContentException e)
        {
            LOG.error("unable to unmarshal request", e);
            throw new BadRequestServletException("invalid registration data");
        }

        receivedDeviceReg.setSubscriber(persistenceService.getCurrentSubscriber());

        DeviceRegistration currentReg = DeviceRegistration.queryByInfo(session, receivedDeviceReg);

        if(receivedDeviceReg.getRegistrationToken() != null && receivedDeviceReg.getRegistrationToken().length() > 0)
        {
            ServletUtil.sendStat(request, Stat.C2DM_REGISTERED_DEVICE);
            if(currentReg == null)
            {
                currentReg = receivedDeviceReg;
                LOG.info("Saving new registration for device " + currentReg.getDeviceId());
                session.save(currentReg);
            }
            else
            {
                currentReg.setRegistrationToken(receivedDeviceReg.getRegistrationToken());
                LOG.info("Updating registration for device " + currentReg.getDeviceId());
                session.update(currentReg);
            }
        }
        else if(currentReg != null)
        {
            ServletUtil.sendStat(request, Stat.C2DM_UNREGISTERED_DEVICE);
            LOG.info("Deleting registration for device " + currentReg.getDeviceId());
            session.delete(currentReg);
        }
        else
        {
            LOG.info("Registration for device not found to delete " + receivedDeviceReg.getDeviceId());
        }
    }

    private Marshaller getMarshaller(String contentType)
    {
        try
        {
            LOG.debug("Creating Marshaller for 'Accept' content type of " + contentType);
            return Marshaller.createMarshaller(contentType);
        }
        catch(MarshallerNotFoundException e)
        {
            LOG.warn("Unrecognized content-type provided, assuming XML");
            return new XmlMarshaller();
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
