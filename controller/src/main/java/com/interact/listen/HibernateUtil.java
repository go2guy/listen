package com.interact.listen;

import com.interact.listen.config.Property;
import com.interact.listen.history.Channel;
import com.interact.listen.jobs.NewVoicemailPagerJob;
import com.interact.listen.resource.*;
import com.interact.listen.resource.Pin.PinType;
import com.interact.listen.security.SecurityUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;

import liquibase.ClassLoaderFileOpener;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.exception.LiquibaseException;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "REC_CATCH_EXCEPTION", justification = "For any Throwable we need to rethrow it as an ExceptionInInitializerError")
public final class HibernateUtil
{
    private static final Logger LOG = Logger.getLogger(HibernateUtil.class);
    private static final SessionFactory SESSION_FACTORY;
    private static final Environment ENVIRONMENT = Environment.valueOf(System.getProperty("com.interact.listen.env", "PROD"));

    /**
     * Provides default configuration values for various environments.
     */
    private static enum Environment
    {
        DEV  ("jdbc:hsqldb:mem:listendb",      "sa",   "", "org.hibernate.dialect.HSQLDialect",        "org.hsqldb.jdbcDriver", "super"),
        TEST ("jdbc:hsqldb:mem:listendb",      "sa",   "", "org.hibernate.dialect.HSQLDialect",        "org.hsqldb.jdbcDriver", "super"),
        PROD ("jdbc:mysql://localhost/listen", "root", "", "org.hibernate.dialect.MySQLInnoDBDialect", "com.mysql.jdbc.Driver", "Int3ract!Inc");

        private final String dbUrl, dbUsername, dbPassword, dbDialect, dbDriver;
        private final String guiPassword;

        private Environment(String dbUrl, String dbUsername, String dbPassword, String dbDialect, String dbDriver, String guiPassword)
        {
            this.dbUrl = dbUrl;
            this.dbUsername = dbUsername;
            this.dbPassword = dbPassword;
            this.dbDialect = dbDialect;
            this.dbDriver = dbDriver;
            this.guiPassword = guiPassword;
        }

        public String getDbUrl()
        {
            return dbUrl;
        }

        public String getDbUsername()
        {
            return dbUsername;
        }

        public String getDbPassword()
        {
            return dbPassword;
        }

        public String getDbDialect()
        {
            return dbDialect;
        }

        public String getDbDriver()
        {
            return dbDriver;
        }

        public String getGuiPassword()
        {
            return guiPassword;
        }
    }
    
    private HibernateUtil()
    {
        throw new AssertionError("Cannot instantiate utility class HibernateUtil");
    }

