package com.interact.listen.marshal.converter;

import org.joda.time.LocalTime;

public class JodaLocalTimeConverter implements Converter
{
    @Override
    public String marshal(Object value)
    {
        if(value == null)
        {
            return "";
        }

        LocalTime time = (LocalTime)value;
        return String.valueOf(time.getHourOfDay() + ":" + time.getMinuteOfHour());
    }

    @Override
    public Object unmarshal(String value) throws ConversionException
    {
        try
        {
            String[] parts = value.split(":");
            if(parts.length != 2)
            {
                throw new ConversionException(value, "LocalTime");
            }
            return new LocalTime(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        }
        catch(NumberFormatException e)
        {
            throw new ConversionException(value, "LocalTime");
        }
    }
}
