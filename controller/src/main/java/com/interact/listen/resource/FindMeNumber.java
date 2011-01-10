package com.interact.listen.resource;

import java.io.Serializable;
import java.util.*;

import javax.persistence.*;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@Entity
@Table(name = "FIND_ME_NUMBER")
public class FindMeNumber extends Resource implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    @Column(name = "VERSION")
    @Version
    private Integer version = Integer.valueOf(0);

    @Column(name = "NUMBER", nullable = false)
    private String number;

    @JoinColumn(name = "SUBSCRIBER_ID", nullable = false)
    @ManyToOne
    private Subscriber subscriber;

    @Column(name = "ENABLED", nullable = false)
    private Boolean enabled = Boolean.FALSE;

    @Column(name = "PRIORITY", nullable = false)
    private Integer priority;

    @Column(name = "DIAL_DURATION", nullable = false)
    private Integer dialDuration;

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

    public Integer getPriority()
    {
        return priority;
    }

    public void setPriority(Integer priority)
    {
        this.priority = priority;
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

    public Boolean getEnabled()
    {
        return enabled;
    }

    public void setEnabled(Boolean enabled)
    {
        this.enabled = enabled;
    }

    public Integer getDialDuration()
    {
        return dialDuration;
    }

    public void setDialDuration(Integer dialDuration)
    {
        this.dialDuration = dialDuration;
    }

    public static List<FindMeNumber> queryBySubscriberOrderByPriority(Session session, Subscriber subscriber,
                                                                      boolean includeDisabled)
    {
        DetachedCriteria subquery = DetachedCriteria.forClass(FindMeNumber.class);
        subquery.createAlias("subscriber", "subscriber_alias");
        subquery.add(Restrictions.eq("subscriber_alias.id", subscriber.getId()));
        subquery.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        subquery.setProjection(Projections.id());

        Criteria criteria = session.createCriteria(FindMeNumber.class);
        criteria.add(Subqueries.propertyIn("id", subquery));
        if(!includeDisabled)
        {
            criteria.add(Restrictions.eq("enabled", true));
        }
        criteria.addOrder(Order.asc("priority"));

        criteria.setFetchMode("subscriber", FetchMode.SELECT);
        return (List<FindMeNumber>)criteria.list();
    }

    public static void deleteBySubscriber(Session session, Subscriber subscriber)
    {
        String hql = "delete from FindMeNumber f where f.subscriber.id = :s";
        org.hibernate.Query query = session.createQuery(hql);
        query.setParameter("s", subscriber.getId());
        query.executeUpdate();
    }

    /**
     * Retrieves the provided subscribers Find Me numbers, grouped into sets by priority. The returned Map will be a
     * TreeMap, keyed by priority. Each map entry will be a set of FindMeNumbers.
     * 
     * @param session
     * @param subscriber
     */
    public static TreeMap<Integer, Set<FindMeNumber>> queryBySubscriberInPriorityGroups(Session session,
                                                                                        Subscriber subscriber,
                                                                                        boolean includeDisabled)
    {
        List<FindMeNumber> results = queryBySubscriberOrderByPriority(session, subscriber, includeDisabled);
        TreeMap<Integer, Set<FindMeNumber>> groups = new TreeMap<Integer, Set<FindMeNumber>>();
        for(FindMeNumber result : results)
        {
            if(groups.get(result.getPriority()) == null)
            {
                groups.put(result.getPriority(), new HashSet<FindMeNumber>());
            }
            groups.get(result.getPriority()).add(result);
        }
        return groups;
    }
    
    public static JSONArray groupsToJson(TreeMap<Integer, Set<FindMeNumber>> groups)
    {
        JSONArray json = new JSONArray();
        for(Set<FindMeNumber> group : groups.values())
        {
            JSONArray jsonGroup = new JSONArray();
            for(FindMeNumber number : group)
            {
                JSONObject jsonNumber = new JSONObject();
                jsonNumber.put("number", number.getNumber());
                jsonNumber.put("duration", number.getDialDuration());
                jsonNumber.put("enabled", number.getEnabled());
                jsonGroup.add(jsonNumber);
            }
            json.add(jsonGroup);
        }
        return json;
    }

    @Override
    public Resource copy(boolean withIdAndVersion)
    {
        FindMeNumber copy = new FindMeNumber();
        copy.setDialDuration(dialDuration);
        copy.setEnabled(enabled);
        copy.setNumber(number);
        copy.setPriority(priority);
        copy.setSubscriber(subscriber);

        if(withIdAndVersion)
        {
            copy.setId(id);
            copy.setVersion(version);
        }
        return copy;
    }
    
    @Override
    public boolean validate()
    {
        if(dialDuration == null)
        {
            addToErrors("dialDuration cannot be null");
        }
        else if(dialDuration < 1)
        {
            addToErrors("dialDuration cannot be less than one");
        }
        
        if(enabled == null)
        {
            addToErrors("enabled cannot be null");
        }
        
        if(priority == null)
        {
            addToErrors("priority cannot be null");
        }
        else if(priority < 0)
        {
            addToErrors("priority cannot be less than zero");
        }
        
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
}
