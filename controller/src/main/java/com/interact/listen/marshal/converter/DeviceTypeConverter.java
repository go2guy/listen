package com.interact.listen.marshal.converter;

import com.interact.listen.resource.DeviceRegistration.DeviceType;

public class DeviceTypeConverter implements Converter
{

    @Override
    public String marshal(Object value)
    {
        DeviceType order = (DeviceType)value;
        return order.toString();
    }

    @Override
    public Object unmarshal(String value) throws ConversionException
    {
        try
        {
            return DeviceType.valueOf(value);
        }
        catch(IllegalArgumentException e)
        {
            throw new ConversionException(value, "DeviceType");
        }
    }

}
