package com.interact.listen.resource;

import com.interact.listen.xml.XmlUtil;

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

    @Override
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
        this.number = XmlUtil.getTagContents("number", xml);
    }
}
