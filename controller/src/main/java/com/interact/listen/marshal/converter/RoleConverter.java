package com.interact.listen.marshal.converter;

import com.interact.listen.resource.Subscriber.Role;

public class RoleConverter implements Converter
{
    @Override
    public String marshal(Object value)
    {
        Role role = (Role)value;
        return role.toString();
    }

    @Override
    public Object unmarshal(String value) throws ConversionException
    {
        try
        {
            return Role.valueOf(value);
        }
        catch(IllegalArgumentException e)
        {
            throw new ConversionException(value, "Role");
        }
    }
}