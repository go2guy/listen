package com.interact.listen.resource;

import static org.junit.Assert.*;

import com.interact.listen.ListenTest;
import com.interact.listen.TestUtil;
import com.interact.listen.resource.TimeRestriction.Action;
import com.interact.listen.resource.TimeRestriction.DayOfWeek;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

public class SubscriberTest extends ListenTest
{
    private Subscriber subscriber;

    @Before
    public void setUp()
    {
        subscriber = new Subscriber();
    }

    @Test
    public void test_version_defaultsToZero()
    {
        assertEquals(Integer.valueOf(0), subscriber.getVersion());
    }

    @Test
    public void test_setVoicemailPin_withValidVoicemailPin_setsVoicemailPin()
    {
        final String pin = TestUtil.randomNumeric(4).toString();
        subscriber.setVoicemailPin(pin);

        assertEquals(pin, subscriber.getVoicemailPin());
    }

    @Test
    public void test_setVoicemailPin_withLeadingZeroes_preservesLeadingZeroes()
    {
        final String pin = "00001";
        subscriber.setVoicemailPin(pin);
        assertEquals(pin, subscriber.getVoicemailPin());
    }

    @Test
    public void test_setId_withValidId_setsId()
    {
        final Long id = System.currentTimeMillis();
        subscriber.setId(id);

        assertEquals(id, subscriber.getId());
    }

    @Test
    public void test_setVersion_withValidVersion_setsVersion()
    {
        final Integer version = 1;
        subscriber.setVersion(version);

        assertEquals(version, subscriber.getVersion());
    }

    @Test
    public void test_validate_validProperties_returnsNoErrors()
    {
        subscriber = getPopulatedSubscriber();

        assertTrue(subscriber.validate());
        assertFalse(subscriber.hasErrors());
    }

    @Test
    public void test_copy_withoutIdAndVersion_createsShallowCopyWithoutIdAndVersion()
    {
        Subscriber original = getPopulatedSubscriber();
        Subscriber copy = original.copy(false);

        assertEquals(original.getUsername(), copy.getUsername());
        assertEquals(original.getPassword(), copy.getPassword());
        assertEquals(original.getVoicemailPin(), copy.getVoicemailPin());
        assertTrue(original.getVoicemails() == copy.getVoicemails()); // same reference
        assertEquals(original.getEmailAddress(), copy.getEmailAddress());
        assertEquals(original.getWorkEmailAddress(), copy.getWorkEmailAddress());

        assertNull(copy.getId());
        assertEquals(Integer.valueOf(0), copy.getVersion());
    }

    @Test
    public void test_copy_withIdAndVersion_createsShallowCopyWithIdAndVersion()
    {
        Subscriber original = getPopulatedSubscriber();
        Subscriber copy = original.copy(true);

        assertEquals(original.getId(), copy.getId());
        assertEquals(original.getVersion(), copy.getVersion());
    }

    @Test
    public void test_equals_sameObject_returnsTrue()
    {
        assertTrue(subscriber.equals(subscriber));
    }

    @Test
    public void test_equals_thatNull_returnsFalse()
    {
        assertFalse(subscriber.equals(null));
    }

    @Test
    public void test_equals_thatNotAsubscriber_returnsFalse()
    {
        assertFalse(subscriber.equals(new String()));
    }

    @Test
    public void test_equals_usernameNotEqual_returnsFalse()
    {
        subscriber.setUsername(String.valueOf(System.currentTimeMillis()));

        Subscriber that = new Subscriber();
        that.setUsername(null);

        assertFalse(subscriber.equals(that));
    }

    @Test
    public void test_equals_allPropertiesEqual_returnsTrue()
    {
        String username = String.valueOf(System.currentTimeMillis());

        subscriber.setUsername(username);

        Subscriber that = new Subscriber();
        that.setUsername(username);

        assertTrue(subscriber.equals(that));
    }

    @Test
    public void test_hashCode_returnsUniqueHashcodeForRelevantFields()
    {
        Subscriber obj = new Subscriber();

        // hashcode-relevant properties set to static values for predictability
        obj.setUsername("JJBLT");

        // set a property that has no effect on hashcode to something dynamic
        obj.setPassword(String.valueOf(System.currentTimeMillis()));

        assertEquals(70610985, obj.hashCode());
    }

    // email notification restriction tests
    
    @Test
    public void test_shouldSendNewVoicemailEmail_whenNotificationIsDisabled_returnsFalse()
    {
        subscriber = createSubscriber(session);
        subscriber.setIsEmailNotificationEnabled(false);
        
        assertFalse(subscriber.shouldSendNewVoicemailEmail());
    }
    
    @Test
    public void test_shouldSendNewVoicemailEmail_whenNotificationIsEnabledButEmailAddressisNull_returnsFalse()
    {
        subscriber = createSubscriber(session);
        subscriber.setIsEmailNotificationEnabled(true);
        subscriber.setEmailAddress(null);
        
        assertFalse(subscriber.shouldSendNewVoicemailEmail());
    }
    
