package com.interact.listen.resource;

import static org.junit.Assert.*;

import com.interact.listen.HibernateUtil;
import com.interact.listen.resource.ListenSpotSubscriber.PhoneNumberProtocolType;
import com.interact.listen.resource.Pin.PinType;

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
    public void test_setPhoneNumber_withValidPhoneNumber_setsId()
    {
        final String phoneNumber = "foo";
        listenSpotSubscriber.setHttpApi(phoneNumber);

        assertEquals(phoneNumber, listenSpotSubscriber.getPhoneNumber());
    }
    
    @Test
    public void test_setPhoneNumberProtocol_withValidProtocol_setsProtocol()
    {
        final PhoneNumberProtocolType protocol = PhoneNumberProtocolType.VOIP;
        listenSpotSubscriber.setPhoneNumberProtocol(protocol);

        assertEquals(protocol, listenSpotSubscriber.getPhoneNumberProtocol());
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

    @Test
    public void test_equals_sameObject_returnsTrue()
    {
        assertTrue(listenSpotSubscriber.equals(listenSpotSubscriber));
    }

    @Test
    public void test_equals_thatNull_returnsFalse()
    {
        assertFalse(listenSpotSubscriber.equals(null));
    }

    @Test
    public void test_equals_thatNotAListenSpotSubscriber_returnsFalse()
    {
        assertFalse(listenSpotSubscriber.equals(new String()));
    }

    @Test
    public void test_equals_httpApiNotEqual_returnsFalse()
    {
        listenSpotSubscriber.setHttpApi("foo");

        ListenSpotSubscriber that = new ListenSpotSubscriber();
        that.setHttpApi(null);

        assertFalse(listenSpotSubscriber.equals(that));
    }

    @Test
    public void test_equals_allPropertiesEqual_returnsTrue()
    {
        String httpApi = String.valueOf(System.currentTimeMillis());

        listenSpotSubscriber.setHttpApi(httpApi);

        ListenSpotSubscriber that = new ListenSpotSubscriber();
        that.setHttpApi(httpApi);

        assertTrue(listenSpotSubscriber.equals(that));
    }
    
    @Test
    public void test_validate_nullPhoneNumber_returnsFalseAndHasErrors()
    {
        listenSpotSubscriber.setPhoneNumber(null);

        assertFalse(listenSpotSubscriber.validate());
        assertTrue(listenSpotSubscriber.hasErrors());
    }
    
    @Test
    public void test_validate_blankPhoneNumber_returnsFalseAndHasErrors()
    {
        listenSpotSubscriber.setPhoneNumber("");

        assertFalse(listenSpotSubscriber.validate());
        assertTrue(listenSpotSubscriber.hasErrors());
    }
    
    @Test
    public void test_validate_nullPhoneNumberProtocol_returnsFalseAndHasErrors()
    {
        listenSpotSubscriber.setPhoneNumberProtocol(null);

        assertFalse(listenSpotSubscriber.validate());
        assertTrue(listenSpotSubscriber.hasErrors());
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
