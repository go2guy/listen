package com.interact.listen.resource;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.interact.listen.HibernateUtil;

import java.util.List;

import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.junit.Before;
import org.junit.Test;

public class ListenSpotSubscriberTest
{
    private ListenSpotSubscriber listenSpotSubscriber;

    @Before
    public void setUp()
    {
        listenSpotSubscriber = new ListenSpotSubscriber();
    }

    @Test
    public void test_version_defaultsToZero()
    {
        assertEquals(Integer.valueOf(0), listenSpotSubscriber.getVersion());
    }

    @Test
    public void test_setId_withValidId_setsId()
    {
        final Long id = System.currentTimeMillis();
        listenSpotSubscriber.setId(id);

        assertEquals(id, listenSpotSubscriber.getId());
    }

    @Test
    public void test_setHttpApi_withValidHttpApi_setsId()
    {
        final String httpApi = "http://example.com/spot/ccxml/basichttp";
        listenSpotSubscriber.setHttpApi(httpApi);

        assertEquals(httpApi, listenSpotSubscriber.getHttpApi());
    }

    @Test
    public void test_validate_nullHttpApi_returnsFalseAndHasErrors()
    {
        listenSpotSubscriber.setHttpApi(null);

        assertFalse(listenSpotSubscriber.validate());
        assertTrue(listenSpotSubscriber.hasErrors());
    }

    @Test
    public void test_validate_blankHttpApi_returnsFalseAndHasErrors()
    {
        listenSpotSubscriber.setHttpApi("");

        assertFalse(listenSpotSubscriber.validate());
        assertTrue(listenSpotSubscriber.hasErrors());
    }

    @Test
    public void test_validate_whitespaceHttpApi_returnsFalseAndHasErrors()
    {
        listenSpotSubscriber.setHttpApi(" ");

        assertFalse(listenSpotSubscriber.validate());
        assertTrue(listenSpotSubscriber.hasErrors());
    }

    @Test
    public void test_copy_withoutIdAndVersion_createsShallowCopyWithoutIdAndVersion()
    {
        ListenSpotSubscriber original = getPopulatedListenSpotSubscriber();
        ListenSpotSubscriber copy = original.copy(false);

        assertEquals(original.getHttpApi(), copy.getHttpApi());

        assertNull(copy.getId());
        assertEquals(Integer.valueOf(0), copy.getVersion());
    }

    @Test
    public void test_copy_withIdAndVersion_createsShallowCopyWithIdAndVersion()
    {
        ListenSpotSubscriber original = getPopulatedListenSpotSubscriber();
        ListenSpotSubscriber copy = original.copy(true);

        assertEquals(original.getId(), copy.getId());
        assertEquals(original.getVersion(), copy.getVersion());
    }

    @Test
    public void test_list_withNoListenSpotSubscribers_returnsEmptyList()
    {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction transaction = session.beginTransaction();
        List<ListenSpotSubscriber> list = ListenSpotSubscriber.list(session);
        transaction.commit();

        assertEquals(0, list.size());
    }
    
    private ListenSpotSubscriber getPopulatedListenSpotSubscriber()
    {
        ListenSpotSubscriber l = new ListenSpotSubscriber();
        l.setHttpApi("/foo/bar/baz");
        l.setId(System.currentTimeMillis());
        l.setVersion(10);
        return l;
    }
}
