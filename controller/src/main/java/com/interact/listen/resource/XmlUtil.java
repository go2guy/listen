package com.interact.listen.resource;

public class XmlUtil
{
    // FIXME i feel like i need to take a shower for writing this. get rid of this and use some proper xml parsing!
    public static String getTagContents(String tag, String xml)
    {
        String startTag = "<" + tag + ">";
        int start = xml.indexOf(startTag);
        int end = xml.indexOf("</" + tag + ">");

        if(start < 0 || end < 0)
        {
            return null;
        }
        return xml.substring(start + startTag.length(), end);
    }

    public static String getAttributeValue(String tag, String attribute, String xml)
    {
        int tagPos = xml.indexOf("<" + tag);
        if(tagPos < 0)
        {
            return null;
        }

        int attributePos = xml.indexOf(attribute, tagPos);
        if(attributePos < 0)
        {
            return null;
        }

        int valueStart = attributePos + attribute.length() + 2; // two for ="
        return xml.substring(valueStart, xml.indexOf("\"", valueStart));
    }
}
