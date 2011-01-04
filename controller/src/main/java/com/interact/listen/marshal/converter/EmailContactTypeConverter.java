package com.interact.listen.marshal.converter;

import com.interact.listen.contacts.resource.EmailContact.Type;

public class EmailContactTypeConverter implements Converter
{

    @Override
    public String marshal(Object value)
    {
        Type order = (Type)value;
        return order.toString();
    }

    @Override
    public Object unmarshal(String value) throws ConversionException
    {
        try
        {
            return Type.valueOf(value);
        }
        catch(IllegalArgumentException e)
        {
            throw new ConversionException(value, "EmailContactType");
        }
    }

}
