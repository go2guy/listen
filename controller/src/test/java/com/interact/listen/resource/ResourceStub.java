package com.interact.listen.resource;

public class ResourceStub implements Resource
{
    private Long id;

    @Override
    public void loadFromXml(String xml, boolean loadId)
    {
    // TODO
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
}
