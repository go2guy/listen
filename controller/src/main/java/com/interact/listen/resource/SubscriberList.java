package com.interact.listen.resource;

import com.thoughtworks.xstream.XStream;

import java.util.ArrayList;
import java.util.List;

public class SubscriberList implements Resource
{
    private String href = "/subscribers";
    private List<Subscriber> list = new ArrayList<Subscriber>();

    public SubscriberList(List<Subscriber> list)
    {
        this.list = list;
    }

    public String getHref()
    {
        return href;
    }

    public List<Subscriber> getList()
    {
        return list;
    }

    public void setHref(String href)
    {
        this.href = href;
    }

    public String toXml()
    {
        XStream xstream = new XStream();

        xstream.alias("subscribers", SubscriberList.class);
        xstream.useAttributeFor(SubscriberList.class, "href");
        xstream.addImplicitCollection(SubscriberList.class, "list");

        xstream.alias("subscriber", Subscriber.class);
        xstream.useAttributeFor(Subscriber.class, "href");
        xstream.omitField(Subscriber.class, "id");
        xstream.omitField(Subscriber.class, "number");

        return xstream.toXML(this);
    }
}
