package com.interact.listen.marshal;

import com.interact.listen.marshal.converter.*;
import com.interact.listen.marshal.json.JsonMarshaller;
import com.interact.listen.marshal.xml.XmlMarshaller;
import com.interact.listen.resource.Resource;
import com.interact.listen.resource.ResourceList;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.*;

import org.apache.commons.lang.StringEscapeUtils;

public abstract class Marshaller
{
    /** Global names of methods that should be omitted when marshalling objects */
    protected static final List<String> OMIT_METHODS = new ArrayList<String>();

    /** Map of Converters that should be used when marshalling/unmarshalling certain data types */
    private static Map<Class<?>, Class<? extends Converter>> converters = new HashMap<Class<?>, Class<? extends Converter>>();

    static
    {
        OMIT_METHODS.add("getClass");
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
    public abstract String marshal(ResourceList list, Class<? extends Resource> resourceClass);

    /**
     * Unmarshals the content of the provided {@code InputStream} into a resource.
     * 
     * @param inputStream input stream containing marshalled content
     * @param asResource resource type to unmarshal as
     * @return unmarshalled {@link Resource}
     */
    public abstract Resource unmarshal(InputStream inputStream, Class<? extends Resource> asResource)
        throws MalformedContentException;

    /**
     * Returns the content type that is marshalled by this marshaller.
     * @return content type
     */
    public abstract String getContentType();

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

        if(contentType.startsWith("application/xml"))
        {
            return new XmlMarshaller();
        }
        else if(contentType.startsWith("application/json"))
        {
            return new JsonMarshaller();
        }

        throw new MarshallerNotFoundException(contentType);
    }

    /**
     * Sorts the provided array of {@link Method}s. Note that the array provided as an argument is modified as a result
     * of this operation.
     * 
     * @param methods {@code Method} array to sort
     */
    protected final void sortMethods(Method[] methods)
    {
        Arrays.sort(methods, new Comparator<Method>()
        {
            public int compare(Method a, Method b)
            {
                return a.getName().compareTo(b.getName());
            }
        });
    }

    /**
     * Gets the tag name that should be used for the provided class name.
     * 
     * @param className class name
     * @return tag representing the class
     */
    protected final String getTagForClass(String className)
    {
        return className.substring(0, 1).toLowerCase() + className.substring(1);
    }

    /**
     * Assumes {@code methodName} starts with "get", e.g. 'getFoo()'.
     * 
     * @param methodName method name for which to derive field name
     * @return the field name that should be used as the XML tag
     */
    protected final String getTagForMethod(String methodName)
    {
        String withoutGet = methodName.substring("get".length());
        return withoutGet.substring(0, 1).toLowerCase() + withoutGet.substring(1);
    }

    /**
     * Reflectively calls the provided {@code Method} on the provided {@code Resource}.
     * 
     * @param method method to invoke
     * @param resource resource to call method on
     * @return method's return value
     */
    protected final Object invokeMethod(Method method, Resource resource)
    {
        try
        {
            return method.invoke(resource);
        }
        catch(InvocationTargetException e)
        {
            throw new AssertionError("InvocationTargetException when calling [" + method.getName() + "] of Resource [" +
                                     resource.getClass().getName() + "]");
        }
        catch(IllegalAccessException e)
        {
            throw new AssertionError("IllegalAccessException when calling [" + method.getName() + "] of Resource [" +
                                     resource.getClass().getName() + "]");
        }
    }

    public static final Class<? extends Converter> getConverterClass(Class<?> forClass)
    {
        return converters.get(forClass);
    }

    public static final Long getIdFromHref(String href)
    {
        if(href == null)
        {
           return null;
        }

        boolean hasId = href.matches("\\/[^\\/]+\\/[^\\/]+");
        if(!hasId)
        {
            return null;
        }

        Long id = Long.parseLong(href.substring(href.lastIndexOf("/") + 1));
        return id;
    }

    public static final Method findMethod(String name, Class clazz)
    {
        for(Method method : clazz.getMethods())
        {
            if(method.getName().equals(name))
            {
                return method;
            }
        }
        return null;
    }

    public static final String convert(Class<?> forClass, Object value)
    {
        try
        {
            Class<? extends Converter> converterClass = Marshaller.getConverterClass(forClass);
            Converter converter = converterClass.newInstance();
            return converter.marshal(value);
        }
        catch(IllegalAccessException e)
        {
            throw new AssertionError(e);
        }
        catch(InstantiationException e)
        {
            throw new AssertionError(e);
        }
    }

    public static final String escapeXml(String toEscape)
    {
        return StringEscapeUtils.escapeXml(toEscape);
    }

    public static final String encodeUrl(String toEncode)
    {
        try
        {
            return URLEncoder.encode(toEncode, "UTF-8");
        }
        catch(UnsupportedEncodingException e)
        {
            throw new AssertionError(e);
        }
    }

    protected final String buildSpecificHref(String resource, Long id)
    {
        return "/" + resource + "/" + id;
    }

    protected final String buildListHref(String resource, int first, int max, String fields, String properties)
    {
        StringBuilder href = new StringBuilder();
        href.append("/").append(resource).append("?");
        href.append("_first=").append(first);
        href.append("&_max=").append(max);

        if(fields != null && fields.length() > 0)
        {
            href.append("&_fields=").append(fields);
        }

        if(properties != null && properties.length() > 0)
        {
            href.append("&").append(properties);
        }

        return href.toString();
    }
}
