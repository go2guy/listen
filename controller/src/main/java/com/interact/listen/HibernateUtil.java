package com.interact.listen;

import com.interact.listen.resource.*;

import java.text.DecimalFormat;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;

public final class HibernateUtil
{
    private static final SessionFactory SESSION_FACTORY;

    private HibernateUtil()
    {
        throw new AssertionError("Cannot instantiate utility class " + this.getClass().getName());
    }

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
            config.setProperty("hibernate.connection.autocommit", "false");
            config.setProperty("hibernate.cache.provider_class", "org.hibernate.cache.NoCacheProvider");
            config.setProperty("hibernate.hbm2ddl.auto", "create-drop");
            config.setProperty("hibernate.show_sql", "false");
            config.setProperty("hibernate.transaction.factory_class",
                               "org.hibernate.transaction.JDBCTransactionFactory");
            config.setProperty("hibernate.current_session_context_class", "thread");

            // application classes
            config.addAnnotatedClass(Conference.class);
            config.addAnnotatedClass(ConferenceHistory.class);
            config.addAnnotatedClass(ListenSpotSubscriber.class);
            config.addAnnotatedClass(Participant.class);
            config.addAnnotatedClass(Subscriber.class);
            config.addAnnotatedClass(User.class);
            config.addAnnotatedClass(Voicemail.class);

            SESSION_FACTORY = config.buildSessionFactory();

            if(Boolean.valueOf(System.getProperty("bootstrap", "false")))
            {
                bootstrap();
            }
        }
        catch(Exception e)
        {
            System.err.println("SessionFactory creation failed: " + e);
            throw new ExceptionInInitializerError(e);
        }
    }

    public static SessionFactory getSessionFactory()
    {
        return SESSION_FACTORY;
    }

    private static void bootstrap()
    {
        Session session = getSessionFactory().getCurrentSession();
        Transaction transaction = session.beginTransaction();

        try
        {
            for(int i = 0; i < 10; i++)
            {
                Conference conference = new Conference();
                conference.setActivePin(new DecimalFormat("000000").format(i));
                conference.setAdminPin("111" + conference.getActivePin());
                conference.setIsStarted(true);
                conference.setPassivePin("000" + conference.getActivePin());
                session.save(conference);

                System.out.println("BOOTSTRAP: Saved Conference " + conference.getId());

                for(int j = 0; j < 10; j++)
                {
                    Participant participant = new Participant();
                    participant.setAudioResource("/foo/bar");
                    participant.setConference(conference);
                    participant.setIsAdmin(j == 0);
                    participant.setIsAdminMuted(false);
                    participant.setIsHolding(false);
                    participant.setIsMuted(false);
                    participant.setNumber("999" + conference.getActivePin() + new DecimalFormat("000").format(j));
                    participant.setSessionID(participant.getNumber() + String.valueOf(System.currentTimeMillis()));
                    session.save(participant);

                    System.out.println("BOOTSTRAP: Saved Participant " + participant.getId());
                }
            }

            transaction.commit();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            transaction.rollback();

            System.out.println("System exiting (bootstrap error).");
            System.exit(1);
        }
    }
}
