package com.interact.listen.resource;

import static org.junit.Assert.*;

import com.interact.listen.resource.Pin.PinType;

import org.junit.Before;
import org.junit.Test;

public class PinTest
{
    private Pin pin;

    @Before
    public void setUp()
    {
        pin = new Pin();
    }

    @Test
    public void test_version_defaultsToZero()
    {
        assertEquals(Integer.valueOf(0), pin.getVersion());
    }

    @Test
    public void test_newInstance_returnsPopulatedPin()
    {
        final String number = String.valueOf(System.currentTimeMillis());
        final PinType type = PinType.PASSIVE;
        pin = Pin.newInstance(number, type);

        assertEquals(number, pin.getNumber());
        assertEquals(type, pin.getType());
    }

    @Test
    public void test_setNumber_withValidNumber_setsNumber()
    {
        final String number = String.valueOf(System.currentTimeMillis());
        pin.setNumber(number);

        assertEquals(number, pin.getNumber());
    }

    @Test
    public void test_setId_withValidId_setsId()
    {
        final Long id = System.currentTimeMillis();
        pin.setId(id);

        assertEquals(id, pin.getId());
    }

    @Test
    public void test_setVersion_withValidVersion_setsVersion()
    {
        final Integer version = 1;
        pin.setVersion(version);

        assertEquals(version, pin.getVersion());
    }

    @Test
    public void test_setType_withValidType_setsType()
    {
        final PinType type = PinType.ADMIN;
        pin.setType(type);

        assertEquals(type, pin.getType());
    }

    @Test
    public void test_validate_withNullNumber_returnsFalseAndHasErrors()
    {
        pin = getPopulatedPin();
        pin.setNumber(null);

        assertFalse(pin.validate());
        assertTrue(pin.hasErrors());
    }

    @Test
    public void test_validate_withValidPin_returnsTrueAndHasNoErrors()
    {
        pin = getPopulatedPin();
        assertTrue(pin.validate());
        assertFalse(pin.hasErrors());
    }

    @Test
    public void test_validate_withBlankNumber_returnsFalseAndHasErrors()
    {
        pin = getPopulatedPin();
        pin.setNumber("  ");

        assertFalse(pin.validate());
        assertTrue(pin.hasErrors());
    }

    @Test
    public void test_validate_withNullType_returnsFalseAndHasErrors()
    {
        pin = getPopulatedPin();
        pin.setType(null);

        assertFalse(pin.validate());
        assertTrue(pin.hasErrors());
    }

    @Test
    public void test_validate_withNullConference_returnsFalseAndHasErrors()
    {
        pin = getPopulatedPin();
        pin.setConference(null);

        assertFalse(pin.validate());
        assertTrue(pin.hasErrors());
    }

    @Test
    public void test_copy_withoutIdAndVersion_createsShallowCopyWithoutIdAndVersion()
    {
        Pin original = getPopulatedPin();
        Pin copy = original.copy(false);

        assertTrue(original.getConference() == copy.getConference()); // same reference
        assertEquals(original.getNumber(), copy.getNumber());
        assertEquals(original.getType(), copy.getType());

        assertNull(copy.getId());
        assertEquals(Integer.valueOf(0), copy.getVersion());
    }

    @Test
    public void test_copy_withIdAndVersion_createsShallowCopyWithIdAndVersion()
    {
        Pin original = getPopulatedPin();
        Pin copy = original.copy(true);

        assertEquals(original.getId(), copy.getId());
        assertEquals(original.getVersion(), copy.getVersion());
    }

    @Test
    public void test_equals_sameObject_returnsTrue()
    {
        assertTrue(pin.equals(pin));
    }

    @Test
    public void test_equals_thatNull_returnsFalse()
    {
        assertFalse(pin.equals(null));
    }

    @Test
    public void test_equals_thatNotAPin_returnsFalse()
    {
        assertFalse(pin.equals(new String()));
    }

    @Test
    public void test_equals_numberNotEqual_returnsFalse()
    {
        pin.setNumber(String.valueOf(System.currentTimeMillis()));

        Pin that = new Pin();
        that.setNumber(null);

        assertFalse(pin.equals(that));
    }

    @Test
    public void test_equals_allPropertiesEqual_returnsTrue()
    {
        String number = String.valueOf(System.currentTimeMillis());

        pin.setNumber(number);

        Pin that = new Pin();
        that.setNumber(number);

        assertTrue(pin.equals(that));
    }

    private Pin getPopulatedPin()
    {
        Conference c = new Conference();

        Pin p = new Pin();
        p.setId(System.currentTimeMillis());
        p.setVersion(1);
        p.setNumber(String.valueOf(System.currentTimeMillis()));
        p.setType(PinType.ACTIVE);
        p.setConference(c);
        return p;
    }
}
