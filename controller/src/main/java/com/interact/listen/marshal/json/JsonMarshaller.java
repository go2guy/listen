package com.interact.listen.marshal.json;

import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.converter.Converter;
import com.interact.listen.resource.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonMarshaller extends Marshaller
{
    @Override
    public String marshal(Resource resource)
    {
        try
        {
            Class<? extends Resource> clazz = resource.getClass();
            Method[] methods = clazz.getMethods();
            sortMethods(methods);

            String resourceTag = getTagForClass(clazz.getSimpleName());

            StringBuilder json = new StringBuilder();
            json.append("{");
            // json.append(resourceTag);
            // json.append(":{");
            json.append("\"href\":").append("\"/").append(resourceTag).append("s/").append(resource.getId()).append("\",");

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
                        String associatedTag = getTagForClass(returnType.getSimpleName());
                        json.append("{\"href\":");
                        json.append("\"/").append(associatedTag).append("s/").append(((Resource)result).getId()).append("\"");
                        json.append("}");
                    }
                }
                else
                {
                    Class<? extends Converter> converterClass = getConverterClass(returnType);
                    Converter converter = converterClass.newInstance();
                    String resultString = converter.marshal(result);

                    json.append("\"").append(resultString).append("\"");
                }

                json.append(",");
            }

            // remove the last comma
            json.deleteCharAt(json.length() - 1);
            // json.append("}");
            json.append("}");

            return json.toString();
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
    public String marshal(List<Resource> list, Class<? extends Resource> resourceClass)
    {
        if(list == null)
        {
            throw new IllegalArgumentException("List cannot be null");
        }

        String tag = getTagForClass(resourceClass.getSimpleName()) + "s";

        StringBuilder json = new StringBuilder();
        json.append("[");
        for(Resource resource : list)
        {
            json.append("{");
            json.append("\"href\":\"/").append(tag).append("/").append(resource.getId()).append("\"");
            json.append("}");
        }
        json.append("]");

        return json.toString();
    }

    @Override
    public Resource unmarshal(InputStream inputStream, Class<? extends Resource> asResource)
    {
        try
        {
            Resource resource = (Resource)asResource.newInstance();
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject)parser.parse(new InputStreamReader(inputStream));

            String href = (String)json.get("href");
            if(href != null)
            {
                resource.setId(getIdFromHref(href));
            }

            for(Object k : json.keySet())
            {
                String key = (String)k;

                if(key.equals("href"))
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
                    if(value.get("href") == null)
                    {
                        associatedResource = null;
                    }
                    else
                    {
                        associatedResource.setId(getIdFromHref((String)value.get("href")));
                    }
                    method.invoke(resource, associatedResource);
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
                    Object convertedValue = converter.unmarshal((String)json.get(key));
                    method.invoke(resource, convertedValue);
                }
            }

            return resource;
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
        catch(InvocationTargetException e)
        {
            // FIXME
            throw new RuntimeException(e);
        }
        catch(IOException e)
        {
            // FIXME
            throw new RuntimeException(e);
        }
        catch(ParseException e)
        {
            // FIXME
            throw new RuntimeException(e);
        }
    }
}
