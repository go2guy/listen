package com.interact.listen;

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

    @After
    public void tearDownHibernate()
    {
        transaction.rollback();
    }
}
