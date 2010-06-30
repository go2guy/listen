package com.interact.listen.marshal.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Converts a Date to/from ISO8601 format.
 */
public class Iso8601DateConverter implements Converter
{
    /** Format to use when converting */
    public static final String ISO8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    @Override
    public String marshal(Object value)
    {
        if(value != null)
        {
            Date date = (Date)value;
            SimpleDateFormat sdf = new SimpleDateFormat(ISO8601_FORMAT);
            return sdf.format(date);
        }
        
        return null;
    }

    @Override
    public Object unmarshal(String value) throws ConversionException
    {
        if(value == null)
        {
            return null;
        }
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
