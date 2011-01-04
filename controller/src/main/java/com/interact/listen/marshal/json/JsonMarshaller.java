package com.interact.listen.marshal.json;

import com.interact.listen.exception.ListenRuntimeException;
import com.interact.listen.marshal.MalformedContentException;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.converter.ConversionException;
import com.interact.listen.marshal.converter.Converter;
import com.interact.listen.resource.Resource;
import com.interact.listen.resource.ResourceList;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Implementation of {@link Marshaller} for "application/json" content.
 */
public class JsonMarshaller extends Marshaller
{
    private static final Logger LOG = Logger.getLogger(JsonMarshaller.class);

    @Override
    public String marshal(Resource resource)
    {
        Class<? extends Resource> clazz = resource.getClass();
        Method[] methods = clazz.getMethods();
        sortMethods(methods);

        String resourceTag = getTagForClass(clazz.getSimpleName());
        String href = buildHref(resource);

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"href\":\"").append(href).append("\",");

        for(Method method : methods)
        {
            if(!method.getName().startsWith("get") || OMIT_METHODS.contains(method.getName()))
            {
                continue;
            }

            String propertyTag = getTagForMethod(method.getName());
            json.append("\"").append(propertyTag).append("\":");

            Object result = invokeMethod(method, resource);
            Class<?> returnType = method.getReturnType();
            if(Resource.class.isAssignableFrom(returnType))
            {
                if(result == null)
                {
                    json.append("null");
                }
                else
                {
                    String itemHref = buildHref((Resource)result);
                    json.append("{\"href\":\"").append(itemHref).append("\"").append("}");
                }
            }
            else if(java.util.Collection.class.isAssignableFrom(returnType))
            {
                String s = ((ParameterizedType)method.getGenericReturnType()).getActualTypeArguments()[0].toString();
                String name = s.substring(s.lastIndexOf(".") + 1);
                String associatedTag = getTagForClass(name);

                String listHref = buildListHref(associatedTag + "s", 0, 100, null, resourceTag + "=" + encodeUrl(href));
                json.append("{\"href\":\"").append(listHref).append("\"}");
            }
            else
            {
                String resultString = convertAndEscape(returnType, result);
                json.append(conditionallyQuote(resultString, returnType));
            }

            json.append(",");
        }

        // remove the last comma
        json.deleteCharAt(json.length() - 1);
        json.append("}");

