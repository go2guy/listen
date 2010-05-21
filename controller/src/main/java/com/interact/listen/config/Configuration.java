package com.interact.listen.config;

import com.interact.listen.HibernateUtil;

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
                LOG.warn("Property [" + key + "] was null, using default value [" + key.getDefaultValue() + "]");
                return key.getDefaultValue();
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
            return key.getDefaultValue();
        }
        finally
        {
            session.clear();
        }
    }
}