    @Test
    public void test_shouldSendNewVoicemailEmail_whenNotificationIsEnabledButEmailAddressisBlank_returnsFalse()
    {
        subscriber = createSubscriber(session);
        subscriber.setIsEmailNotificationEnabled(true);
        subscriber.setEmailAddress(" ");
        
        assertFalse(subscriber.shouldSendNewVoicemailEmail());
    }
    
    @Test
    public void test_shouldSendNewVoicemailEmail_whenEnabledAndNoTimeRestrictionsAreSet_returnsTrue()
    {
        subscriber = createSubscriber(session);
        subscriber.setIsEmailNotificationEnabled(true);
        assert subscriber.getEmailAddress() != null && !subscriber.getEmailAddress().trim().equals("");

        int count = TimeRestriction.queryBySubscriberAndAction(session, subscriber, Action.NEW_VOICEMAIL_EMAIL).size();
        assert count == 0; // no time restrictions

        assertTrue(subscriber.shouldSendNewVoicemailEmail());
    }
    
    @Test
    public void test_shouldSendNewVoicemailEmail_whenEnabledAndCurrentTimeIsOutsideOfTheOnlyTimeRestriction_returnsFalse()
    {
        subscriber = createSubscriber(session);
        subscriber.setIsEmailNotificationEnabled(true);
        assert subscriber.getEmailAddress() != null && !subscriber.getEmailAddress().trim().equals("");
        
        LocalTime now = new LocalTime();
        Set<DayOfWeek> days = new HashSet<DayOfWeek>();
        for(DayOfWeek dow : DayOfWeek.values())
        {
            days.add(dow);
        }

        TimeRestriction restriction = TimeRestriction.create(subscriber, now.plusHours(2), now.plusHours(3), days, Action.NEW_VOICEMAIL_EMAIL);
        session.save(restriction);

        assertFalse(subscriber.shouldSendNewVoicemailEmail());
    }
    
    @Test
    public void test_shouldSendNewVoicemailEmail_whenEnabledAndCurrentTimeIsInsideOfTheOnlyTimeRestriction_returnsTrue()
    {
        subscriber = createSubscriber(session);
        subscriber.setIsEmailNotificationEnabled(true);
        assert subscriber.getEmailAddress() != null && !subscriber.getEmailAddress().trim().equals("");
        
        Set<DayOfWeek> days = new HashSet<DayOfWeek>();
        DayOfWeek today = jodaDayOfWeekToDayOfWeek(new LocalDate().getDayOfWeek());
        days.add(today);
        
        LocalTime now = new LocalTime();
        TimeRestriction restriction = TimeRestriction.create(subscriber, now.minusHours(1), now.plusHours(1), days, Action.NEW_VOICEMAIL_EMAIL);
        session.save(restriction);
        
        assertTrue(subscriber.shouldSendNewVoicemailEmail());
    }
    
    @Test
    public void test_shouldSendNewVoicemailEmail_whenEnabledAndCurrentTimeIsInsideOfTheOnlyTimeRestrictionButRestrictionOnlyAppliesToTomorrow_returnsFalse()
    {
        subscriber = createSubscriber(session);
        subscriber.setIsEmailNotificationEnabled(true);
        assert subscriber.getEmailAddress() != null && !subscriber.getEmailAddress().trim().equals("");
        
        Set<DayOfWeek> days = new HashSet<DayOfWeek>();
        DayOfWeek today = jodaDayOfWeekToDayOfWeek(new LocalDate().plusDays(1).getDayOfWeek());
        days.add(today);
        
        LocalTime now = new LocalTime();
        TimeRestriction restriction = TimeRestriction.create(subscriber, now.minusHours(1), now.plusHours(1), days, Action.NEW_VOICEMAIL_EMAIL);
        session.save(restriction);
        
        assertFalse(subscriber.shouldSendNewVoicemailEmail());
    }
    
    // SMS notification restriction tests
    
    @Test
    public void test_shouldSendNewVoicemailSms_whenNotificationIsDisabled_returnsFalse()
    {
        subscriber = createSubscriber(session);
        subscriber.setIsSmsNotificationEnabled(false);
        
        assertFalse(subscriber.shouldSendNewVoicemailSms());
    }
    
    @Test
    public void test_shouldSendNewVoicemailSms_whenNotificationIsEnabledButSmsAddressisNull_returnsFalse()
    {
        subscriber = createSubscriber(session);
        subscriber.setIsSmsNotificationEnabled(true);
        subscriber.setSmsAddress(null);
        
        assertFalse(subscriber.shouldSendNewVoicemailSms());
    }
    
    @Test
    public void test_shouldSendNewVoicemailSms_whenNotificationIsEnabledButSmsAddressisBlank_returnsFalse()
    {
        subscriber = createSubscriber(session);
        subscriber.setIsSmsNotificationEnabled(true);
        subscriber.setSmsAddress(" ");
        
        assertFalse(subscriber.shouldSendNewVoicemailSms());
    }
    
