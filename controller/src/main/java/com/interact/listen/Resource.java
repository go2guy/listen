package com.interact.listen;

public interface Resource
{
    public String toXml(boolean deep);
    public void loadFromXml(String xml, boolean loadId);
}
