package com.interact.listen.resource;

public interface Resource
{
    public void loadFromXml(String xml, boolean loadId);
    public Long getId();
}