    @Test
    public void test_shouldSendNewVoicemailSms_whenEnabledAndNoTimeRestrictionsAreSet_returnsTrue()
    {
        subscriber = createSubscriber(session);
        subscriber.setIsSmsNotificationEnabled(true);
        assert subscriber.getSmsAddress() != null && !subscriber.getSmsAddress().trim().equals("");

        int count = TimeRestriction.queryBySubscriberAndAction(session, subscriber, Action.NEW_VOICEMAIL_SMS).size();
        assert count == 0; // no time restrictions

        assertTrue(subscriber.shouldSendNewVoicemailSms());
    }
    
    @Test
    public void test_shouldSendNewVoicemailSms_whenEnabledAndCurrentTimeIsOutsideOfTheOnlyTimeRestriction_returnsFalse()
    {
        subscriber = createSubscriber(session);
        subscriber.setIsSmsNotificationEnabled(true);
        assert subscriber.getSmsAddress() != null && !subscriber.getSmsAddress().trim().equals("");
        
        LocalTime now = new LocalTime();
        Set<DayOfWeek> days = new HashSet<DayOfWeek>();
        for(DayOfWeek dow : DayOfWeek.values())
        {
            days.add(dow);
        }

        TimeRestriction restriction = TimeRestriction.create(subscriber, now.plusHours(2), now.plusHours(3), days, Action.NEW_VOICEMAIL_SMS);
        session.save(restriction);

        assertFalse(subscriber.shouldSendNewVoicemailSms());
    }
    
    @Test
    public void test_shouldSendNewVoicemailSms_whenEnabledAndCurrentTimeIsInsideOfTheOnlyTimeRestriction_returnsTrue()
    {
        subscriber = createSubscriber(session);
        subscriber.setIsSmsNotificationEnabled(true);
        assert subscriber.getSmsAddress() != null && !subscriber.getSmsAddress().trim().equals("");
        
        Set<DayOfWeek> days = new HashSet<DayOfWeek>();
        DayOfWeek today = jodaDayOfWeekToDayOfWeek(new LocalDate().getDayOfWeek());
        days.add(today);
        
        LocalTime now = new LocalTime();
        TimeRestriction restriction = TimeRestriction.create(subscriber, now.minusHours(1), now.plusHours(1), days, Action.NEW_VOICEMAIL_SMS);
        session.save(restriction);
        
        assertTrue(subscriber.shouldSendNewVoicemailSms());
    }
    
    @Test
    public void test_shouldSendNewVoicemailSms_whenEnabledAndCurrentTimeIsInsideOfTheOnlyTimeRestrictionButRestrictionOnlyAppliesToTomorrow_returnsFalse()
    {
        subscriber = createSubscriber(session);
        subscriber.setIsSmsNotificationEnabled(true);
        assert subscriber.getSmsAddress() != null && !subscriber.getSmsAddress().trim().equals("");
        
        Set<DayOfWeek> days = new HashSet<DayOfWeek>();
        DayOfWeek today = jodaDayOfWeekToDayOfWeek(new LocalDate().plusDays(1).getDayOfWeek());
        days.add(today);
        
        LocalTime now = new LocalTime();
        TimeRestriction restriction = TimeRestriction.create(subscriber, now.minusHours(1), now.plusHours(1), days, Action.NEW_VOICEMAIL_SMS);
        session.save(restriction);
        
        assertFalse(subscriber.shouldSendNewVoicemailSms());
    }
    
    private Subscriber getPopulatedSubscriber()
    {
        Subscriber s = new Subscriber();
        s.setId(System.currentTimeMillis());
        s.setPassword(String.valueOf(System.currentTimeMillis()));
        s.setUsername(String.valueOf(System.currentTimeMillis()));
        s.setVoicemailPin(TestUtil.randomNumeric(4).toString());
        s.setVersion(1);
        return s;
    }
    
    private DayOfWeek jodaDayOfWeekToDayOfWeek(int jodaDayOfWeek)
    {
        switch(jodaDayOfWeek)
        {
            case DateTimeConstants.MONDAY:
                return DayOfWeek.MONDAY;
            case DateTimeConstants.TUESDAY:
                return DayOfWeek.TUESDAY;
            case DateTimeConstants.WEDNESDAY:
                return DayOfWeek.WEDNESDAY;
            case DateTimeConstants.THURSDAY:
                return DayOfWeek.THURSDAY;
            case DateTimeConstants.FRIDAY:
                return DayOfWeek.FRIDAY;
            case DateTimeConstants.SATURDAY:
                return DayOfWeek.SATURDAY;
            case DateTimeConstants.SUNDAY:
                return DayOfWeek.SUNDAY;
            default:
                throw new IllegalArgumentException();
        }
    }
}
