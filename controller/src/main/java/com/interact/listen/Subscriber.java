package com.interact.listen;

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

    public Integer getVersion()
    {
        return version;
    }

    @Override
    public String toXml()
    {
        StringBuilder xml = new StringBuilder();
        xml.append("<subscriber href=\"/subscribers/").append(id).append("\">");
        xml.append("<id>").append(id).append("</id>");
        xml.append("<number>").append(number).append("</number>");
        xml.append("</subscriber>");
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
            if(start > 0 && end > 0 && end > start)
            {
                String idString = xml.substring(start + "<id>".length() + 1, end);
                this.id = Long.parseLong(idString);
            }
        }

        start = xml.indexOf("<number>");
        end = xml.indexOf("</number>");
        if(start > 0 && end > 0 && end > start)
        {
            this.number = xml.substring(start + "<number>".length() + 1, end);
        }
    }
}
