package com.interact.listen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.interact.listen.attendant.*;
import com.interact.listen.license.AlwaysTrueMockLicense;
import com.interact.listen.license.License;
import com.interact.listen.resource.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;

public abstract class ListenTest
{
    protected Session session;
    private Transaction transaction;

    @Before
    public void setUpHibernate()
    {
        session = HibernateUtil.getSessionFactory().getCurrentSession();
        transaction = session.beginTransaction();
    }

    @Before
    public void setUpLicensing()
    {
        License.setLicense(new AlwaysTrueMockLicense());
    }

    @After
    public void tearDownHibernate()
    {
        transaction.rollback();
    }

    public void assertConstructorThrowsAssertionError(Class<?> clazz, String expectedMessage)
        throws IllegalAccessException, InstantiationException
    {
        Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
        constructor.setAccessible(true);

        try
        {
            constructor.newInstance();
            fail("Expected InvocationTargetException with root cause of AssertionError for utility class constructor");
        }
        catch(InvocationTargetException e)
        {
            Throwable cause = e.getCause();
            assertTrue(cause instanceof AssertionError);
            assertEquals(expectedMessage, cause.getMessage());
        }
    }

    public static Subscriber createSubscriber(Session session)
    {
        Subscriber subscriber = new Subscriber();
        subscriber.setEmailAddress(TestUtil.randomString() + "@example.com");
        subscriber.setIsActiveDirectory(false);
        subscriber.setIsAdministrator(false);
        subscriber.setPassword(TestUtil.randomString());
        subscriber.setRealName(TestUtil.randomString());
        subscriber.setUsername(TestUtil.randomString());
        subscriber.setVoicemailPin(TestUtil.randomNumeric(4).toString());
        session.save(subscriber);
        return subscriber;
    }

    public static Voicemail createVoicemail(Session session, Subscriber forSubscriber)
    {
        Voicemail voicemail = new Voicemail();
        voicemail.setDateCreated(new Date());
        voicemail.setDescription(TestUtil.randomString());
        voicemail.setDuration(String.valueOf(TestUtil.randomNumeric(3)));
        voicemail.setFileSize(String.valueOf(TestUtil.randomNumeric(4)));
        voicemail.setForwardedBy(null);
        voicemail.setIsNew(true);
        voicemail.setSubscriber(forSubscriber);
        voicemail.setUri(TestUtil.randomString());
        session.save(voicemail);
        return voicemail;
    }

    public static Conference createConference(Session session, Subscriber forSubscriber)
    {
        Conference conference = new Conference();
        conference.setArcadeId(TestUtil.randomString());
        conference.setDescription(TestUtil.randomString());
        conference.setIsRecording(false);
        conference.setIsStarted(false);
        conference.setRecordingSessionId(null);
        conference.setStartTime(null);
        conference.setSubscriber(forSubscriber);
        session.save(conference);
        return conference;
    }

    public static Participant createParticipant(Session session, Conference forConference, Boolean isAdmin)
    {
        Participant participant = new Participant();
        participant.setAudioResource(TestUtil.randomString());
        participant.setConference(forConference);
        participant.setId(TestUtil.randomNumeric(10));
        participant.setIsAdmin(isAdmin);
        participant.setIsAdminMuted(Boolean.FALSE);
        participant.setIsMuted(Boolean.TRUE);
        participant.setIsPassive(Boolean.FALSE);
        participant.setNumber(String.valueOf(System.currentTimeMillis()) + String.valueOf(TestUtil.randomNumeric(10)));
        participant.setSessionID(TestUtil.randomString());
        session.save(participant);
        return participant;
    }
    
    public static Menu createMenu(Session session)
    {
        Menu menu = new Menu();
        menu.setName(TestUtil.randomString());
        menu.setAudioFile(TestUtil.randomString());
        session.save(menu);
        return menu;
    }

    public static Action createAction(Session session, String actionType, Menu forMenu, String keyPressed, Menu targetMenu)
    {
        Action action = null;
        if(actionType.equals("DialNumberAction"))
        {
            action = new DialNumberAction();
            ((DialNumberAction)action).setNumber(TestUtil.randomString());
        }
        else if(actionType.equals("DialPressedNumberAction"))
        {
            action = new DialPressedNumberAction();
        }
        else if(actionType.equals("GoToMenuAction"))
        {
            action = new GoToMenuAction();
            ((GoToMenuAction)action).setGoToMenu(targetMenu);
        }
        else if(actionType.equals("LaunchApplicationAction"))
        {
            action = new LaunchApplicationAction();
            ((LaunchApplicationAction)action).setApplicationName(TestUtil.randomString());
        }
        
        action.setMenu(forMenu);
        action.setKeyPressed(keyPressed);
        session.save(action);
        
        return action;
    }
}
