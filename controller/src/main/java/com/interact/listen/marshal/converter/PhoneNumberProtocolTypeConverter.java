package com.interact.listen.marshal.converter;

import com.interact.listen.resource.ListenSpotSubscriber.PhoneNumberProtocolType;

public class PhoneNumberProtocolTypeConverter implements Converter
{
    @Override
    public String marshal(Object value)
    {
        PhoneNumberProtocolType type = (PhoneNumberProtocolType)value;
        return type.toString();
    }

    @Override
    public Object unmarshal(String value) throws ConversionException
    {
        try
        {
            return PhoneNumberProtocolType.valueOf(value);
        }
        catch(IllegalArgumentException e)
        {
            throw new ConversionException(value, "PhoneNumberProtocolType");
        }
    }
}
