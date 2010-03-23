package com.interact.listen.xml.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Iso8601DateConverter implements Converter
{
    public static final String ISO8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    @Override
    public String marshal(Object value)
    {
        Date date = (Date)value;
        SimpleDateFormat sdf = new SimpleDateFormat(ISO8601_FORMAT);
        return sdf.format(date);
    }

    @Override
    public Object unmarshal(String value)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(ISO8601_FORMAT);
        try
        {
            return sdf.parse(value);
        }
        catch(ParseException e)
        {
            // FIXME create some sort of ConversionException
            throw new RuntimeException(e);
        }
    }
}
