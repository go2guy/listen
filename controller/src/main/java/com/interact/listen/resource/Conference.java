package com.interact.listen.resource;

import javax.persistence.*;

@Entity
public class Conference implements Resource
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Version
    private Integer version = Integer.valueOf(0);

    public Integer getVersion()
    {
        return version;
    }

    public void setVersion(Integer version)
    {
        this.version = version;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    @Override
    public String toXml(boolean deep)
    {
        StringBuilder xml = new StringBuilder();

        if(deep)
        {
            xml.append("<conference href=\"/conferences/").append(id).append("\">");
            xml.append("<id>").append(id).append("</id>");
            xml.append("</conference>");
        }
        else
        {
            xml.append("<conference href=\"/conferences/").append(id).append("\"/>");
        }
        return xml.toString();
    }

    @Override
    public void loadFromXml(String xml, boolean loadId)
    {
        // FIXME super-gross xml parsing until I get a decent xml binding framework in place
        if(loadId)
        {
            String id = XmlUtil.getTagContents("id", xml);
            if(id == null)
            {
                this.id = null;
            }
            else
            {
                this.id = Long.parseLong(XmlUtil.getTagContents("id", xml));
            }
        }
    }


}
