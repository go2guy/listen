package com.interact.listen.marshal.converter;

import com.interact.listen.resource.AccessNumber.NumberType;

public class AccessNumberTypeConverter implements Converter
{

    @Override
    public String marshal(Object value)
    {
        NumberType order = (NumberType)value;
        return order.toString();
    }

    @Override
    public Object unmarshal(String value) throws ConversionException
    {
        try
        {
            return NumberType.valueOf(value);
        }
        catch(IllegalArgumentException e)
        {
            throw new ConversionException(value, "NumberType");
        }
    }

}