    static
    {
        try
        {
            final String dbUrl = System.getProperty("com.interact.listen.db.url", ENVIRONMENT.getDbUrl());
            final String dbUsername = System.getProperty("com.interact.listen.db.username", ENVIRONMENT.getDbUsername());
            final String dbPassword = System.getProperty("com.interact.listen.db.password", ENVIRONMENT.getDbPassword());
            final String dbDialect = System.getProperty("com.interact.listen.db.dialect", ENVIRONMENT.getDbDialect());
            final String dbDriver = System.getProperty("com.interact.listen.db.driver", ENVIRONMENT.getDbDriver());

            LOG.debug("DB connection string = [" + dbUrl + "]");
            LOG.debug("DB username =          [" + dbUsername + "]");
            LOG.debug("DB password =          [*]");
            LOG.debug("DB dialect =           [" + dbDialect + "]");
            LOG.debug("DB driver =            [" + dbDriver + "]");

            AnnotationConfiguration config = new AnnotationConfiguration();
            config.setProperty("hibernate.dialect", dbDialect);
            config.setProperty("hibernate.connection.driver_class", dbDriver);
            config.setProperty("hibernate.connection.url", dbUrl);
            config.setProperty("hibernate.connection.username", dbUsername);
            config.setProperty("hibernate.connection.password", dbPassword);
            config.setProperty("hibernate.connection.pool_size", "1");
            config.setProperty("hibernate.connection.autocommit", "false");
            config.setProperty("hibernate.cache.provider_class", "org.hibernate.cache.NoCacheProvider");
            config.setProperty("hibernate.show_sql", "false");
            config.setProperty("hibernate.transaction.factory_class",
                               "org.hibernate.transaction.JDBCTransactionFactory");
            config.setProperty("hibernate.current_session_context_class", "thread");

            // application classes
            config.addAnnotatedClass(AccessNumber.class);
            config.addAnnotatedClass(ActionHistory.class);
            config.addAnnotatedClass(Audio.class);
            config.addAnnotatedClass(CallDetailRecord.class);
            config.addAnnotatedClass(Conference.class);
            config.addAnnotatedClass(ConferenceHistory.class);
            config.addAnnotatedClass(ConferenceRecording.class);
            config.addAnnotatedClass(History.class);
            config.addAnnotatedClass(ListenSpotSubscriber.class);
            config.addAnnotatedClass(Participant.class);
            config.addAnnotatedClass(Pin.class);
            config.addAnnotatedClass(Property.class);
            config.addAnnotatedClass(ScheduledConference.class);
            config.addAnnotatedClass(Subscriber.class);
            config.addAnnotatedClass(Voicemail.class);

            SESSION_FACTORY = config.buildSessionFactory();

            doLiquibaseUpgrades();

            Session session = getSessionFactory().getCurrentSession();
            Transaction transaction = session.beginTransaction();

            PersistenceService persistenceService = new PersistenceService(session, null, Channel.GUI);

            createAdminSubscriberIfNotPresent(session, persistenceService);

            if(Boolean.valueOf(System.getProperty("bootstrap", "false")))
            {
                bootstrap(persistenceService);
            }
            
            startPagingThread();

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

    // TODO get rid of this eventually (replace with some sort of SQL script, maybe? or don't even need it?)
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
        for(int i = 0; i < 25; i++)
        {
            String extension = new DecimalFormat("000").format(100 + i);

            Subscriber subscriber = new Subscriber();
            subscriber.setPassword(SecurityUtil.hashPassword(ENVIRONMENT.getGuiPassword()));
            subscriber.setUsername(extension);
            subscriber.setVoicemailPin(Long.valueOf(extension));
            persistenceService.save(subscriber);

            AccessNumber accessNumber = new AccessNumber();
            accessNumber.setGreetingLocation("/greetings/" + extension);
            accessNumber.setNumber(extension);
            accessNumber.setSubscriber(subscriber);
            persistenceService.save(accessNumber);

            subscriber.addToAccessNumbers(accessNumber);

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
            conference.setDescription(subscriber.getUsername() + "'s Conference");
            persistenceService.save(conference);

            subscriber.addToConferences(conference);

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
                participant.setNumber("402" + extension + new DecimalFormat("0000").format(j));
                participant.setSessionID(participant.getNumber() + String.valueOf(System.currentTimeMillis()));
                persistenceService.save(participant);

                LOG.debug("Saved Participant " + participant.getId());
            }
        }
    }

    private static void createAdminSubscriberIfNotPresent(Session session, PersistenceService persistenceService)
    {
        Long count = Subscriber.count(session);
        if(count == 0)
        {
            LOG.debug("Created admin Subscriber");
            Subscriber subscriber = new Subscriber();
            subscriber.setIsAdministrator(Boolean.TRUE);
            subscriber.setPassword(SecurityUtil.hashPassword(ENVIRONMENT.getGuiPassword()));
            subscriber.setUsername("Admin");
            subscriber.setVoicemailPin(123L);
            persistenceService.save(subscriber);
        }
    }

    private static void doLiquibaseUpgrades() throws SQLException, LiquibaseException
    {
        Liquibase liquibase = null;
        try
        {
            LOG.debug("Executing Liquibase updates");

            // Connection connection = config.buildSettings().getConnectionProvider().getConnection();
            Connection connection = ((SessionFactoryImplementor)SESSION_FACTORY).getConnectionProvider().getConnection();
            if(connection == null)
            {
                throw new RuntimeException("Could not get connection to perform Liquibase updates");
            }

            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
            database.setDefaultSchemaName(connection.getCatalog());

            liquibase = new Liquibase("changelog.xml", new ClassLoaderFileOpener(), connection);
            liquibase.update(null);
        }
        finally
        {
            if(liquibase != null && liquibase.getDatabase() != null)
            {
                liquibase.getDatabase().close();
            }
        }
    }
    
    private static void startPagingThread() throws Exception
    {
        try
        {
            SchedulerFactory sf = new StdSchedulerFactory();
            Scheduler sched = sf.getScheduler();
            JobDetail jobDetail = new JobDetail("job1", "group1", NewVoicemailPagerJob.class);
            CronTrigger cronTrigger = new CronTrigger("cronTrigger", "group2", "0 0/1 * * * ?");
            sched.scheduleJob(jobDetail, cronTrigger);
            sched.start();
        }
        catch(Exception e)
        {
            LOG.fatal("Exception starting voicemail paging job", e);
            throw e;
        }
    }
}
