package com.interact.listen.marshal;

import com.interact.listen.contacts.resource.EmailContact;
import com.interact.listen.history.Channel;
import com.interact.listen.marshal.converter.*;
import com.interact.listen.marshal.json.JsonMarshaller;
import com.interact.listen.marshal.xml.XmlMarshaller;
import com.interact.listen.resource.*;
import com.interact.listen.resource.DeviceRegistration.DeviceType;
import com.interact.listen.resource.Subscriber.PlaybackOrder;
import com.interact.listen.resource.Subscriber.Role;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

import org.apache.log4j.Logger;
import org.joda.time.Duration;
import org.joda.time.LocalTime;

/**
 * Marshals {@code Resource}s into various content types and unmarshals {@code String}s back into {@code Resource}s.
 */
public abstract class Marshaller
{
    /** Global names of methods that should be omitted when marshalling objects */
    protected static final List<String> OMIT_METHODS = new ArrayList<String>();

    /** Class logger */
    private static final Logger LOG = Logger.getLogger(Marshaller.class);

    /** Default Converters that should be used when marshalling/unmarshalling certain data types */
    private static final Map<Class<?>, Class<? extends Converter>> DEFAULT_CONVERTERS = new HashMap<Class<?>, Class<? extends Converter>>();

    /** Converters that should override default converters when marshalling/unmarshalling certain data types */
    private Map<Class<?>, Class<? extends Converter>> converters = new HashMap<Class<?>, Class<? extends Converter>>();

    static
    {
        OMIT_METHODS.add("getClass");
        DEFAULT_CONVERTERS.put(AccessNumber.NumberType.class, AccessNumberTypeConverter.class);
        DEFAULT_CONVERTERS.put(Boolean.class, BooleanConverter.class);
        DEFAULT_CONVERTERS.put(CallDetailRecord.CallDirection.class, CallDirectionConverter.class);
        DEFAULT_CONVERTERS.put(Channel.class, HistoryChannelConverter.class);
        DEFAULT_CONVERTERS.put(Date.class, Iso8601DateConverter.class);
        DEFAULT_CONVERTERS.put(DeviceRegistration.RegisteredType.class, DeviceRegisteredTypeConverter.class);
        DEFAULT_CONVERTERS.put(DeviceType.class, DeviceTypeConverter.class);
        DEFAULT_CONVERTERS.put(Duration.class, JodaDurationConverter.class);
        DEFAULT_CONVERTERS.put(EmailContact.Type.class, EmailContactTypeConverter.class);
        DEFAULT_CONVERTERS.put(Integer.class, IntegerConverter.class);
        DEFAULT_CONVERTERS.put(LocalTime.class, JodaLocalTimeConverter.class);
        DEFAULT_CONVERTERS.put(Long.class, LongConverter.class);
        DEFAULT_CONVERTERS.put(Pin.PinType.class, PinTypeConverter.class);
        DEFAULT_CONVERTERS.put(PlaybackOrder.class, PlaybackOrderConverter.class);
        DEFAULT_CONVERTERS.put(String.class, StringConverter.class);
        DEFAULT_CONVERTERS.put(TimeRestriction.Action.class, TimeRestrictionActionConverter.class);
        DEFAULT_CONVERTERS.put(CallRestriction.Directive.class, CallRestrictionDirectiveConverter.class);
        DEFAULT_CONVERTERS.put(Role.class, RoleConverter.class);
    }

    /**
     * Marshals the provided {@link Resource}.
     * 
     * @param resource {@code Resource} to marshal
     * @return marshalled {@code String}
     */
    public abstract String marshal(Resource resource);

    /**
     * Marshals a {@code List} of {@link Resource}s.
     * 
     * @param list list of resources
     * @param resourceClass resource class that the list contains
     * @return marshalled {@code String}
     */
    public abstract String marshal(ResourceList list, Class<? extends Resource> resourceClass);

    /**
     * Unmarshals the content of the provided {@code InputStream} into a resource.
     * 
     * @param inputStream input stream containing marshalled content
     * @param resource resource to unmarshal stream into
     * @param unmarshalId whether or not to unmarshal the id from the href
     * @return unmarshalled {@link Resource}
     */
    public abstract Resource unmarshal(InputStream inputStream, Resource resource, boolean unmarshalId)
        throws MalformedContentException;

    /**
     * Returns the content type that is marshalled by this {@code Marshaller}.
     * 
     * @return content type
     */
    public abstract String getContentType();

