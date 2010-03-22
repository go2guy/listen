package com.interact.listen;

import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.Voicemail;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

public class HibernateUtil
{
    private static final SessionFactory sessionFactory;

    static
    {
        try
        {
            AnnotationConfiguration config = new AnnotationConfiguration();
            config.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
            config.setProperty("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver");
            config.setProperty("hibernate.connection.url", "jdbc:hsqldb:mem:demodb");
            config.setProperty("hibernate.connection.username", "sa");
            config.setProperty("hibernate.connection.password", "");
            config.setProperty("hibernate.connection.pool_size", "1");
            config.setProperty("hibernate.connection.autocommit", "true");
            config.setProperty("hibernate.cache.provider_class", "org.hibernate.cache.NoCacheProvider");
            config.setProperty("hibernate.hbm2ddl.auto", "create-drop");
            config.setProperty("hibernate.show_sql", "true");
            config.setProperty("hibernate.transaction.factory_class",
                               "org.hibernate.transaction.JDBCTransactionFactory");
            config.setProperty("hibernate.current_session_context_class", "thread");

            // application classes
            config.addAnnotatedClass(Subscriber.class);
            config.addAnnotatedClass(Voicemail.class);

            sessionFactory = config.buildSessionFactory();
        }
        catch(Throwable t)
        {
            System.err.println("SessionFactory creation failed: " + t);
            throw new ExceptionInInitializerError(t);
        }
    }

    public static SessionFactory getSessionFactory()
    {
        return sessionFactory;
    }
}
