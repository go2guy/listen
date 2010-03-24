package com.interact.listen.marshal.converter;

public interface Converter
{
    public String marshal(Object value);
    public Object unmarshal(String value) throws ConversionException;
}
