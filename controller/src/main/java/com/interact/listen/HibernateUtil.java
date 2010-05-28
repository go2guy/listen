package com.interact.listen;

import com.interact.listen.config.Property;
import com.interact.listen.resource.*;
import com.interact.listen.resource.Pin.PinType;
import com.interact.listen.security.SecurityUtil;

import java.io.File;
import java.text.DecimalFormat;

import org.apache.log4j.Logger;
import org.hibernate.*;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "REC_CATCH_EXCEPTION", justification = "For any Throwable we need to rethrow it as an ExceptionInInitializerError")
public final class HibernateUtil
{
    private static final Logger LOG = Logger.getLogger(HibernateUtil.class);
    private static final SessionFactory SESSION_FACTORY;

    private HibernateUtil()
    {
        throw new AssertionError("Cannot instantiate utility class HibernateUtil");
    }

    static
    {
        try
        {
            AnnotationConfiguration config = new AnnotationConfiguration();
            config.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
            config.setProperty("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver");

            String dburl = getDbConnectionString();
            LOG.debug("DB connection string is [" + dburl + "]");

            config.setProperty("hibernate.connection.url", dburl);
            config.setProperty("hibernate.connection.username", "sa");
            config.setProperty("hibernate.connection.password", "");
            config.setProperty("hibernate.connection.pool_size", "1");
            config.setProperty("hibernate.connection.autocommit", "false");
            config.setProperty("hibernate.cache.provider_class", "org.hibernate.cache.NoCacheProvider");
            config.setProperty("hibernate.hbm2ddl.auto", "update");
            config.setProperty("hibernate.show_sql", "false");
            config.setProperty("hibernate.transaction.factory_class",
                               "org.hibernate.transaction.JDBCTransactionFactory");
            config.setProperty("hibernate.current_session_context_class", "thread");

            // application classes
            config.addAnnotatedClass(Audio.class);
            config.addAnnotatedClass(Conference.class);
            config.addAnnotatedClass(ConferenceHistory.class);
            config.addAnnotatedClass(ConferenceRecording.class);
            config.addAnnotatedClass(ListenSpotSubscriber.class);
            config.addAnnotatedClass(Participant.class);
            config.addAnnotatedClass(Pin.class);
            config.addAnnotatedClass(Property.class);
            config.addAnnotatedClass(Subscriber.class);
            config.addAnnotatedClass(User.class);
            config.addAnnotatedClass(Voicemail.class);

            SESSION_FACTORY = config.buildSessionFactory();

            Session session = getSessionFactory().getCurrentSession();
            Transaction transaction = session.beginTransaction();

            PersistenceService persistenceService = new PersistenceService(session);

            createAdminUserIfNotPresent(session, persistenceService);

            if(Boolean.valueOf(System.getProperty("bootstrap", "false")))
            {
                bootstrap(persistenceService);
            }

            transaction.commit();
        }
        catch(Throwable t) // SUPPRESS CHECKSTYLE IllegalCatchCheck
        {
            LOG.error("SessionFactory creation failed", t);
            throw new ExceptionInInitializerError(t);
        }
    }

    public static SessionFactory getSessionFactory()
    {
        return SESSION_FACTORY;
    }

    private static String getDbConnectionString()
    {
        String configured = System.getProperty("com.interact.listen.dburl");
        if(configured != null)
        {
            return configured;
        }

        try
        {
            String dirPath = System.getProperty("data.dir", "/var/lib/com.interact.listen");
            File dir = new File(dirPath);
            if(!dir.exists())
            {
                LOG.debug("Creating data directory at [" + dir + "]");
                if(!dir.mkdirs())
                {
                    LOG.error("Cannot create directory [" + dir + "]"); 
                }
            }
            else if(dir.exists() && !dir.isDirectory())
            {
                // first try user home
                File homeDir = new File(System.getProperty("user.home"));
                if(homeDir.exists() && homeDir.canWrite())
                {
                    dir = new File(homeDir, ".com.interact.listen");
                    if(!dir.mkdirs())
                    {
                        LOG.error("Cannot create directory [" + dir + "]");
                    }
                }
                else
                {
                    // then try temp directory
                    File temp = File.createTempFile("temp", "temp");
                    File tempDir = new File(temp.getParent(), "com.interact.listen");
                    LOG.debug("Cannot use [" + dir + "] for data directory, using [" + tempDir + "]");
                    dir = tempDir;
                }
            }

            return "jdbc:hsqldb:file:" + dir.getAbsolutePath() + "/database/listendb";
        }
        catch(Exception e)
        {
            LOG.error("Error initializing data directory, using current working directory", e);
        }

        return "jdbc:hsqldb:file:listendb";
    }

