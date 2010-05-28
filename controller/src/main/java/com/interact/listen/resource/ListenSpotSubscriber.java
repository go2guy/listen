package com.interact.listen.resource;

import com.interact.listen.util.ComparisonUtil;

import java.util.List;

import javax.persistence.*;

import org.hibernate.Criteria;
import org.hibernate.Session;

@Entity
@Table(name = "LISTEN_SPOT_SUBSCRIBER")
public class ListenSpotSubscriber extends Resource
{
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    @Column(name = "VERSION")
    @Version
    private Integer version = Integer.valueOf(0);

    @Column(name = "HTTP_API", nullable = false, unique = true)
    private String httpApi;

    @Column(name = "PHONE_NUMBER")
    private String phoneNumber;

    @Column(name = "PHONE_NUMBER_PROTOCOL")
    @Enumerated(EnumType.STRING)
    private PhoneNumberProtocolType phoneNumberProtocol;

    public static enum PhoneNumberProtocolType
    {
        VOIP, PSTN;
    }

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

    public String getPhoneNumber()
    {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }

    public PhoneNumberProtocolType getPhoneNumberProtocol()
    {
        return phoneNumberProtocol;
    }

    public void setPhoneNumberProtocol(PhoneNumberProtocolType type)
    {
        this.phoneNumberProtocol = type;
    }

    @Override
    public boolean validate()
    {
        if(httpApi == null || httpApi.trim().equals(""))
        {
            addToErrors("'httpApi' cannot be null or blank");
        }

        if(phoneNumber == null || phoneNumber.trim().equals(""))
        {
            addToErrors("'phoneNumber' cannot be null or blank");
        }

        if(phoneNumberProtocol == null)
        {
            addToErrors("'phoneNumberProtocol' cannot be null");
        }

        return !hasErrors();
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
