package com.interact.listen.marshal.xml;

import com.interact.listen.ListenRuntimeException;
import com.interact.listen.marshal.MalformedContentException;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.converter.ConversionException;
import com.interact.listen.resource.Resource;
import com.interact.listen.resource.ResourceList;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class XmlMarshaller extends Marshaller
{
    private static final Logger LOG = Logger.getLogger(XmlMarshaller.class);

    @Override
    public String marshal(Resource resource)
    {
        StringBuilder xml = new StringBuilder();

        // iterate over properties
        Class<? extends Resource> clazz = resource.getClass();
        Method[] methods = clazz.getMethods();
        sortMethods(methods);

        String resourceTag = getTagForClass(resource.getClass().getSimpleName());

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
            else if(java.util.Collection.class.isAssignableFrom(returnType))
            {
                String s = ((ParameterizedType)method.getGenericReturnType()).getActualTypeArguments()[0].toString();
                String name = s.substring(s.lastIndexOf(".") + 1);
                String associatedTag = getTagForClass(name);

                String collectionHref = buildSpecificHref(resourceTag + "s", resource.getId());
                String href = buildListHref(associatedTag + "s", 0, 100, null, resourceTag + "=" + encodeUrl(collectionHref));
                href = escapeXml(href);
                xml.append("<").append(propertyTag).append(" href=\"").append(href).append("\"/>");
            }
            else
            {
                String resultString = convert(returnType, result);
                xml.append(marshalTag(propertyTag, resultString));
            }
        }

        String classTag = getTagForClass(clazz.getSimpleName());
        xml.append("</").append(classTag).append(">");

        return xml.toString();
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

        String href = buildListHref(tag, list.getFirst(), list.getMax(), list.getFieldsForQuery(),
                                    list.getSearchPropertiesForQuery());
        href = escapeXml(href);
        xml.append("<").append(tag).append(" href=\"").append(href).append("\"");

        int count = list.getList().size();
        xml.append(" count=\"").append(count).append("\"");
        xml.append(" total=\"").append(list.getTotal()).append("\"");

        if(count < list.getTotal())
        {
            if(list.getFirst() + list.getMax() < list.getTotal())
            {
                String next = buildListHref(tag, list.getMax() + list.getFirst(), list.getMax(),
                                            list.getFieldsForQuery(), list.getSearchPropertiesForQuery());
                next = escapeXml(next);
                xml.append(" next=\"").append(next).append("\"");
            }
        }

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
            String classTag = getTagForClass(resource.getClass().getSimpleName());

            String itemHref = buildSpecificHref(classTag + "s", resource.getId());
            itemHref = escapeXml(itemHref);
            xml.append("<").append(classTag).append(" href=\"").append(itemHref).append("\"");

            for(String field : list.getFields())
            {
                String methodName = "get" + field.substring(0, 1).toUpperCase() + field.substring(1);
                Method method = Marshaller.findMethod(methodName, resource.getClass());

                if(method == null || OMIT_METHODS.contains(method.getName()))
                {
                    LOG.debug("Method [" + methodName + "] not found, or method is explicitly omitted, for [" +
                              resource.getClass() + "] when marshalling list");
                    continue;
                }

                Object result = invokeMethod(method, resource);
                String propertyTag = getTagForMethod(method.getName());

                xml.append(" ").append(propertyTag).append("=\"");

                Class<?> returnType = method.getReturnType();
                if(Resource.class.isAssignableFrom(returnType))
                {
                    if(result == null)
                    {
                        xml.append("nil");
                    }
                    else
                    {
                        String associatedTag = getTagForClass(returnType.getSimpleName());
                        String resourceHref = buildSpecificHref(associatedTag + "s", ((Resource)result).getId());
                        resourceHref = escapeXml(resourceHref);
                        xml.append(resourceHref);
                    }
                }
                else if(java.util.Collection.class.isAssignableFrom(returnType))
                {
                    String s = ((ParameterizedType)method.getGenericReturnType()).getActualTypeArguments()[0] .toString();
                    String name = s.substring(s.lastIndexOf(".") + 1);
                    String associatedTag = getTagForClass(name);

                    xml.append("/").append(associatedTag).append("s?");
                    xml.append(classTag).append("=");
                    xml.append("/").append(classTag).append("/").append(resource.getId());
                }
                else
                {
                    String resultString = convert(returnType, result);
                    xml.append(resultString);
                }

                xml.append("\"");
            }

            xml.append("/>");
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

        String href = buildSpecificHref(classTag + "s", resource.getId());
        href = escapeXml(href);

        StringBuilder xml = new StringBuilder();
        xml.append("<").append(classTag).append(" ");
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
     * Marshals a single XML tag. If {@code value} is {@code null} a null element (e.g. {@code <foo nil="true"/>}) is
     * returned. Otherwise the value is returned between XML tags.
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
            xml.append(escapeXml(value.toString()));
            xml.append("</").append(tagName).append(">");
        }
        return xml.toString();
    }

    @Override
    public Resource unmarshal(InputStream inputStream, Resource resource, boolean unmarshalId)
        throws MalformedContentException
    {
        try
        {
            XMLReader reader = XMLReaderFactory.createXMLReader();
            SaxContentHandler contentHandler = new SaxContentHandler(this, resource, unmarshalId);
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

    @Override
    public String getContentType()
    {
        return "application/xml";
    }
}
