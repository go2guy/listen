package com.interact.listen;

public interface Resource
{
    public String toXml();
    public void loadFromXml(String xml, boolean loadId);
}
