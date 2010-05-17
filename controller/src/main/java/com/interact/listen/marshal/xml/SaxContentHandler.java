package com.interact.listen.marshal.xml;

import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.converter.ConversionException;
import com.interact.listen.marshal.converter.Converter;
import com.interact.listen.resource.Resource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Handler implementation for unmarshalling XML content.
 */
public class SaxContentHandler extends DefaultHandler
{
    private static final Logger LOG = Logger.getLogger(SaxContentHandler.class);

    private XmlMarshaller marshaller;

    private Resource resource;
    private String resourceElement;
    private boolean unmarshalId = false;

    private String value;
    private Attributes attributes;

    public SaxContentHandler(XmlMarshaller marshaller, Resource resource, boolean unmarshalId)
    {
        super();
        this.marshaller = marshaller;
        this.resource = resource;
        this.unmarshalId = unmarshalId;
    }

    public Resource getResource()
    {
        return resource;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes theAttributes)
    {
        this.value = null;
        this.attributes = theAttributes;

        // resourceElement not loaded yet, assume this is the first element
        if(resourceElement == null)
        {
            resourceElement = qName;

            if(unmarshalId)
            {
                String href = theAttributes.getValue("href");
                // no href on POST
                if(href != null)
                {
                    resource.setId(Marshaller.getIdFromHref(href));
                }
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length)
    {
        value = new String(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if(!qName.equals(resourceElement) && (value != null || attributes.getValue("href") != null))
        {
            if(!unmarshalId && qName.equals("id"))
            {
                return;
            }

            try
            {
                // all of the regular elements
                String methodName = "set";
                methodName += qName.substring(0, 1).toUpperCase();
                methodName += qName.substring(1);

                Method method = Marshaller.findMethod(methodName, resource.getClass());

                if(method == null)
                {
                    LOG.warn("method " + methodName + " does not exist for resource " + resource);
                    throw new SAXException("Cannot set property " + qName);
                }

                Class<?> parameterType = method.getParameterTypes()[0]; // assume one parameter

                if(Resource.class.isAssignableFrom(parameterType))
                {
                    Resource associatedResource = (Resource)parameterType.newInstance();
                    associatedResource.setId(Marshaller.getIdFromHref(attributes.getValue("href")));
                    method.invoke(resource, associatedResource);
                }
                else if(java.util.Collection.class.isAssignableFrom(parameterType))
                {
                    LOG.debug("Skipping [" + qName + "] when unmarshalling, it is a Collection");
                }
                else
                {
                    Class<? extends Converter> converterClass = marshaller.getConverterClass(parameterType);
                    if(converterClass == null)
                    {
                        throw new AssertionError("No Converter configured for [" + parameterType +
                                                 "], you should probably write one");
                    }

                    Converter converter = converterClass.newInstance();
                    Object convertedValue = converter.unmarshal(value);
                    method.invoke(resource, convertedValue);
                }
            }
            catch(IllegalAccessException e)
            {
                throw new AssertionError(e);
            }
            catch(InstantiationException e)
            {
                throw new AssertionError(e);
            }
            catch(InvocationTargetException e)
            {
                throw new AssertionError(e);
            }
            catch(ConversionException e)
            {
                throw new SAXException(e);
            }
        }
    }
}
