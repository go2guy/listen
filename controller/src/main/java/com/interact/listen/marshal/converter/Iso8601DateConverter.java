package com.interact.listen.marshal.converter;

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
    public Object unmarshal(String value) throws ConversionException
    {
        SimpleDateFormat sdf = new SimpleDateFormat(ISO8601_FORMAT);
        try
        {
            return sdf.parse(value);
        }
        catch(ParseException e)
        {
            throw new ConversionException(value, "Date");
        }
    }
}
