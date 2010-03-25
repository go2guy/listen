package com.interact.listen.resource;

import java.util.*;

public class ResourceList
{
    private List<Resource> list = new ArrayList<Resource>();
    private Set<String> fields = new HashSet<String>();
    private Map<String, String> searchProperties = new HashMap<String, String>();
    private int max;
    private int first;
    private Long total;

    public List<Resource> getList()
    {
        return list;
    }

    public void setList(List<Resource> list)
    {
        this.list = list;
    }

    public Set<String> getFields()
    {
        return fields;
    }

    public String getFieldsForQuery()
    {
        String f = fields.toString();
        f = f.substring(1, f.length() - 1);
        return f;
    }

    public void setFields(Set<String> fields)
    {
        this.fields = fields;
    }

    public int getMax()
    {
        return max;
    }

    public void setMax(int max)
    {
        this.max = max;
    }

    public int getFirst()
    {
        return first;
    }

    public void setFirst(int first)
    {
        this.first = first;
    }

    public Map<String, String> getSearchProperties()
    {
        return searchProperties;
    }

    public String getSearchPropertiesForQuery()
    {
        if(searchProperties.size() == 0)
        {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for(Map.Entry<String, String> property : searchProperties.entrySet())
        {
            builder.append(property.getKey()).append("=").append(property.getValue()).append("&");
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    public void setSearchProperties(Map<String, String> searchProperties)
    {
        this.searchProperties = searchProperties;
    }

    public void setTotal(Long total)
    {
        this.total = total;
    }

    public Long getTotal()
    {
        return total;
    }
}
