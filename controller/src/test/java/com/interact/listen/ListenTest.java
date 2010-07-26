package com.interact.listen;

import com.interact.listen.license.AlwaysTrueMockLicense;
import com.interact.listen.license.License;

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
}
