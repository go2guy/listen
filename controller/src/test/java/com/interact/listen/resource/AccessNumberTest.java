package com.interact.listen.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import com.interact.listen.ListenTest;
import com.interact.listen.PersistenceService;
import com.interact.listen.TestUtil;
import com.interact.listen.resource.Voicemail.MessageLightState;
import com.interact.listen.spot.MessageLightToggler;

import org.junit.Before;
import org.junit.Test;

public class AccessNumberTest extends ListenTest
{
    private AccessNumber accessNumber;

    @Before
    public void setUp()
    {
        accessNumber = new AccessNumber();
    }

    @Test
    public void test_isCreatedByUser_defaultsToTrue()
    {
        assertTrue(accessNumber.getIsCreatedByUser());
    }

    @Test
    public void test_isFindMeNumber_defaultsToFalse()
    {
        assertFalse(accessNumber.getIsFindMeNumber());
    }

    @Test
    public void test_findMePriority_defaultsToZero()
    {
        assertEquals(Integer.valueOf(0), accessNumber.getFindMePriority());
    }

    @Test
    public void test_findMeDuration_defaultsToZero()
    {
        assertEquals(Integer.valueOf(0), accessNumber.getFindMeDuration());
    }

    @Test
    public void test_setId_setsId()
    {
        final Long id = System.currentTimeMillis();
        accessNumber.setId(id);
        assertEquals(id, accessNumber.getId());
    }

    @Test
    public void test_setVersion_setsVersion()
    {
        final Integer version = 1234;
        accessNumber.setVersion(version);
        assertEquals(version, accessNumber.getVersion());
    }

    @Test
    public void test_setNumber_setsNumber()
    {
        final String number = TestUtil.randomString();
        accessNumber.setNumber(number);
        assertEquals(number, accessNumber.getNumber());
    }

    @Test
    public void test_setSubscriber_setsSubscriber()
    {
        final Subscriber subscriber = createSubscriber(session);
        accessNumber.setSubscriber(subscriber);
        assertEquals(subscriber, accessNumber.getSubscriber());
    }

    @Test
    public void test_setGreetingLocation_setsGreetingLocation()
    {
        final String greetingLocation = TestUtil.randomString();
        accessNumber.setGreetingLocation(greetingLocation);
        assertEquals(greetingLocation, accessNumber.getGreetingLocation());
    }

    @Test
    public void test_setSupportsMessageLight_setsSupportsMessageLight()
    {
        final Boolean light = Boolean.TRUE;
        accessNumber.setSupportsMessageLight(light);
        assertEquals(light, accessNumber.getSupportsMessageLight());
    }

    @Test
    public void test_setIsCreatedByUser_setsIsCreatedByUser()
    {
        final Boolean is = Boolean.FALSE;
        accessNumber.setIsCreatedByUser(is);
        assertEquals(is, accessNumber.getIsCreatedByUser());
    }

    @Test
    public void test_setIsFindMeNumber_setsIsFindMeNumber()
    {
        final Boolean is = Boolean.FALSE;
        accessNumber.setIsFindMeNumber(is);
        assertEquals(is, accessNumber.getIsFindMeNumber());
    }

    @Test
    public void test_setFindMePriority_setsFindMePriority()
    {
        final Integer priority = TestUtil.randomInteger();
        accessNumber.setFindMePriority(priority);
        assertEquals(priority, accessNumber.getFindMePriority());
    }

    @Test
    public void test_setFindMeDuration_setsFindMePriority()
    {
        final Integer priority = TestUtil.randomInteger();
        accessNumber.setFindMeDuration(priority);
        assertEquals(priority, accessNumber.getFindMeDuration());
    }

    @Test
    public void test_validate_nullNumber_returnsFalse()
    {
        accessNumber.setNumber(null);
        accessNumber.setSubscriber(new Subscriber());
        assertFalse(accessNumber.validate());
        assertTrue(accessNumber.errors().contains("number cannot be null or empty"));
    }

    @Test
    public void test_validate_blankNumber_returnsFalse()
    {
        accessNumber.setNumber(" ");
        accessNumber.setSubscriber(new Subscriber());
        assertFalse(accessNumber.validate());
        assertTrue(accessNumber.errors().contains("number cannot be null or empty"));
    }

    @Test
    public void test_validate_nullSubscriber_returnsFalse()
    {
        accessNumber.setNumber(TestUtil.randomString());
        accessNumber.setSubscriber(null);
        assertFalse(accessNumber.validate());
        assertTrue(accessNumber.errors().contains("subscriber cannot be null"));
    }
    
    @Test
    public void test_afterSave_whenAccessNumberSupportsMessageLight_invokesMessageLightToggle()
    {
        Subscriber subscriber = createSubscriber(session);
        AccessNumber accessNumber = createAccessNumber(session, subscriber);
        
        MessageLightToggler mlt = mock(MessageLightToggler.class);
        PersistenceService ps = mock(PersistenceService.class);
        
        accessNumber.setSupportsMessageLight(true);
        accessNumber.useMessageLightToggler(mlt);
        
        accessNumber.afterSave(ps, null);
        verify(mlt).toggleMessageLight(ps, accessNumber);
    }
    
    @Test
    public void test_afterSave_whenAccessNumberDoesntSupportMessageLight_doesntInvokesMessageLightToggle()
    {
        Subscriber subscriber = createSubscriber(session);
        AccessNumber accessNumber = createAccessNumber(session, subscriber);
        
        MessageLightToggler mlt = mock(MessageLightToggler.class);
        PersistenceService ps = mock(PersistenceService.class);
        
        accessNumber.setSupportsMessageLight(false);
        accessNumber.useMessageLightToggler(mlt);
        
        accessNumber.afterSave(ps, null);
        verifyZeroInteractions(mlt);
    }
    
    @Test
    public void test_afterDelete_invokesMessageLightToggleOff()
    {
        Subscriber subscriber = createSubscriber(session);
        AccessNumber accessNumber = createAccessNumber(session, subscriber);
        
        MessageLightToggler mlt = mock(MessageLightToggler.class);
        PersistenceService ps = mock(PersistenceService.class);
        
        accessNumber.useMessageLightToggler(mlt);
        
        accessNumber.afterDelete(ps, null);
        verify(mlt).toggleMessageLight(ps, accessNumber, MessageLightState.OFF);
    }
}
