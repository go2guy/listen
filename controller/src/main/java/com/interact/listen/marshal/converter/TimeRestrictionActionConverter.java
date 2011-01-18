package com.interact.listen.marshal.converter;

import com.interact.listen.resource.TimeRestriction.Action;

public class TimeRestrictionActionConverter implements Converter
{
    @Override
    public String marshal(Object value)
    {
        Action action = (Action)value;
        return action.toString();
    }

    @Override
    public Object unmarshal(String value) throws ConversionException
    {
        try
        {
            return Action.valueOf(value);
        }
        catch(IllegalArgumentException e)
        {
            throw new ConversionException(value, "TimeRestrictionAction");
        }
    }
}
