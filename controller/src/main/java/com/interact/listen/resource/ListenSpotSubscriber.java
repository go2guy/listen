package com.interact.listen.resource;

import com.interact.listen.util.ComparisonUtil;

import java.util.List;

import javax.persistence.*;

import org.hibernate.Criteria;
import org.hibernate.Session;

@Entity
public class ListenSpotSubscriber extends Resource
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Version
    private Integer version = Integer.valueOf(0);

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

    public static List<ListenSpotSubscriber> list(Session session)
    {
        Criteria criteria = session.createCriteria(ListenSpotSubscriber.class);
        return criteria.list();
    }

    @Override
    public boolean equals(Object that)
    {
        if(this == that)
        {
            return true;
        }

        if(that == null)
        {
            return false;
        }

        if(!(that instanceof ListenSpotSubscriber))
        {
            return false;
        }

        ListenSpotSubscriber listenSpotSubscriber = (ListenSpotSubscriber)that;

        if(!ComparisonUtil.isEqual(listenSpotSubscriber.getHttpApi(), getHttpApi()))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int hash = 1;
        hash *= prime + (getHttpApi() == null ? 0 : getHttpApi().hashCode());
        return hash;
    }
}
