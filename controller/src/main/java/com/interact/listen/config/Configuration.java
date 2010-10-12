package com.interact.listen.config;

import com.interact.listen.HibernateUtil;

import java.util.ArrayList;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

public final class Configuration
{
    private static final Logger LOG = Logger.getLogger(Configuration.class);

    private Configuration()
    {
        throw new AssertionError("Cannot instantiate utility class ConfigurationHolder");
    }

    public static synchronized void set(Property.Key key, String value)
    {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        try
        {
            transaction = session.beginTransaction();
            Property property = Property.newInstance(key.getKey(), value);
            session.saveOrUpdate(property);
            transaction.commit();
        }
        catch(Throwable t) // SUPPRESS CHECKSTYLE IllegalCatchCheck
        {
            // catching a Throwable is okay here, we want to use the default value if we get a DB connection error
            LOG.error(t);
            if(transaction != null)
            {
                transaction.rollback();
            }
        }
        finally
        {
            session.close();
        }
    }

    public static String get(Property.Key key)
    {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        try
        {
            transaction = session.beginTransaction();
            Property property = (Property)session.get(Property.class, key.getKey());
            transaction.commit();
            if(property == null)
            {
                return getDefaultValue(key);
            }
            return property.getValue();
        }
        catch(Throwable t) // SUPPRESS CHECKSTYLE IllegalCatchCheck
        {
            // catching a Throwable is okay here, we want to use the default value if we get a DB connection error
            LOG.error(t);
            if(transaction != null)
            {
                transaction.rollback();
            }
            return getDefaultValue(key);
        }
        finally
        {
            session.clear();
        }
    }

    private static String getDefaultValue(Property.Key key)
    {
        String systemProperty = System.getProperty(key.getKey());
        if(systemProperty != null)
        {
            LOG.warn("Using Property [" + key + "] system value [" + systemProperty + "]");
            return systemProperty;
        }
        LOG.warn("Using Property [" + key + "] default value [" + key.getDefaultValue() + "]");
        return key.getDefaultValue();
    }

    public static String firstSpotSystem()
    {
        Set<String> systems = Property.delimitedStringToSet(Configuration.get(Property.Key.SPOT_SYSTEMS), ",");
        if(systems.size() == 0)
        {
            throw new IllegalStateException("Cannot retrieve first system name, there are no Spot Subscribers");
        }
        String system = new ArrayList<String>(systems).get(0);
        return system.substring(0, system.indexOf("/", "https://".length())); // disgusting
    }

    public static String phoneNumber()
    {
        String number = Configuration.get(Property.Key.PHONE_NUMBER);
        if(number == null)
        {
            return "";
        }
        return number.split(";")[1];
    }

    public static String phoneNumberProtocol()
    {
        String number = Configuration.get(Property.Key.PHONE_NUMBER);
        if(number == null)
        {
            return "";
        }
        return number.split(";")[0];
    }
}
