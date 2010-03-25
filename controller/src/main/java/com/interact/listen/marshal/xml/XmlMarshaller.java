package com.interact.listen.marshal.xml;

import com.interact.listen.ListenRuntimeException;
import com.interact.listen.marshal.MalformedContentException;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.converter.ConversionException;
import com.interact.listen.marshal.converter.Converter;
import com.interact.listen.resource.Resource;
import com.interact.listen.resource.ResourceList;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class XmlMarshaller extends Marshaller
{
    @Override
    public String marshal(Resource resource)
    {
        try
        {
            StringBuilder xml = new StringBuilder();

            // iterate over properties
            Class<? extends Resource> clazz = resource.getClass();
            Method[] methods = clazz.getMethods();
            sortMethods(methods);

            xml.append(marshalOpeningResourceTag(resource, false));

            for(Method method : methods)
            {
                if(!method.getName().startsWith("get") || OMIT_METHODS.contains(method.getName()))
                {
                    continue;
                }

                Object result = invokeMethod(method, resource);
                String propertyTag = getTagForMethod(method.getName());

                Class<?> returnType = method.getReturnType();
                if(Resource.class.isAssignableFrom(returnType))
                {
                    if(result == null)
                    {
                        xml.append(marshalTag(getTagForClass(returnType.getSimpleName()), null));
                    }
                    else
                    {
                        xml.append(marshalOpeningResourceTag((Resource)result, true));
                    }
                }
                else
                {
                    Class<? extends Converter> converterClass = getConverterClass(returnType);
                    Converter converter = converterClass.newInstance();
                    String resultString = converter.marshal(result);

                    xml.append(marshalTag(propertyTag, resultString));
                }
            }

            String classTag = getTagForClass(clazz.getSimpleName());
            xml.append("</").append(classTag).append(">");

            return xml.toString();
        }
        catch(IllegalAccessException e)
        {
            throw new AssertionError("IllegalAccessException: " + e);
        }
        catch(InstantiationException e)
        {
            throw new AssertionError("InstantiationException: " + e);
        }
    }

    @Override
    public String marshal(ResourceList list, Class<? extends Resource> resourceClass)
    {
        if(list == null)
        {
            throw new IllegalArgumentException("List cannot be null");
        }

        String tag = getTagForClass(resourceClass.getSimpleName()) + "s"; // pluralize it for the list

        StringBuilder xml = new StringBuilder();
        xml.append("<").append(tag).append(" href=\"/").append(tag).append("?");
        xml.append("_first=").append(list.getFirst()).append("&");
        xml.append("_max=").append(list.getMax());
        String fields = list.getFieldsForQuery();
        if(fields.length() > 0)
        {
            xml.append("&").append("_fields=").append(list.getFieldsForQuery());
        }
        String properties = list.getSearchPropertiesForQuery();
        if(properties.length() > 0)
        {
            xml.append("&").append(list.getSearchPropertiesForQuery());
        }
        xml.append("\" ");
        xml.append("count=\"").append(list.getList().size()).append("\"");
//        xml.append("total=\"").append(list.getTotal()).append("\"");

        if(list.getList().size() == 0)
        {
            xml.append("/>");
        }
        else
        {
            xml.append(">");
        }

        for(Resource resource : list.getList())
        {
            xml.append(marshalOpeningResourceTag(resource, true));
        }
        
        if(list.getList().size() > 0)
        {
            xml.append(marshalClosingResourceTag(tag));
        }

        return xml.toString();
    }

    /**
     * Marshals a single {@link Resource} as a shallow tag (i.e. only containing its {@code href}).
     * 
     * @param resource {@code Resource} to marshal
     * @param selfClosing whether or not the tag should be self-closing
     * @return XML string
     */
    private String marshalOpeningResourceTag(Resource resource, boolean selfClosing)
    {
        String classTag = getTagForClass(resource.getClass().getSimpleName());
        return marshalOpeningResourceTag(classTag, "/" + classTag + "s/" + resource.getId(), selfClosing);
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
    private String marshalOpeningResourceTag(String resourceName, String href, boolean selfClosing)
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
    private String marshalClosingResourceTag(String resourceName)
    {
        return "</" + resourceName + ">";
    }

    /**
     * Marshals a single XML tag. If {@code value} is {@code null} a null element (e.g. {@code <foo nil="true"/>}) is returned.
     * Otherwise the value is returned between XML tags.
     * 
     * @param tagName tag name
     * @param value value
     * @return XML string
     */
    private String marshalTag(String tagName, Object value)
    {
        StringBuilder xml = new StringBuilder();
        if(value == null)
        {
            // FIXME make this xsi:nil="true" -> we need to define the namespace in the XML, though
            xml.append("<").append(tagName).append(" nil=\"true\"/>");
        }
        else
        {
            xml.append("<").append(tagName).append(">");
            xml.append(value.toString());
            xml.append("</").append(tagName).append(">");
        }
        return xml.toString();
    }

    @Override
    public Resource unmarshal(InputStream inputStream, Class<? extends Resource> asResource) throws MalformedContentException
    {
        try
        {
            XMLReader reader = XMLReaderFactory.createXMLReader();
            SaxContentHandler contentHandler = new SaxContentHandler(this, asResource);
            SaxErrorHandler errorHandler = new SaxErrorHandler();
            reader.setContentHandler(contentHandler);
            reader.setErrorHandler(errorHandler);

            reader.parse(new InputSource(inputStream));

            return contentHandler.getResource();
        }
        catch(SAXException e)
        {
            if(e.getCause() instanceof ConversionException)
            {
                throw new MalformedContentException(e.getCause().getMessage());
            }
            throw new MalformedContentException(e);
        }
        catch(IOException e)
        {
            throw new ListenRuntimeException(e);
        }
    }
}
