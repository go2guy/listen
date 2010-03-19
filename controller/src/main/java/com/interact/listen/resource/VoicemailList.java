package com.interact.listen.resource;

import com.thoughtworks.xstream.XStream;

import java.util.ArrayList;
import java.util.List;

public class VoicemailList implements Resource
{
    private String href = "/voicemails";
    private List<Voicemail> list = new ArrayList<Voicemail>();

    public VoicemailList(List<Voicemail> list)
    {
        this.list = list;
    }

    public List<Voicemail> getList()
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
        xstream.alias("voicemails", VoicemailList.class);
        xstream.useAttributeFor(VoicemailList.class, "href");
        xstream.addImplicitCollection(VoicemailList.class, "list");

        xstream.alias("voicemail", Voicemail.class);
        xstream.useAttributeFor(Voicemail.class, "href");
        xstream.omitField(Voicemail.class, "id");
        xstream.omitField(Voicemail.class, "subscriber");
        xstream.omitField(Voicemail.class, "fileLocation");

        return xstream.toXML(this);
    }
}
