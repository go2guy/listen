package com.interact.listen.marshal.converter;

import com.interact.listen.resource.DeviceRegistration.RegisteredType;

public class DeviceRegisteredTypeConverter implements Converter
{

    @Override
    public String marshal(Object value)
    {
        RegisteredType order = (RegisteredType)value;
        return order.toString();
    }

    @Override
    public Object unmarshal(String value) throws ConversionException
    {
        try
        {
            return RegisteredType.valueOf(value);
        }
        catch(IllegalArgumentException e)
        {
            throw new ConversionException(value, "DeviceRegisteredType");
        }
    }

}
