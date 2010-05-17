package com.interact.listen.marshal.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.interact.listen.resource.ListenSpotSubscriber.PhoneNumberProtocolType;

import org.junit.Before;
import org.junit.Test;

public class PhoneNumberProtocolTypeConverterTest
{
    private Converter converter;

    @Before
    public void setUp()
    {
        converter = new PhoneNumberProtocolTypeConverter();
    }

    @Test
    public void test_marshal_withVoip_returnsVoipString()
    {
        assertEquals("VOIP", converter.marshal(PhoneNumberProtocolType.VOIP));
    }

    @Test
    public void test_marshal_withPstn_returnsPstnString()
    {
        assertEquals("PSTN", converter.marshal(PhoneNumberProtocolType.PSTN));
    }

    @Test
    public void test_unmarshal_withVoipString_returnsVoipProtocolType() throws ConversionException
    {
        assertEquals(PhoneNumberProtocolType.VOIP, converter.unmarshal("VOIP"));
    }

    @Test
    public void test_unmarshal_withPstnString_returnsPstnProtocolType() throws ConversionException
    {
        assertEquals(PhoneNumberProtocolType.PSTN, converter.unmarshal("PSTN"));
    }

    @Test
    public void test_unmarshal_withUnrecognizedString_throwsConversionExceptionWithMessage()
    {
        final String toUnmarshal = "WAFFLES";
        try
        {
            converter.unmarshal(toUnmarshal);
            fail("Expected ConversionException for unrecognized PhoneNumberProtocolType");
        }
        catch(ConversionException e)
        {
            assertEquals("Could not convert value [" + toUnmarshal + "] to type [PhoneNumberProtocolType]",
                         e.getMessage());
        }
    }
}
