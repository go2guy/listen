package com.interact.listen.resource;

import javax.persistence.*;

@Entity
public class Subscriber implements Resource
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Version
    private Integer version = Integer.valueOf(0);

    private String number;

    public String getNumber()
    {
        return number;
    }

    public void setNumber(String number)
    {
        this.number = number;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Integer getVersion()
    {
        return version;
    }

    public void setVersion(Integer version)
    {
        this.version = version;
    }

    @Override
    public String toXml(boolean deep)
    {
        StringBuilder xml = new StringBuilder();

        if(deep)
        {
            xml.append("<subscriber href=\"/subscribers/").append(id).append("\">");
            xml.append("<id>").append(id).append("</id>");
            xml.append("<number>").append(number).append("</number>");
            xml.append("</subscriber>");
        }
        else
        {
            xml.append("<subscriber href=\"/subscribers/").append(id).append("\"/>");
        }
        return xml.toString();
    }

    @Override
    public void loadFromXml(String xml, boolean loadId)
    {
        // FIXME super-gross xml parsing until I get a decent xml binding framework in place
        int start, end;

        if(loadId)
        {
            start = xml.indexOf("<id>");
            end = xml.indexOf("</id>");
            if(start > 0 && end > 0)
            {
                String idString = xml.substring(start + "<id>".length(), end);
                this.id = Long.parseLong(idString);
            }
        }

        start = xml.indexOf("<number>");
        end = xml.indexOf("</number>");
        if(start > 0 && end > 0)
        {
            this.number = xml.substring(start + "<number>".length(), end);
        }
    }
}
