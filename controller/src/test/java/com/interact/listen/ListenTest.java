package com.interact.listen;

import com.interact.listen.license.AlwaysTrueMockLicense;
import com.interact.listen.license.License;
import com.interact.listen.resource.Subscriber;
import com.interact.listen.resource.Voicemail;

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

    public static Subscriber createSubscriber(Session session)
    {
        Subscriber subscriber = new Subscriber();
        subscriber.setEmailAddress(TestUtil.randomString() + "@example.com");
        subscriber.setIsActiveDirectory(false);
        subscriber.setIsAdministrator(false);
        subscriber.setPassword(TestUtil.randomString());
        subscriber.setRealName(TestUtil.randomString());
        subscriber.setUsername(TestUtil.randomString());
        subscriber.setVoicemailPin(TestUtil.randomNumeric(8));
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
}
