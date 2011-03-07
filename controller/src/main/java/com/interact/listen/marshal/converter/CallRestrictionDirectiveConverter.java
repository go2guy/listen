package com.interact.listen.marshal.converter;

import com.interact.listen.resource.CallRestriction.Directive;

public class CallRestrictionDirectiveConverter implements Converter
{
    @Override
    public String marshal(Object value)
    {
        Directive directive = (Directive)value;
        return directive.toString();
    }

    @Override
    public Object unmarshal(String value) throws ConversionException
    {
        try
        {
            return Directive.valueOf(value);
        }
        catch(IllegalArgumentException e)
        {
            throw new ConversionException(value, "CallRestrictionDirective");
        }
    }
}