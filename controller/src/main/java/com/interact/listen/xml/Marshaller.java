package com.interact.listen.xml;

import com.interact.listen.resource.Resource;
import com.interact.listen.xml.converter.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class Marshaller
{
    /** Global names of methods that should be omitted when marshalling objects */
    private static final List<String> OMIT_METHODS = new ArrayList<String>();

    /** Map of Converters that should be used when marshalling/unmarshalling certain data types */
    private Map<Class<?>, Class<? extends Converter>> converters = new HashMap<Class<?>, Class<? extends Converter>>();

    static
    {
        OMIT_METHODS.add("getClass");
    }

    /**
     * Constructs a new {@code Marshaller}.
     */
    public Marshaller()
    {
        // TODO maybe factor this out into a ConverterRegistry so it's only loaded once (not every time this is
        // constructed)
        converters.put(String.class, StringConverter.class);
        converters.put(Integer.class, IntegerConverter.class);
        converters.put(Long.class, LongConverter.class);
    }

    /**
     * Marshals the provided {@link Resource} to XML.
     * 
     * @param resource {@code Resource} to marshal
     * @return XML string
     */
    public String marshal(Resource resource)
    {
        StringBuilder xml = new StringBuilder();

        // iterate over properties
        Class<? extends Resource> clazz = resource.getClass();
        Method[] methods = clazz.getMethods();
        sortMethods(methods);

        xml.append(marshalOpeningResourceTag(resource, false));

        for(Method method : methods)
        {
            if(!method.getName().startsWith("get"))
            {
                continue;
            }

            if(OMIT_METHODS.contains(method.getName()))
            {
                continue;
            }

            Object result = invokeMethod(method, resource);
            String propertyTag = getTagForMethod(method.getName());

            // FIXME handle other class types here (e.g. Date)
            Class<?> returnType = method.getReturnType();
            if(Resource.class.isAssignableFrom(returnType))
            {
                xml.append(marshalOpeningResourceTag((Resource)result, true));
            }
            else
            {
                xml.append(marshalTag(propertyTag, result));
            }
        }

        String classTag = getTagForClass(clazz.getSimpleName());
        xml.append("</").append(classTag).append(">");

        return xml.toString();
    }

    /**
     * Marshals a single {@link Resource} as a shallow tag (i.e. only containing its {@code href}).
     * 
     * @param resource {@code Resource} to marshal
     * @param selfClosing whether or not the tag should be self-closing
     * @return XML string
     */
    public String marshalOpeningResourceTag(Resource resource, boolean selfClosing)
    {
        String classTag = getTagForClass(resource.getClass().getSimpleName());
        return marshalOpeningResourceTag(classTag, "/" + classTag + "/" + resource.getId(), selfClosing);
    }

    /**
     * Marshals a resource tag from the provided name and href. This can be used for {@code Resource}s that may not have
     * a defined class, like {@code Resource} lists.
     * 
     * @param name resource name
     * @param href resource href
     * @param selfClosing whether or not the tag should be self-closing
     * @return XML string
     */
    public String marshalOpeningResourceTag(String resourceName, String href, boolean selfClosing)
    {
        StringBuilder xml = new StringBuilder();
        xml.append("<").append(resourceName).append(" ");
        xml.append("href=\"").append(href).append("\"");
        if(selfClosing)
        {
            xml.append("/");
        }
        xml.append(">");
        return xml.toString();
    }

    /**
     * Marshals a closing tag for the provided resource name.
     * 
     * @param resourceName resource name
     * @return XML string
     */
    public String marshalClosingResourceTag(String resourceName)
    {
        return "</" + resourceName + ">";
    }

    /**
     * Marshals a single XML tag. If {@code value} is {@code null} a null element (e.g. {@code <foo/>}) is returned.
     * Otherwise the value is returned between XML tags.
     * 
     * @param tagName tag name
     * @param value value
     * @return XML string
     */
    public String marshalTag(String tagName, Object value)
    {
        StringBuilder xml = new StringBuilder();
        if(value == null)
        {
            xml.append("<").append(tagName).append("/>");
        }
        else
        {
            xml.append("<").append(tagName).append(">");
            xml.append(value.toString());
            xml.append("</").append(tagName).append(">");
        }
        return xml.toString();
    }

    private String getTagForClass(String className)
    {
        return className.substring(0, 1).toLowerCase() + className.substring(1);
    }

    /**
     * Assumes {@code methodName} starts with "get", e.g. 'getFoo()'.
     * 
     * @param methodName method name for which to derive field name
     * @return the field name that should be used as the XML tag
     */
    private String getTagForMethod(String methodName)
    {
        String withoutGet = methodName.substring("get".length());
        return withoutGet.substring(0, 1).toLowerCase() + withoutGet.substring(1);
    }

    private Object invokeMethod(Method method, Resource resource)
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

    /**
     * Sorts the provided array of {@link Method}s. Note that the array provided as an argument is modified as a result
     * of this operation.
     * 
     * @param methods {@code Method} array to sort
     */
    private void sortMethods(Method[] methods)
    {
        Arrays.sort(methods, new Comparator<Method>()
        {
            public int compare(Method a, Method b)
            {
                return a.getName().compareTo(b.getName());
            }
        });
    }

    public Resource unmarshal(InputStream inputStream)
    {
        try
        {
            XMLReader reader = XMLReaderFactory.createXMLReader();
            SaxContentHandler contentHandler = new SaxContentHandler(this);
            SaxErrorHandler errorHandler = new SaxErrorHandler();
            reader.setContentHandler(contentHandler);
            reader.setErrorHandler(errorHandler);

            reader.parse(new InputSource(inputStream));

            return contentHandler.getResource();
        }
        catch(SAXException e)
        {
            // FIXME
            throw new RuntimeException(e);
        }
        catch(IOException e)
        {
            // FIXME
            throw new RuntimeException(e);
        }
    }

    public Class<? extends Converter> getConverterClass(Class<?> forClass)
    {
        return converters.get(forClass);
    }
}
