package com.interact.listen.xml.converter;

public interface Converter
{
    public String marshal(Object value);
    public Object unmarshal(String value);
}
