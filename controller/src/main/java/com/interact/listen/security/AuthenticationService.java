package com.interact.listen.security;

import com.interact.listen.DefaultPersistenceService;
import com.interact.listen.PersistenceService;
import com.interact.listen.config.Configuration;
import com.interact.listen.config.Property;
import com.interact.listen.exception.NumberAlreadyInUseException;
import com.interact.listen.exception.UnauthorizedModificationException;
import com.interact.listen.history.Channel;
import com.interact.listen.resource.AccessNumber;
import com.interact.listen.resource.AccessNumber.NumberType;
import com.interact.listen.resource.Conference;
import com.interact.listen.resource.Subscriber;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;

public class AuthenticationService
{
    private static final Logger LOG = Logger.getLogger(AuthenticationService.class);

    public enum Realm
    {
        LOCAL, ACTIVE_DIRECTORY;
    }

    public enum Code
    {
        SUCCESS("Success"),
        INVALID_CREDENTIALS("Sorry, those aren't valid credentials"),
        PROTOCOL_ERROR("An error occurred logging in, please contact an Administrator");

        private final String message;

        private Code(String message)
        {
            this.message = message;
        }

        public String getMessage()
        {
            return message;
        }
    }

    public static final class Result
    {
        private Realm realm;
        private Code code;
        private Subscriber subscriber;

        public Result(Realm realm, Code code, Subscriber subscriber)
        {
            this.realm = realm;
            this.code = code;
            this.subscriber = subscriber;
        }

        public Realm getRealm()
        {
            return realm;
        }

        public Code getCode()
        {
            return code;
        }

        public Subscriber getSubscriber()
        {
            return subscriber;
        }

        public boolean wasSuccessful()
        {
            return code == Code.SUCCESS;
        }
    }

    public Result authenticate(Session session, String username, String password)
    {
        if(username == null || password == null)
        {
            throw new IllegalArgumentException("Username and/or password cannot be null");
        }

        Subscriber subscriber = Subscriber.queryByUsername(session, username);
        if(subscriber != null && !subscriber.getIsActiveDirectory())
        {
            if(!isValidLocalPassword(subscriber, password))
            {
                LOG.warn("Local Auth: Invalid credentials for [" + username + "] (invalid local password)");
                return new Result(Realm.LOCAL, Code.INVALID_CREDENTIALS, subscriber);
            }
            else
            {
                return new Result(Realm.LOCAL, Code.SUCCESS, subscriber);
            }
        }
        else if(!Boolean.valueOf(Configuration.get(Property.Key.ACTIVE_DIRECTORY_ENABLED)))
        {
            LOG.warn("Local Auth: Invalid credentials for [" + username + "] (subscriber = null, AD = disabled)");
            return new Result(Realm.LOCAL, Code.INVALID_CREDENTIALS, subscriber);
        }
        else
        {
            String server = Configuration.get(Property.Key.ACTIVE_DIRECTORY_SERVER);
            String domain = Configuration.get(Property.Key.ACTIVE_DIRECTORY_DOMAIN);
            ActiveDirectoryAuthenticator auth = new ActiveDirectoryAuthenticator(server, domain);

            try
            {
                AuthenticationResult result = auth.authenticate(username, password);
                if(!result.isSuccessful())
                {
                    LOG.warn("AD Auth: Invalid credentials for [" + username + "], (invalid AD password)");
                    return new Result(Realm.ACTIVE_DIRECTORY, Code.INVALID_CREDENTIALS, subscriber);
                }
                else
                {
                    LOG.debug("Login successful for Active Directory account [" + username + "]");
                    if(subscriber == null)
                    {
                        LOG.debug("Local Subscriber for AD user does not exist, creating");
                        subscriber = new Subscriber();
                        subscriber.setUsername(username);
                        subscriber.setIsActiveDirectory(true);
                        subscriber.setLastLogin(new Date());
                        subscriber.setRealName(result.getDisplayName());
                        subscriber.setWorkEmailAddress(result.getMail());

                        PersistenceService ps = new DefaultPersistenceService(session, subscriber, Channel.GUI);
                        ps.save(subscriber);

                        if(result.getTelephoneNumber() != null)
                        {
                            try
                            {
                                AccessNumber number = new AccessNumber();
                                number.setNumber(result.getTelephoneNumber());
                                number.setNumberType(NumberType.EXTENSION);
                                number.setPublicNumber(true);
                                number.setSupportsMessageLight(true);

                                List<AccessNumber> newNumbers = new ArrayList<AccessNumber>();
                                newNumbers.add(number);

                                subscriber.updateAccessNumbers(session, ps, newNumbers, true);
                            }
                            catch(NumberAlreadyInUseException e)
                            {
                                LOG.warn("When adding new AD Subscriber [" + username + "], accessNumber [" +
                                         e.getNumber() + "] was already in use by another Subscriber");
                            }
                            catch(UnauthorizedModificationException e)
                            {
                                LOG.warn("Admin received unathorized modification exception?", e);
                            }
                        }

                        Conference.createNew(ps, subscriber);
                    }
                    return new Result(Realm.ACTIVE_DIRECTORY, Code.SUCCESS, subscriber);
                }
            }
            catch(AuthenticationException e)
            {
                LOG.error(e);
                return new Result(Realm.ACTIVE_DIRECTORY, Code.PROTOCOL_ERROR, subscriber);
            }
        }
    }

    private boolean isValidLocalPassword(Subscriber subscriber, String password)
    {
        return subscriber.getPassword().equals(SecurityUtil.hashPassword(password));
    }
}
