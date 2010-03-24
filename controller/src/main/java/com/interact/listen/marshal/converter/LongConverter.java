package com.interact.listen.marshal.converter;

public class LongConverter implements Converter
{
    @Override
    public String marshal(Object value)
    {
        return String.valueOf(value);
    }

    @Override
    public Object unmarshal(String value)
    {
        return Long.parseLong(value);
    }
}
