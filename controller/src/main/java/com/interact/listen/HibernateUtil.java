package com.interact.listen;

import com.interact.listen.resource.*;
import com.interact.listen.resource.Pin.PinType;
import com.interact.listen.security.SecurityUtil;

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
            config.addAnnotatedClass(Pin.class);
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

        PersistenceService persistenceService = new PersistenceService(session);

        // provisions subscribers/users/conferences/participants

        // for N=0..5

        // subscriber number = 10N
        // subscriber VM pin = 10N
        // user username = 10N
        // user password = super
        // conference activePin = 11110N
        // conference adminPin = 99910N
        // conference passivePin = 00010N
        // participant numbers are 40200N000M, where M=0..9

        try
        {
            // Administrative user
            User user = new User();
            user.setUsername("Admin");
            user.setPassword(SecurityUtil.hashPassword("super"));
            user.setIsAdministrator(Boolean.TRUE);
            persistenceService.save(user);

            // dummy accounts
            for(int i = 0; i < 5; i++)
            {
                Subscriber subscriber = new Subscriber();
                subscriber.setNumber(new DecimalFormat("000").format(100 + i));
                subscriber.setVoicemailGreetingLocation("/greetings/" + subscriber.getNumber());
                subscriber.setVoicemailPin(subscriber.getNumber());
                persistenceService.save(subscriber);

                String basePin = new DecimalFormat("000").format(i + 100);

                Pin activePin = Pin.newInstance("111" + basePin, PinType.ACTIVE);
                Pin adminPin = Pin.newInstance("999" + basePin, PinType.ADMIN);
                Pin passivePin = Pin.newInstance("000" + basePin, PinType.PASSIVE);

                persistenceService.save(activePin);
                persistenceService.save(adminPin);
                persistenceService.save(passivePin);

                Conference conference = new Conference();
                conference.addToPins(activePin);
                conference.addToPins(adminPin);
                conference.addToPins(passivePin);

                conference.setIsStarted(true);
                conference.setDescription(subscriber.getNumber());
                persistenceService.save(conference);

                user = new User();
                user.setPassword(SecurityUtil.hashPassword("super"));
                user.setSubscriber(subscriber);
                user.setUsername(subscriber.getNumber());
                user.addToConferences(conference);
                persistenceService.save(user);

                System.out.println("BOOTSTRAP: Saved Conference " + conference.getId());

                for(int j = 0; j < 10; j++)
                {
                    Participant participant = new Participant();
                    participant.setAudioResource("/foo/bar");
                    participant.setConference(conference);
                    participant.setIsAdmin(j == 0);
                    participant.setIsAdminMuted(false);
                    participant.setIsMuted(false);
                    participant.setIsPassive(j == 6);
                    participant.setNumber("402" + basePin + new DecimalFormat("0000").format(j));
                    participant.setSessionID(participant.getNumber() + String.valueOf(System.currentTimeMillis()));
                    persistenceService.save(participant);

                    System.out.println("BOOTSTRAP: Saved Participant " + participant.getId());
                }
            }

            // account for integration testing
            Subscriber subscriber = new Subscriber();
            subscriber.setNumber("347");
            subscriber.setVoicemailGreetingLocation("/greetings/" + subscriber.getNumber());
            subscriber.setVoicemailPin(subscriber.getNumber());
            persistenceService.save(subscriber);

            Pin activePin = Pin.newInstance("111", PinType.ACTIVE);
            Pin adminPin = Pin.newInstance("347", PinType.ADMIN);
            Pin passivePin = Pin.newInstance("000", PinType.PASSIVE);

            persistenceService.save(activePin);
            persistenceService.save(adminPin);
            persistenceService.save(passivePin);

//            Conference conference = new Conference();
//            conference.setIsStarted(false);
//            conference.setDescription("Ladi's Conference");
//            conference.addToPins(activePin);
//            conference.addToPins(adminPin);
//            conference.addToPins(passivePin);
//
//            persistenceService.save(conference);
//
//            user = new User();
//            user.setPassword(SecurityUtil.hashPassword("super"));
//            user.setSubscriber(subscriber);
//            user.setUsername(subscriber.getNumber());
//            user.addToConferences(conference);
//            persistenceService.save(user);

            transaction.commit();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            transaction.rollback();
            throw new ExceptionInInitializerError(e);
        }
    }
}