    /**
     * Escapes the provided value according to the marshaller's escaping needs. For example, a JSON marshaller might
     * add backslashes before double-quotes if values are typically already enclosed in quotes.
     * 
     * @param value value to escape
     * @return escaped value
     */
    public abstract String escape(String value);

    /**
     * Factory method for retrieving the correct {@code Marshaller} implementation for a given content type.
     * 
     * @param contentType content type for which to create {@code Marshaller}
     * @return {@code Marshaller} implementation for the provided content type
     * @throws MarshallerNotFoundException if no implementation is found for the provided content type
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
     * Sorts the provided array of {@link Method}s. <b>The {@code Method[]} array provided as an argument is modified as
     * a result of this operation.</b>
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
    public static final String getTagForClass(String className)
    {
        return className.substring(0, 1).toLowerCase() + className.substring(1);
    }

    /**
     * Assumes {@code methodName} starts with "get", e.g. 'getFoo()'.
     * 
     * @param methodName method name for which to derive field name
     * @return the field name that should be used as the XML tag
     */
    public static final String getTagForMethod(String methodName)
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
            LOG.error(e);
            throw new AssertionError("InvocationTargetException when calling [" + method.getName() + "] of Resource [" +
                                     resource.getClass().getName() + "]");
        }
        catch(IllegalAccessException e)
        {
            LOG.error(e);
            throw new AssertionError("IllegalAccessException when calling [" + method.getName() + "] of Resource [" +
                                     resource.getClass().getName() + "]");
        }
    }

    /**
     * Registers a {@link Converter} class to be used for the provided {@code class} when marshalling/unmarshalling. The
     * provided {@code Converter} will override any dfault {@code Converter}s that may be defined for that class.
     * 
     * @param forClass {@code class} to register {@code Converter} for
     * @param converterClass {@code Converter} to use
     */
    public final void registerConverterClass(Class<?> forClass, Class<? extends Converter> converterClass)
    {
        converters.put(forClass, converterClass);
    }

    /**
     * Retrieves the {@link Converter} class that should be used when marshalling/unmarshalling the provided {@code
     * class}. {@code Converter}s registered with {@link #registerConverterClass(Class, Class)} are searched first, and
     * if none are found the appropriate default {@code Converter} is returned. If no default is found, {@code null} is
     * returned.
     * 
     * @param forClass {@code class} to get {@code Converter} for
     * @return appropriate {@code Converter} class
     */
    public final Class<? extends Converter> getConverterClass(Class<?> forClass)
    {
       if(converters.containsKey(forClass))
        {
            return converters.get(forClass);
        }

        return Marshaller.getDefaultConverterClass(forClass);
    }

    /**
     * Retrieves the default {@link Converter} class for the provided {@code class}.
     * 
     * @param forClass {@code class} to get default {@code Converter} for
     * @return default {@code Converter} class
     */
    public static final Class<? extends Converter> getDefaultConverterClass(Class<?> forClass)
    {
        return DEFAULT_CONVERTERS.get(forClass);
    }

    /**
     * Given an href, retrieves the id component of it. If the id cannot be found or the href is malformed, {@code null}
     * is returned.
     * 
     * @param href href to parse id from
     * @return id, or {@code null} if id cannot be parsed
     * @throws NumberFormatException if the id is not parseable as a {@code Long}
     */
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

    /**
     * Given a {@code class}, looks for a method with the provided name.
     *  
     * @param name method name to search for
     * @param clazz {@code class} to search for method on
     * @return {@code Method} with provided name, or {@code null} if no method found
     */
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

    /**
     * Marshals the provided value with the {@link Converter} for the provided {@code class}. The {@code Converter} used
     * is determined by the same means as {@link #getConverterClass(Class)}. Also escapes the value according to the
     * implementation's {{@link #escape(String)} method.
     * 
     * @param forClass {@code class} to find {@code Converter} for to marshal value
     * @param value value to marshal
     * @return marshalled and escaped value
     */
    public final String convertAndEscape(Class<?> forClass, Object value)
    {
        try
        {
            Class<? extends Converter> converterClass = getConverterClass(forClass);
            Converter converter = converterClass.newInstance();
            String marshalled = converter.marshal(value);
            return escape(marshalled);
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

    public static final String decodeUrl(String toDecode)
    {
        try
        {
            return URLDecoder.decode(toDecode, "UTF-8");
        }
        catch(UnsupportedEncodingException e)
        {
            throw new AssertionError(e);
        }
    }

    public static final String buildHref(Resource resource)
    {
        String name = getTagForClass(resource.getClass().getSimpleName());
        return "/" + name + "s/" + resource.getId();
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