    private static void bootstrap(PersistenceService persistenceService)
    {
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

        // dummy accounts
        for(int i = 0; i < 5; i++)
        {
            Subscriber subscriber = new Subscriber();
            subscriber.setNumber(new DecimalFormat("000").format(100 + i));
            subscriber.setVoicemailGreetingLocation("/greetings/" + subscriber.getNumber());
            subscriber.setVoicemailPin(subscriber.getNumber());
            persistenceService.save(subscriber);

            String basePin = new DecimalFormat("000").format(i + 100);

            //Pin activePin = Pin.newInstance("111" + basePin, PinType.ACTIVE);
            //Pin adminPin = Pin.newInstance("999" + basePin, PinType.ADMIN);
            //Pin passivePin = Pin.newInstance("000" + basePin, PinType.PASSIVE);

            Pin activePin = Pin.newRandomInstance(PinType.ACTIVE);
            Pin adminPin = Pin.newRandomInstance(PinType.ADMIN);
            Pin passivePin = Pin.newRandomInstance(PinType.PASSIVE);

            persistenceService.save(activePin);
            persistenceService.save(adminPin);
            persistenceService.save(passivePin);

            Conference conference = new Conference();
            conference.addToPins(activePin);
            conference.addToPins(adminPin);
            conference.addToPins(passivePin);

            conference.setIsStarted(true);
            conference.setIsRecording(false);
            conference.setDescription(subscriber.getNumber());
            persistenceService.save(conference);

            User user = new User();
            user.setPassword(SecurityUtil.hashPassword("super"));
            user.setSubscriber(subscriber);
            user.setUsername(subscriber.getNumber());
            user.addToConferences(conference);
            persistenceService.save(user);

            LOG.debug("Saved Conference " + conference.getId());

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

                LOG.debug("Saved Participant " + participant.getId());
            }
        }

        // account for integration testing
//        Subscriber subscriber = new Subscriber();
//        subscriber.setNumber("347");
//        subscriber.setVoicemailGreetingLocation("/greetings/" + subscriber.getNumber());
//        subscriber.setVoicemailPin(subscriber.getNumber());
//        persistenceService.save(subscriber);
//
//        Pin activePin = Pin.newInstance("111", PinType.ACTIVE);
//        Pin adminPin = Pin.newInstance("347", PinType.ADMIN);
//        Pin passivePin = Pin.newInstance("000", PinType.PASSIVE);
//
//        persistenceService.save(activePin);
//        persistenceService.save(adminPin);
//        persistenceService.save(passivePin);
    }

    private static void createAdminUserIfNotPresent(Session session, PersistenceService persistenceService)
    {
        Criteria criteria = session.createCriteria(User.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.setFirstResult(0);
        criteria.setProjection(Projections.rowCount());
        criteria.add(Restrictions.eq("isAdministrator", Boolean.TRUE));

        Long count = (Long)criteria.list().get(0);

        if(count == 0)
        {
            LOG.debug("Created admin User");
            User user = new User();
            user.setUsername("Admin");
            user.setPassword(SecurityUtil.hashPassword("conference4U!"));
            user.setIsAdministrator(Boolean.TRUE);
            persistenceService.save(user);
        }
    }
}
