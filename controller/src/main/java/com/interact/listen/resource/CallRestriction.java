package com.interact.listen.resource;

import java.io.Serializable;
import java.util.*;

import javax.persistence.*;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.*;

@Entity
@Table(name = "CALL_RESTRICTION")
public class CallRestriction extends Resource implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    @Column(name = "VERSION")
    @Version
    private Integer version = Integer.valueOf(0);
    
    @Column(name = "DIRECTIVE")
    @Enumerated(EnumType.STRING)
    private Directive directive;
    
    @Column(name = "FOR_EVERYONE")
    private Boolean forEveryone = false;
    
    @JoinColumn(name = "SUBSCRIBER_ID")
    @ManyToOne
    private Subscriber subscriber;
    
    @Column(name = "DESTINATION", nullable = false)
    private String destination;
    
    public enum Directive
    {
        ALLOW, DENY;
    }

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

    public Directive getDirective()
    {
        return directive;
    }

    public void setDirective(Directive directive)
    {
        this.directive = directive;
    }

    public Boolean getForEveryone()
    {
        return forEveryone;
    }

    public void setForEveryone(Boolean forEveryone)
    {
        this.forEveryone = forEveryone;
    }

    public Subscriber getSubscriber()
    {
        return subscriber;
    }

    public void setSubscriber(Subscriber subscriber)
    {
        this.subscriber = subscriber;
    }

    public String getDestination()
    {
        return destination;
    }

    public void setDestination(String destination)
    {
        this.destination = destination;
    }
    
    @Override
    public boolean validate()
    {
        if(destination == null || destination.equals(""))
        {
            addToErrors("destination cannot be blank or null");
        }
        
        if(forEveryone.equals(Boolean.FALSE) && subscriber ==  null)
        {
            addToErrors("subscriber cannot be null when forEveryone is false");
        }
        
        return !hasErrors();
    }

    public static List<CallRestriction> queryEveryoneAndSubscriberSpecficByDirective(Session session,
                                                                                     Subscriber subscriber,
                                                                                     Directive directive)
    {
        Criteria criteria = session.createCriteria(CallRestriction.class);
        criteria.createAlias("subscriber", "subscriber_alias", Criteria.LEFT_JOIN);
        criteria.add(Restrictions.or(Restrictions.eq("subscriber_alias.id", subscriber.getId()),
                                     Restrictions.eq("forEveryone", true)));
        criteria.add(Restrictions.eq("directive", directive));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.setFetchMode("subscriber", FetchMode.SELECT);

        return (List<CallRestriction>)criteria.list();
    }

    public static Map<String, List<CallRestriction>> queryAllGroupedByDestination(Session session)
    {
        List<CallRestriction> all = queryAll(session);
        Map<String, List<CallRestriction>> destinations = new HashMap<String, List<CallRestriction>>();

        for(CallRestriction restriction : all)
        {
            String destination = restriction.getDestination();
            List<CallRestriction> destinationList = destinations.get(destination);
            if(destinationList == null)
            {
                destinationList = new ArrayList<CallRestriction>();
            }

            destinationList.add(restriction);
            destinations.put(destination, destinationList);
        }
        return destinations;
    }

    public static List<CallRestriction> queryAll(Session session)
    {
        Criteria criteria = session.createCriteria(CallRestriction.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return (List<CallRestriction>)criteria.list();
    }

    public static void deleteAll(Session session)
    {
        org.hibernate.Query query = session.createQuery("delete from CallRestriction");
        query.executeUpdate();
    }

    @Override
    public CallRestriction copy(boolean withIdAndVersion)
    {
        CallRestriction copy = new CallRestriction();
        if(withIdAndVersion)
        {
            copy.setId(getId());
            copy.setVersion(getVersion());
        }

        copy.setDirective(getDirective());
        copy.setForEveryone(forEveryone);
        copy.setSubscriber(subscriber);
        copy.setDestination(destination);
        return copy;
    }
}
