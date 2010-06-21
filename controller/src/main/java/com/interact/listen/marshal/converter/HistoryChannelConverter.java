package com.interact.listen.marshal.converter;

import com.interact.listen.history.Channel;

public class HistoryChannelConverter implements Converter
{
    @Override
    public String marshal(Object value)
    {
        Channel channel = (Channel)value;
        return channel.toString();
    }

    @Override
    public Object unmarshal(String value) throws ConversionException
    {
        try
        {
            return Channel.valueOf(value);
        }
        catch(IllegalArgumentException e)
        {
            throw new ConversionException(value, "Channel");
        }
    }
}
