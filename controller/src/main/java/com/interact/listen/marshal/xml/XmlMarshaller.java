package com.interact.listen.marshal.xml;

import com.interact.listen.exception.ListenRuntimeException;
import com.interact.listen.marshal.MalformedContentException;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.converter.ConversionException;
import com.interact.listen.resource.Resource;
import com.interact.listen.resource.ResourceList;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Implementation of {@link Marshaller} for "application/xml" content.
 */
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

        String resourceName = getTagForClass(resource.getClass().getSimpleName());
        xml.append(marshalReferenceTag(resourceName, buildSpecificHref(resourceName + "s", resource.getId()), false));

        for(Method method : methods)
        {
            if(!method.getName().startsWith("get") || OMIT_METHODS.contains(method.getName()))
            {
                continue;
            }

            Object result = invokeMethod(method, resource);
            String propertyName = getTagForMethod(method.getName());

            Class<?> returnType = method.getReturnType();
            if(Resource.class.isAssignableFrom(returnType))
            {
                if(result == null)
                {
                    xml.append(marshalNullValueTag(propertyName));
                }
                else
                {
                    String href = buildSpecificHref(getTagForClass(returnType.getSimpleName()) + "s",
                                                    ((Resource)result).getId());
                    xml.append(marshalReferenceTag(propertyName, href, true));
                }
            }
            else if(java.util.Collection.class.isAssignableFrom(returnType))
            {
                String s = ((ParameterizedType)method.getGenericReturnType()).getActualTypeArguments()[0].toString();
                String name = s.substring(s.lastIndexOf(".") + 1);
                String referenceName = getTagForClass(name);

                String collectionHref = buildSpecificHref(resourceName + "s", resource.getId());
                String href = buildListHref(referenceName + "s", 0, 100, null, resourceName + "=" +
                                                                               encodeUrl(collectionHref));
                href = escape(href);
                xml.append(marshalReferenceTag(propertyName, href, true));
            }
            else if(result == null)
            {
                xml.append(marshalNullValueTag(propertyName));
            }
            else
            {
                String resultString = convertAndEscape(returnType, result);
                xml.append(marshalNonNullValueTag(propertyName, resultString));
            }
        }

        xml.append(marshalClosingTag(resourceName));
        return xml.toString();
    }

    @Override
    public String marshal(ResourceList list, Class<? extends Resource> resourceClass)
    {
        if(list == null)
        {
            throw new IllegalArgumentException("List cannot be null");
        }

        String listName = getTagForClass(resourceClass.getSimpleName()) + "s"; // pluralize it for the list
        StringBuilder xml = new StringBuilder();

        String href = escape(buildListHref(listName, list.getFirst(), list.getMax(), list.getFieldsForQuery(),
                                           list.getSearchPropertiesForQuery()));
        int count = list.getList().size();
        String next = null;
        if(count < list.getTotal())
        {
            if(list.getFirst() + list.getMax() < list.getTotal())
            {
                next = escape(buildListHref(listName, list.getMax() + list.getFirst(), list.getMax(),
                                            list.getFieldsForQuery(), list.getSearchPropertiesForQuery()));
            }
        }
        boolean close = list.getList().size() == 0;
        xml.append(marshalListTag(listName, href, count, list.getTotal(), next, close));

        for(Resource resource : list.getList())
        {
            String className = getTagForClass(resource.getClass().getSimpleName());

            String itemHref = buildSpecificHref(className + "s", resource.getId());
            itemHref = escape(itemHref);
            xml.append(marshalReferenceTagWithoutEnd(className, itemHref));

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
                String propertyName = getTagForMethod(method.getName());

                xml.append(" ").append(propertyName).append("=\"");

                Class<?> returnType = method.getReturnType();
                if(Resource.class.isAssignableFrom(returnType))
                {
                    if(result == null)
                    {
                        xml.append("nil");
                    }
                    else
                    {
                        String referenceName = getTagForClass(returnType.getSimpleName());
                        String resourceHref = buildSpecificHref(referenceName + "s", ((Resource)result).getId());
                        resourceHref = escape(resourceHref);
                        xml.append(resourceHref);
                    }
                }
                else if(java.util.Collection.class.isAssignableFrom(returnType))
                {
                    String s = ((ParameterizedType)method.getGenericReturnType()).getActualTypeArguments()[0].toString();
                    String name = s.substring(s.lastIndexOf(".") + 1);
                    String referenceName = getTagForClass(name);

                    xml.append("/").append(referenceName).append("s?");
                    xml.append(className).append("=");
                    xml.append("/").append(className).append("/").append(resource.getId());
                }
                else
                {
                    String resultString = convertAndEscape(returnType, result);
                    xml.append(resultString);
                }

                xml.append("\"");
            }

            xml.append("/>");
        }

        if(list.getList().size() > 0)
        {
            xml.append(marshalClosingTag(listName));
        }

        return xml.toString();
    }

    private String marshalTag(String tag, Map<String, String> attributes, boolean close)
    {
        StringBuilder content = new StringBuilder();
        content.append("<").append(tag);
        for(Map.Entry<String, String> entry : attributes.entrySet())
        {
            String key = entry.getKey();
            String value = entry.getValue();
            content.append(" ").append(key).append("=\"").append(value == null ? "nil" : value).append("\"");
        }
        if(close)
        {
            content.append("/");
        }
        content.append(">");
        return content.toString();
    }

    private String marshalReferenceTag(String tag, String href, boolean close)
    {
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put("href", href);
        return marshalTag(tag, attrs, close);
    }

    private String marshalReferenceTagWithoutEnd(String tag, String href)
    {
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put("href", href);
        String content = marshalTag(tag, attrs, false);
        return content.substring(0, content.length() - 1);
    }

    private String marshalNullValueTag(String tag)
    {
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put("nil", "true");
        return marshalTag(tag, attrs, true);
    }

    private String marshalNonNullValueTag(String tag, Object value)
    {
        return marshalOpeningTag(tag) + value.toString() + marshalClosingTag(tag);
    }

    private String marshalOpeningTag(String tag)
    {
        return "<" + tag + ">";
    }

    private String marshalClosingTag(String tag)
    {
        return "</" + tag + ">";
    }

    private String marshalAttribute(String attribute, String value)
    {
        return attribute + "=\"" + (value == null ? "nil" : value) + "\"";
    }

    private String marshalListTag(String tag, String href, Integer count, Long total, String next, boolean close)
    {
        StringBuilder xml = new StringBuilder();
        xml.append("<").append(tag);
        xml.append(" ").append(marshalAttribute("href", href));
        xml.append(" ").append(marshalAttribute("count", count == null ? null : String.valueOf(count)));
        xml.append(" ").append(marshalAttribute("total", total == null ? null : String.valueOf(total)));
        if(next != null)
        {
            xml.append(" ").append(marshalAttribute("next", next));
        }
        if(close)
        {
            xml.append("/");
        }
        xml.append(">");
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

    @Override
    public String escape(String value)
    {
        if(value == null)
        {
            return null;
        }
        return StringEscapeUtils.escapeXml(value);
    }
}
