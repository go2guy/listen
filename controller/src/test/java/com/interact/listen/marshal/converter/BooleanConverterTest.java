package com.interact.listen.marshal.converter;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class BooleanConverterTest
{
    private Converter converter;

    @Before
    public void setUp()
    {
        converter = new BooleanConverter();
    }

    @Test
    public void test_marshal_withBooleanTrue_returnsTrueString()
    {
        assertEquals("true", converter.marshal(Boolean.TRUE));
    }

    @Test
    public void test_marshal_withBooleanFalse_returnsFalseString()
    {
        assertEquals("false", converter.marshal(Boolean.FALSE));
    }

    @Test
    public void test_unmarshal_withTrueString_returnsBooleanTrue() throws ConversionException
    {
        assertEquals(Boolean.TRUE, converter.unmarshal("true"));
    }

    @Test
    public void test_unmarshal_withFalseString_returnsBooleanFalse() throws ConversionException
    {
        assertEquals(Boolean.FALSE, converter.unmarshal("false"));
    }

    @Test
    public void test_unmarshal_withUnrecognizedString_returnsBooleanFalse() throws ConversionException
    {
        assertEquals(Boolean.FALSE, converter.unmarshal("tiddlywinks"));
    }
}
