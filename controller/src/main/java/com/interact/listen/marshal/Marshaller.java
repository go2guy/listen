package com.interact.listen.marshal;

import com.interact.listen.marshal.converter.*;
import com.interact.listen.marshal.json.JsonMarshaller;
import com.interact.listen.marshal.xml.XmlMarshaller;
import com.interact.listen.resource.Resource;

import java.io.InputStream;
import java.util.*;

public abstract class Marshaller
{
    /** Global names of methods that should be omitted when marshalling objects */
    protected static final List<String> OMIT_METHODS = new ArrayList<String>();

    /** Map of Converters that should be used when marshalling/unmarshalling certain data types */
    protected Map<Class<?>, Class<? extends Converter>> converters =
                                                                     new HashMap<Class<?>, Class<? extends Converter>>();

    static
    {
        OMIT_METHODS.add("getClass");
    }

    public Marshaller()
    {
        converters.put(Boolean.class, BooleanConverter.class);
        converters.put(Date.class, Iso8601DateConverter.class);
        converters.put(Integer.class, IntegerConverter.class);
        converters.put(Long.class, LongConverter.class);
        converters.put(String.class, StringConverter.class);
    }

    /**
     * Marshals the provided {@link Resource}.
     * 
     * @param resource {@code Resource} to marshal
     * @return marshalled string
     */
    public abstract String marshal(Resource resource);

    /**
     * Marshals a {@code List} of {@link Resource}s.
     * 
     * @param list list of resources
     * @param resourceClass resource class that the list contains
     * @return marshalled string
     */
    public abstract String marshal(List<Resource> list, Class<? extends Resource> resourceClass);

    /**
     * Unmarshals the content of the provided {@code InputStream} into a resource.
     * 
     * @param inputStream input stream containing marshalled content
     * @return unmarshalled {@link Resource}
     */
    public abstract Resource unmarshal(InputStream inputStream);

    /**
     * Factory method for retrieving the correct {@code Marshaller} implementation for a given content type.
     * 
     * @param contentType content type for which to create {@code Marshaller}
     * @return {@code Marshaller} implementation for the provided content type
     * @throws MarshallerNotFoundException
     */
    public static Marshaller createMarshaller(String contentType) throws MarshallerNotFoundException
    {
        if(contentType == null)
        {
            throw new MarshallerNotFoundException("null");
        }

        if(contentType.equals("application/xml"))
        {
            return new XmlMarshaller();
        }
        else if(contentType.equals("application/json"))
        {
            return new JsonMarshaller();
        }

        throw new MarshallerNotFoundException(contentType);
    }
}
