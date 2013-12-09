package com.interact.listen.resource;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "history")
public abstract class History extends Resource implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    @Column(name = "VERSION")
    @Version
    private Integer version = Integer.valueOf(0);

    @Column(name = "DATE")
    private Date date = new Date();

    @JoinColumn(name = "SUBSCRIBER_ID", nullable = true)
    @OneToOne
    private Subscriber subscriber;

    @Column(name = "SERVICE", nullable = true)
    private String service;

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

    public Date getDate()
    {
        return date == null ? date : new Date(date.getTime());
    }

    public void setDate(Date date)
    {
        this.date = date == null ? null : new Date(date.getTime());
    }

    public Subscriber getSubscriber()
    {
        return subscriber;
    }

    public void setSubscriber(Subscriber subscriber)
    {
        this.subscriber = subscriber;
    }

    public String getService()
    {
        return service;
    }

    public void setService(String service)
    {
        this.service = service;
    }

    public static Long count(Session session)
    {
        Criteria criteria = session.createCriteria(History.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.setProjection(Projections.rowCount());
        return (Long)criteria.list().get(0);
    }
    
    public static Long countBySubscriber(Session session, Subscriber subscriber)
    {
        String hql = "select count(*) from History h where h.subscriber = :subscriber or h.onSubscriber = :subscriber";
        return (Long)session.createQuery(hql).setEntity("subscriber", subscriber).list().get(0);
    }

    public static final List<History> queryAllPaged(Session session, int first, int max, Subscriber subscriber)
    {
        //Kind of gross here.  When querying Action Histories, we need any id where either the subscriberId
        //or the onSubscriberId is the specific subscriber's id.  For call details, we need just the subscriberId
        //matching records.  If new history types are added, a new subquery for those ids should be made with 
        //appropriate criteria for what makes it a match with a specific subscriber
        DetachedCriteria actionHistorySubquery = DetachedCriteria.forClass(ActionHistory.class);
        if(subscriber != null)
        {
            actionHistorySubquery.createAlias("subscriber", "subscriber_alias", Criteria.LEFT_JOIN);
            actionHistorySubquery.createAlias("onSubscriber", "on_subscriber_alias", Criteria.LEFT_JOIN);
            actionHistorySubquery.add(Restrictions.or(Restrictions.eq("subscriber_alias.id", subscriber.getId()),
                                                      Restrictions.eq("on_subscriber_alias.id", subscriber.getId())));
            actionHistorySubquery.setFetchMode("subscriber", FetchMode.SELECT);
            actionHistorySubquery.setFetchMode("onSubscriber", FetchMode.SELECT);
        }
        
        actionHistorySubquery.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        actionHistorySubquery.setProjection(Projections.id());
        
        DetachedCriteria callDetailSubquery = DetachedCriteria.forClass(CallDetailRecord.class);
        if(subscriber != null)
        {
            callDetailSubquery.createAlias("subscriber", "subscriber_alias");
            callDetailSubquery.add(Restrictions.eq("subscriber_alias.id", subscriber.getId()));
            callDetailSubquery.setFetchMode("subscriber", FetchMode.SELECT);
        }
        
        callDetailSubquery.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        callDetailSubquery.setProjection(Projections.id());
        
        Disjunction disjunction = Restrictions.disjunction();
        disjunction.add(Subqueries.propertyIn("id", actionHistorySubquery));
        disjunction.add(Subqueries.propertyIn("id", callDetailSubquery));

        Criteria criteria = session.createCriteria(History.class);
        criteria.add(disjunction);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        criteria.setFirstResult(first);
        criteria.setMaxResults(max);
        criteria.addOrder(Order.desc("date"));

        criteria.setFetchMode("subscriber", FetchMode.SELECT);
        criteria.setFetchMode("onSubscriber", FetchMode.SELECT);

        return (List<History>)criteria.list();
    }

    public static final void deleteAllBySubscriber(Session session, Subscriber subscriber)
    {
        String hql = "delete from History h where h.subscriber = :subscriber or h.onSubscriber = :subscriber";
        session.createQuery(hql).setEntity("subscriber", subscriber).executeUpdate();
    }
}
