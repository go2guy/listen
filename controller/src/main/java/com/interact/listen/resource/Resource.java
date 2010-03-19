package com.interact.listen.resource;

public interface Resource
{
    public String toXml(boolean deep);
    public void loadFromXml(String xml, boolean loadId);
}
