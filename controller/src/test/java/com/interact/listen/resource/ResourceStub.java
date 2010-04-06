package com.interact.listen.resource;

public class ResourceStub extends Resource
{
    private Long id;

    @Override
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }
    
    public void validate()
    {
        // oookkkkkk......why am I here?
    }
}
