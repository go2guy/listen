package com.interact.listen.marshal.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Converts a Date to/from a readable ISO8601 format (readable = not containing 'T' or milliseconds).
 */
public class FriendlyIso8601DateConverter implements Converter
{
    /** Format to use when converting */
    public static final String ISO8601_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Override
    public String marshal(Object value)
    {
        if(value != null)
        {
            Date date = (Date)value;
            SimpleDateFormat sdf = new SimpleDateFormat(ISO8601_FORMAT);
            return sdf.format(date);
        }

        return "";
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
