package com.interact.listen.resource;

public class ListenSpotSubscriber extends Resource
{
    private Long id;
    private String httpApi;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getHttpApi()
    {
        return httpApi;
    }

    public void setHttpApi(String httpApi)
    {
        this.httpApi = httpApi;
    }

    public boolean validate()
    {
        boolean isValid = true;
        if(httpApi == null || httpApi.trim().equals(""))
        {
            addToErrors("'httpApi' cannot be null or blank");
            isValid = false;
        }
        return isValid;
    }
}
