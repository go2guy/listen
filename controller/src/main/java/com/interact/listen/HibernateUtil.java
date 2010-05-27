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

@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "REC_CATCH_EXCEPTION", justification = "Any error occurring here needs to throw ExceptionInInitializerError")
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

            // hsqldb-specific

            // TODO the dialect should match up with the DB URL and driver
            config.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
            config.setProperty("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver");

            // only for HSQLDB, see https://forum.hibernate.org/viewtopic.php?p=2220295
            // (if batching is enabled with HSQLDB, error messages from the driver are vague)
            config.setProperty("hibernate.jdbc.batch_size", "0");

            // general configuration
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

            Conference conference = new Conference();
            conference.setDescription(subscriber.getNumber());
            conference.setIsRecording(false);
            conference.setIsStarted(true);

            Pin active = Pin.newRandomInstance(PinType.ACTIVE);
            Pin admin = Pin.newRandomInstance(PinType.ADMIN);
            Pin passive = Pin.newRandomInstance(PinType.PASSIVE);

            persistenceService.save(active);
            persistenceService.save(admin);
            persistenceService.save(passive);

            conference.addPin(active);
            conference.addPin(admin);
            conference.addPin(passive);
            persistenceService.save(conference);

            User user = new User();
            user.setPassword(SecurityUtil.hashPassword("super"));
            user.setSubscriber(subscriber);
            user.setUsername(subscriber.getNumber());
            persistenceService.save(user);

            user.addConference(conference);

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

                conference.addParticipant(participant);
            }

            LOG.debug("Saved User [" + user + "]");
        }
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
