package com.interact.listen.marshal.converter;

import org.joda.time.Duration;

public class JodaDurationConverter implements Converter
{
    @Override
    public String marshal(Object value)
    {
        if(value == null)
        {
            return "";
        }

        Duration duration = (Duration)value;
        return String.valueOf(duration.getMillis());
    }

    @Override
    public Object unmarshal(String value) throws ConversionException
    {
        try
        {
            return new Duration(Long.valueOf(value));
        }
        catch(NumberFormatException e)
        {
            throw new ConversionException(value, "Duration");
        }
    }
}
