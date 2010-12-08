package com.interact.listen.gui;

import com.interact.listen.HibernateUtil;
import com.interact.listen.ServletUtil;
import com.interact.listen.c2dm.C2DMessaging;
import com.interact.listen.config.GoogleAuth;
import com.interact.listen.config.Property;
import com.interact.listen.exception.BadRequestServletException;
import com.interact.listen.exception.UnauthorizedServletException;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.DeviceRegistration.DeviceType;
import com.interact.listen.stats.Stat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.Session;

public class SetGoogleAuthServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(SetGoogleAuthServlet.class);

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException
    {
        ServletUtil.sendStat(request, Stat.GUI_GOOGLE_AUTH_POST);

        Subscriber currentSubscriber = ServletUtil.currentSubscriber(request);
        if(currentSubscriber == null)
        {
            throw new UnauthorizedServletException();
        }

        if(!currentSubscriber.getIsAdministrator())
        {
            throw new UnauthorizedServletException();
        }

        GoogleAuth ga = GoogleAuth.getInstance();

        boolean c2dmEnabled = Boolean.valueOf(request.getParameter(Property.Key.ANDROID_C2DM_ENABLED.getKey()));
        String username = getNonNullParameter(request, Property.Key.GOOGLE_AUTH_USER.getKey());
        String password = getNonNullParameter(request, "com.interact.listen.google.password");
        String token    = getNonNullParameter(request, Property.Key.GOOGLE_AUTH_TOKEN.getKey());

        LOG.debug("google auth servlet received: " + c2dmEnabled + ", " + username + ", " + password + ", " + token);
        
        boolean wasEnabled = C2DMessaging.isEnabled();
        String oldUsername = ga.getUsername();
        String oldToken    = ga.getToken();

        LOG.debug("google auth servlet current: " + wasEnabled + ", " + oldUsername + ", " + oldToken);

        ga.invalidateCachedToken(oldToken);
        
        if(c2dmEnabled)
        {
            if(username.length() == 0)
            {
                throw new BadRequestServletException("Google account required");
            }
    
            if(password.length() > 0)
            {
                LOG.info("setting google auth token by password for account " + username);
                ga.setByPassword(username, password);
            }
            else if(token.length() > 0)
            {
                LOG.info("setting google auth token for account " + username);
                ga.setByToken(username, token);
            }
            else
            {
                throw new BadRequestServletException("Google password or authorization token required");
            }
    
            if(ga.getToken().length() == 0)
            {
                throw new BadRequestServletException(ga.getLastError().getDescription());
            }
        }

        C2DMessaging.setEnabled(c2dmEnabled);

        String useToken = "";
        if(oldToken.length() > 0 && !username.equals(oldUsername))
        {
            // must us old token as that is how everybody is registered
            useToken = oldToken;
            LOG.info("Using old token to send sender ID and/or enabled changed: " + useToken);
        }
        else if(username.equals(oldUsername) && c2dmEnabled != wasEnabled)
        {
            // just toggled enabled, current token should work fine
            useToken = ga.getToken();
            LOG.info("Using new token to send enabled changed: " + useToken);
        }
        
        if(useToken.length() > 0)
        {
            Session session = HibernateUtil.getSessionFactory().getCurrentSession();
            C2DMessaging.enqueueConfigChanges(session, DeviceType.ANDROID, useToken);
        }
        else
        {
            LOG.info("no config change C2D messages queued");
        }
    }

    private static String getNonNullParameter(HttpServletRequest request, String key)
    {
        String value = request.getParameter(key);
        return value == null ? "" : value;
    }

}