        return json.toString();
    }

    @Override
    public String marshal(ResourceList list, Class<? extends Resource> resourceClass)
    {
        if(list == null)
        {
            throw new IllegalArgumentException("List cannot be null");
        }

        String tag = getTagForClass(resourceClass.getSimpleName());

        String href = buildListHref(tag + "s", list.getFirst(), list.getMax(), list.getFieldsForQuery(), list.getSearchPropertiesForQuery());

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"href\":\"").append(href).append("\",");

        int count = list.getList().size();
        json.append("\"count\":").append(count).append(",");
        json.append("\"total\":").append(list.getTotal()).append(",");
        
        if(count < list.getTotal())
        {
             if(list.getFirst() + count < list.getTotal())
             {
                 String next = buildListHref(tag + "s", count + list.getFirst(), list.getMax(),
                                             list.getFieldsForQuery(), list.getSearchPropertiesForQuery());
                 json.append("\"next\":\"").append(next).append("\",");
             }
        }

        json.append("\"results\":[");
        for(Resource resource : list.getList())
        {
            String itemHref = "/" + tag + "s/" + resource.getId();

            json.append("{");
            json.append("\"href\":\"").append(itemHref).append("\"");

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

                json.append(",\"").append(propertyTag).append("\":");

                Class<?> returnType = method.getReturnType();
                if(Resource.class.isAssignableFrom(returnType))
                {
                    if(result == null)
                    {
                        json.append("null");
                    }
                    else
                    {
                        String resourceHref = buildHref((Resource)result);
                        json.append("{\"href\":\"").append(resourceHref).append("\"}");
                    }
                }
                else if(java.util.Collection.class.isAssignableFrom(returnType))
                {
                    String s = ((ParameterizedType)method.getGenericReturnType()).getActualTypeArguments()[0].toString();
                    String name = s.substring(s.lastIndexOf(".") + 1);
                    String associatedTag = getTagForClass(name);

                    String collectionHref = buildListHref(associatedTag + "s", 0, 100, null, tag + "=" + itemHref);
                    json.append("{\"href\":\"").append(collectionHref).append("\"}");
                }
                else
                {
                    String resultString = convertAndEscape(returnType, result);
                    json.append(conditionallyQuote(resultString, returnType));
                }
            }

            json.append("},");
        }

        if(list.getList().size() > 0)
        {
            json.deleteCharAt(json.length() - 1);
        }

        json.append("]");
        json.append("}");

        return json.toString();
    }

    @Override
    public Resource unmarshal(InputStream inputStream, Resource resource, boolean unmarshalId)
        throws MalformedContentException
    {
        try
        {
            String input = IOUtils.toString(inputStream);
            LOG.debug("Received JSON for unmarshalling: <<" + input + ">>");

            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject)parser.parse(input);

            if(unmarshalId)
            {
                String href = (String)json.get("href");
                if(href != null)
                {
                    resource.setId(getIdFromHref(href));
                }
            }

            for(Object k : json.keySet())
            {
                String key = (String)k;

                if(key.equals("href") || key.equals("id")) // already retrieved the id above
                {
                    continue;
                }

                String methodName = "set";
                methodName += key.substring(0, 1).toUpperCase();
                methodName += key.substring(1);

                Method method = findMethod(methodName, resource.getClass());                
                Class<?> parameterType = method.getParameterTypes()[0];

                if(Resource.class.isAssignableFrom(parameterType))
                {
                    JSONObject value = (JSONObject)json.get(key);

                    Resource associatedResource = (Resource)parameterType.newInstance();
                    if(value == null || value.get("href") == null)
                    {
                        associatedResource = null;
                    }
                    else
                    {
                        associatedResource.setId(getIdFromHref((String)value.get("href")));
                    }
                    method.invoke(resource, associatedResource);
                }
                else if(java.util.Collection.class.isAssignableFrom(parameterType))
                {
                    LOG.debug("Skipping [" + key + "] when unmarshalling, it is a Collection");
                }
                else
                {
                    Class<? extends Converter> converterClass = getConverterClass(parameterType);
                    if(converterClass == null)
                    {
                        throw new AssertionError("No Converter configured for [" + parameterType +
                                                 "], you should probably write one");
                    }
                    Converter converter = converterClass.newInstance();
                    Object value = json.get(key);
                    Object convertedValue = converter.unmarshal(value == null ? null : String.valueOf(value));
                    method.invoke(resource, convertedValue);
                }
            }

            return resource;
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
        catch(IOException e)
        {
            throw new ListenRuntimeException(e);
        }
        catch(ParseException e)
        {
            throw new MalformedContentException(e);
        }
        catch(ConversionException e)
        {
            throw new MalformedContentException(e.getMessage());
        }
    }

    @Override
    public String getContentType()
    {
        return "application/json";
    }

    @Override
    public String escape(String value)
    {
        if(value == null)
        {
            return null;
        }
        return value.replaceAll("\"", "\\\\\"");
    }

    private String conditionallyQuote(String value, Class<?> clazz)
    {
        boolean useQuotes = value != null && isStringType(clazz);
        if(useQuotes)
        {
            return "\"" + value + "\"";
        }
        return value;
    }
    
    private boolean isStringType(Class<?> clazz)
    {
        if(Boolean.class.isAssignableFrom(clazz))
        {
            return false;
        }

        if(Number.class.isAssignableFrom(clazz))
        {
            return false;
        }

        return true;
    }
}
