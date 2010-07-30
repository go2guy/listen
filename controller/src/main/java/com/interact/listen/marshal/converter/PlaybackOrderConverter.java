package com.interact.listen.marshal.converter;

import com.interact.listen.resource.Subscriber.PlaybackOrder;

public class PlaybackOrderConverter implements Converter
{
    @Override
    public String marshal(Object value)
    {
        PlaybackOrder order = (PlaybackOrder)value;
        return order.toString();
    }

    @Override
    public Object unmarshal(String value) throws ConversionException
    {
        try
        {
            return PlaybackOrder.valueOf(value);
        }
        catch(IllegalArgumentException e)
        {
            throw new ConversionException(value, "PlaybackOrder");
        }
    }
}
