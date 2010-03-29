package com.interact.listen.marshal.json;

import com.interact.listen.ListenRuntimeException;
import com.interact.listen.marshal.MalformedContentException;
import com.interact.listen.marshal.Marshaller;
import com.interact.listen.marshal.converter.ConversionException;
import com.interact.listen.marshal.converter.Converter;
import com.interact.listen.resource.Resource;
import com.interact.listen.resource.ResourceList;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonMarshaller extends Marshaller
{
    @Override
    public String marshal(Resource resource)
    {
        Class<? extends Resource> clazz = resource.getClass();
        Method[] methods = clazz.getMethods();
        sortMethods(methods);

        String resourceTag = getTagForClass(clazz.getSimpleName());
        String href = "/" + resourceTag + "s/" + resource.getId();

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"href\":").append("\"").append(href).append("\",");

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
                    json.append("\"/").append(associatedTag).append("s/").append(((Resource)result).getId())
                        .append("\"");
                    json.append("}");
                }
            }
            else if(java.util.Collection.class.isAssignableFrom(returnType))
            {
                String s = ((ParameterizedType)method.getGenericReturnType()).getActualTypeArguments()[0].toString();
                String name = s.substring(s.lastIndexOf(".") + 1);
                String associatedTag = getTagForClass(name);

                json.append("{\"href\":");
                json.append("\"/").append(associatedTag).append("s?");
                json.append(resourceTag).append("=").append(href);
                json.append("\"}");
            }
            else
            {
                String resultString = Marshaller.convert(returnType, result);
                boolean useQuotes = resultString != null && isStringType(returnType);

                if(useQuotes)
                {
                    json.append("\"");
                }
                json.append(resultString);
                if(useQuotes)
                {
                    json.append("\"");
                }
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

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"href\":\"").append("/").append(tag).append("s?");
        json.append("_first=").append(list.getFirst());
        json.append("&_max=").append(list.getMax());
        String fields = list.getFieldsForQuery();
        if(fields.length() > 0)
        {
            json.append("&").append("_fields=").append(list.getFieldsForQuery());
        }
        String properties = list.getSearchPropertiesForQuery();
        if(properties.length() > 0)
        {
            json.append("&").append(list.getSearchPropertiesForQuery());
        }
        json.append("\",");

        int count = list.getList().size();

        json.append("\"count\":").append(count).append(",");
        json.append("\"total\":").append(list.getTotal()).append(",");
        
        if(count < list.getTotal())
        {
             if(list.getFirst() + list.getMax() < list.getTotal())
             {
                 
                 json.append("\"next\":\"/").append(tag).append("s?");
                 json.append("_first=").append(list.getMax() + list.getFirst());
                 json.append("&_max=").append(list.getMax());
                 fields = list.getFieldsForQuery();
                 if(fields.length() > 0)
                 {
                     json.append("&").append("_fields=").append(list.getFieldsForQuery());
                 }
                 properties = list.getSearchPropertiesForQuery();
                 if(properties.length() > 0)
                 {
                     json.append("&").append(list.getSearchPropertiesForQuery());
                 }
                 json.append("\",");
             }
        }

        json.append("\"results\":[");
        for(Resource resource : list.getList())
        {
            String href = "/" + tag + "s/" + resource.getId();

            json.append("{");
            json.append("\"href\":\"").append(href).append("\"");

            for(String field : list.getFields())
            {
                String methodName = "get" + field.substring(0, 1).toUpperCase() + field.substring(1);
                Method method = Marshaller.findMethod(methodName, resource.getClass());

                if(method == null || OMIT_METHODS.contains(method.getName()))
                {
                    System.out.println("Method [" + methodName + "] not found, or method is explicitly omitted, for [" +
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
                        String associatedTag = getTagForClass(returnType.getSimpleName());
                        json.append("{\"href\":\"/").append(associatedTag).append("s/")
                            .append(((Resource)result).getId()).append("\"}");
                    }
                }
                else if(java.util.Collection.class.isAssignableFrom(returnType))
                {
                    String s = ((ParameterizedType)method.getGenericReturnType()).getActualTypeArguments()[0].toString();
                    String name = s.substring(s.lastIndexOf(".") + 1);
                    String associatedTag = getTagForClass(name);

                    json.append("{\"href\":");
                    json.append("\"/").append(associatedTag).append("s?");
                    json.append(tag).append("=").append(href);
                    json.append("\"}");
                }
                else
                {
                    String resultString = Marshaller.convert(returnType, result);
                    json.append("\"").append(resultString).append("\"");
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
    public Resource unmarshal(InputStream inputStream, Class<? extends Resource> asResource)
        throws MalformedContentException
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
                else if(java.util.Collection.class.isAssignableFrom(parameterType))
                {
                    System.out.println("Skipping [" + key + "] when unmarshalling, it is a Collection");
                }
                else
                {
                    Class<? extends Converter> converterClass = Marshaller.getConverterClass(parameterType);
                    if(converterClass == null)
                    {
                        throw new AssertionError("No Converter configured for [" + parameterType +
                                                 "], you should probably write one");
                    }
                    Converter converter = converterClass.newInstance();
                    Object convertedValue = converter.unmarshal(String.valueOf(json.get(key)));
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
