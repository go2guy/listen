package com.interact.listen.resource;

import com.interact.listen.PersistenceService;
import com.interact.listen.history.HistoryService;
import com.interact.listen.resource.Voicemail.MessageLightState;
import com.interact.listen.spot.MessageLightToggler;
import com.interact.listen.spot.SpotSystemMessageLightToggler;
import com.interact.listen.util.ComparisonUtil;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

@Entity
@Table(name = "ACCESS_NUMBER")
public class AccessNumber extends Resource implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    @Column(name = "VERSION")
    @Version
    private Integer version = Integer.valueOf(0);

    @Column(name = "NUMBER", nullable = false, unique = true)
    private String number;

    @JoinColumn(name = "SUBSCRIBER_ID", nullable = false)
    @ManyToOne
    private Subscriber subscriber;

    @Column(name = "GREETING_LOCATION")
    private String greetingLocation;

    @Column(name = "SUPPORTS_MESSAGE_LIGHT", nullable = false)
    private Boolean supportsMessageLight = Boolean.FALSE;

    @Transient
    private MessageLightToggler messageLightToggler = new SpotSystemMessageLightToggler();

    public Long getId()
    {
        return id;
    }

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

    public String getNumber()
    {
        return number;
    }

    public void setNumber(String number)
    {
        this.number = number;
    }

    public Subscriber getSubscriber()
    {
        return subscriber;
    }

    public void setSubscriber(Subscriber subscriber)
    {
        this.subscriber = subscriber;
    }

    public String getGreetingLocation()
    {
        return greetingLocation;
    }

    public void setGreetingLocation(String greetingLocation)
    {
        this.greetingLocation = greetingLocation;
    }

    public Boolean getSupportsMessageLight()
    {
        return supportsMessageLight;
    }

    public void setSupportsMessageLight(Boolean supportsMessageLight)
    {
        this.supportsMessageLight = supportsMessageLight;
    }

    public void useMessageLightToggler(MessageLightToggler toggler)
    {
        this.messageLightToggler = toggler;
    }
    
    @Override
    public boolean validate()
    {
        if(number == null || number.trim().equals(""))
        {
            addToErrors("number cannot be null or empty");
        }

        if(subscriber == null)
        {
            addToErrors("subscriber cannot be null");
        }

        return !hasErrors();
    }

    @Override
    public AccessNumber copy(boolean withIdAndVersion)
    {
        AccessNumber copy = new AccessNumber();
        if(withIdAndVersion)
        {
            copy.setId(id);
            copy.setVersion(version);
        }
        copy.setGreetingLocation(greetingLocation);
        copy.setNumber(number);
        copy.setSubscriber(subscriber);
        copy.setSupportsMessageLight(supportsMessageLight);
        return copy;
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

        if(!(that instanceof AccessNumber))
        {
            return false;
        }

        AccessNumber accessNumber = (AccessNumber)that;

        if(!ComparisonUtil.isEqual(accessNumber.getNumber(), getNumber()))
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
        hash *= prime + (getNumber() == null ? 0 : getNumber().hashCode());
        return hash;
    }

    @Override
    public void afterSave(PersistenceService persistenceService, HistoryService historyService)
    {
        if(supportsMessageLight)
        {
            messageLightToggler.toggleMessageLight(persistenceService, this);
        }
    }

    @Override
    public void afterUpdate(PersistenceService persistenceService, HistoryService historyService, Resource originalResource)
    {
        AccessNumber original = (AccessNumber)originalResource;
        if(supportsMessageLight && (!original.getNumber().equals(number) || !original.getSupportsMessageLight()))
        {
            messageLightToggler.toggleMessageLight(persistenceService, original, MessageLightState.OFF);
            messageLightToggler.toggleMessageLight(persistenceService, this);
        }
        else if(original.getSupportsMessageLight() && !supportsMessageLight)
        {
            messageLightToggler.toggleMessageLight(persistenceService, original, MessageLightState.OFF);
        }
    }

    @Override
    public void afterDelete(PersistenceService persistenceService, HistoryService historyService)
    {
        messageLightToggler.toggleMessageLight(persistenceService, this, MessageLightState.OFF);
    }

    public static AccessNumber queryByNumber(Session session, String number)
    {
        Criteria criteria = session.createCriteria(AccessNumber.class);
        criteria.add(Restrictions.eq("number", number));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return (AccessNumber)criteria.uniqueResult();
    }

    public static List<AccessNumber> queryBySubscriber(Session session, Subscriber subscriber)
    {
        Criteria criteria = buildCriteriaForSubscriberQuery(session, subscriber);
        return (List<AccessNumber>)criteria.list();
    }

    public static List<AccessNumber> queryBySubscriberWhereSupportsMessageLightTrue(Session session, Subscriber subscriber)
    {
        Criteria criteria = buildCriteriaForSubscriberQuery(session, subscriber);
        criteria.add(Restrictions.eq("supportsMessageLight", Boolean.TRUE));
        return (List<AccessNumber>)criteria.list();
    }
    
    public static String querySubscriberNameByAccessNumber(Session session, String number)
    {
        AccessNumber accessNumber = queryByNumber(session, number);
        
        if(accessNumber != null)
        {
            String name = accessNumber.getSubscriber().getRealName();
            
            if(name != null && !name.equals(""))
            {
                return name;
            }
        }
        
        return number;
    }

    private static Criteria buildCriteriaForSubscriberQuery(Session session, Subscriber subscriber)
    {
        Criteria criteria = session.createCriteria(AccessNumber.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.createAlias("subscriber", "subscriber_alias");
        criteria.add(Restrictions.eq("subscriber_alias.id", subscriber.getId()));
        return criteria;
    }
}
