package com.interact.listen.xml;

import com.interact.listen.resource.Resource;
import com.interact.listen.xml.converter.Converter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class SaxContentHandler extends DefaultHandler
{
    private Marshaller marshaller;

    private Resource resource;
    private String resourceElement;

    private String value;
    private Attributes attributes;

    public SaxContentHandler(Marshaller marshaller)
    {
        super();
        this.marshaller = marshaller;
    }

    public Resource getResource()
    {
        return resource;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
    {
        this.attributes = attributes;

        // resource not loaded yet, assume this is the first element
        if(resource == null)
        {
            resourceElement = qName;
            try
            {
                String className = getClassName(qName);
                Class resourceClass = Class.forName(className);

                resource = (Resource)resourceClass.newInstance();
                
                String href = attributes.getValue("href");
                resource.setId(getIdFromHref(href));
            }
            catch(ClassNotFoundException e)
            {
                // FIXME
                throw new RuntimeException(e);
            }
            catch(IllegalAccessException e)
            {
                // FIXME
                throw new RuntimeException(e);
            }
            catch(InstantiationException e)
            {
                // FIXME
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length)
    {
        value = new String(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName)
    {
        if(!qName.equals(resourceElement))
        {
            // all of the regular elements
            try
            {
                String methodName = "set";
                methodName += qName.substring(0, 1).toUpperCase();
                methodName += qName.substring(1);

                Method method = findMethod(methodName, resource.getClass());
                Class<?> parameterType = method.getParameterTypes()[0]; // assume one parameter

                if(Resource.class.isAssignableFrom(parameterType))
                {
                    Resource associatedResource = (Resource)parameterType.newInstance();
                    associatedResource.setId(getIdFromHref(attributes.getValue("href")));

                    method.invoke(resource, associatedResource);
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
            // catch(NoSuchMethodException e)
            // {
            // // FIXME
            // throw new RuntimeException(e);
            // }
            catch(InvocationTargetException e)
            {
                // FIXME
                throw new RuntimeException(e);
            }
            catch(IllegalAccessException e)
            {
                // thrown by converterClass.newInstance() or method.invoke();
                // FIXME
                throw new RuntimeException(e);
            }
            catch(InstantiationException e)
            {
                // thrown by converterClass.newInstance()
                // FIXME
                throw new RuntimeException(e);
            }
        }
    }

    private String getClassName(String elementName)
    {
        String capitalized = elementName.substring(0, 1).toUpperCase();
        if(elementName.length() > 1)
        {
            capitalized += elementName.substring(1);
        }

        return "com.interact.listen.resource." + capitalized;
    }

    private Method findMethod(String name, Class clazz)
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

    private Long getIdFromHref(String href)
    {
        if(href != null)
        {
            Long id = Long.parseLong(href.substring(href.lastIndexOf("/") + 1));
            return id;
        }
        return null;
    }
}
