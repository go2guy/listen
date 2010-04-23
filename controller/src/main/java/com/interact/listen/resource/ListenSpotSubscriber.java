package com.interact.listen.resource;

import javax.persistence.*;

@Entity
public class ListenSpotSubscriber extends Resource
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Version
    private Integer version;

    private String httpApi;

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

    public Integer getVersion()
    {
        return version;
    }

    public void setVersion(Integer version)
    {
        this.version = version;
    }

    public String getHttpApi()
    {
        return httpApi;
    }

    public void setHttpApi(String httpApi)
    {
        this.httpApi = httpApi;
    }

    @Override
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

    @Override
    public ListenSpotSubscriber copy(boolean withIdAndVersion)
    {
        ListenSpotSubscriber copy = new ListenSpotSubscriber();
        if(withIdAndVersion)
        {
            copy.setId(id);
            copy.setVersion(version);
        }

        copy.setHttpApi(httpApi);
        return copy;
    }
}
