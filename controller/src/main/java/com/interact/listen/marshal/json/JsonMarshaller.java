package com.interact.listen.marshal.json;

import com.interact.listen.marshal.Marshaller;
import com.interact.listen.resource.Resource;

import java.io.InputStream;
import java.util.List;

public class JsonMarshaller extends Marshaller
{
    @Override
    public String marshal(Resource resource)
    {
        // FIXME implement
        return "";
    }

    @Override
    public String marshal(List<Resource> list, Class<? extends Resource> resourceClass)
    {
        // FIXME implement
        return "";
    }

    @Override
    public Resource unmarshal(InputStream inputStream)
    {
        // FIXME implement
        return null;
    }
}
