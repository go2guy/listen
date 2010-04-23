package com.interact.listen.resource;

public class ResourceStub extends Resource
{
    private Long id;

    @Override
    public Long getId()
    {
        return id;
    }

    @Override
    public void setId(Long id)
    {
        this.id = id;
    }

    @Override
    public boolean validate()
    {
        return true;
    }

    @Override
    public ResourceStub copy(boolean withIdAndVersion)
    {
        ResourceStub copy = new ResourceStub();
        if(withIdAndVersion)
        {
            copy.setId(id);
        }
        return copy;
    }
}
