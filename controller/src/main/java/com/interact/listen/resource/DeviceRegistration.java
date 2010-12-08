package com.interact.listen.resource;

import com.interact.listen.PersistenceService;
import com.interact.listen.history.HistoryService;
import com.interact.listen.util.ComparisonUtil;

import java.io.Serializable;
import java.util.*;

import javax.persistence.*;

//import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.*;

@Entity
@Table(name = "DEVICE_REGISTRATION")
public class DeviceRegistration extends Resource implements Serializable
{
    private static final long serialVersionUID = 1L;

    //private static final Logger LOG = Logger.getLogger(DeviceRegistration.class);

    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    @Column(name = "VERSION")
    @Version
    private Integer version = Integer.valueOf(0);

    @JoinColumn(name = "SUBSCRIBER_ID", nullable = false)
    @ManyToOne
    private Subscriber subscriber;

    @Column(name = "DEVICE_ID", nullable = false)
    private String deviceId;

    @Column(name = "DEVICE_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private DeviceType deviceType = DeviceType.ANDROID;

    @Column(name = "REGISTRATION_TOKEN")
    private String registrationToken;

    public enum DeviceType
    {
        ANDROID;
    }

    @Override
    public Long getId()
    {
        return id;
    }

    public Integer getVersion()
    {
        return version;
    }

    public Subscriber getSubscriber()
    {
        return subscriber;
    }

    public String getDeviceId()
    {
        return deviceId;
    }
    
    public DeviceType getDeviceType()
    {
        return deviceType;
    }

    public String getRegistrationToken()
    {
        return registrationToken;
    }

    @Override
    public void setId(Long id)
    {
        this.id = id;
    }

    public void setVersion(Integer version)
    {
        this.version = version;
    }

    public void setSubscriber(Subscriber subscriber)
    {
        this.subscriber = subscriber;
    }

    public void setDeviceId(String deviceId)
    {
        this.deviceId = deviceId;
    }
    
    public void setDeviceType(DeviceType deviceType)
    {
        this.deviceType = deviceType;
    }
    
    public void setRegistrationToken(String registrationToken)
    {
        this.registrationToken = registrationToken;
    }

    @Override
    public boolean validate()
    {
        if(deviceType == null)
        {
            addToErrors("provide a device type");
        }
        if(deviceId == null)
        {
            addToErrors("provide a device ID");
        }
        if(registrationToken == null)
        {
            addToErrors("provide a registation token");
        }
        if(subscriber == null)
        {
            addToErrors("set the subscriber");
        }

        return !hasErrors();
    }

    @Override
    public DeviceRegistration copy(boolean withIdAndVersion)
    {
        DeviceRegistration copy = new DeviceRegistration();
        if(withIdAndVersion)
        {
            copy.setId(id);
            copy.setVersion(version);
        }

        copy.setSubscriber(subscriber);
        copy.setDeviceId(deviceId);
        copy.setDeviceType(deviceType);
        copy.setRegistrationToken(registrationToken);
        
        return copy;
    }

    @Override
    public boolean equals(Object that)
    {
        if(this == that)
        {
            return true;
        }

        if(that == null || !(that instanceof DeviceRegistration))
        {
            return false;
        }

        DeviceRegistration dr = (DeviceRegistration)that;
        return ComparisonUtil.isEqual(dr.getSubscriber(), getSubscriber()) && ComparisonUtil.isEqual(dr.getDeviceId(), getDeviceId());
    }

    @Override
    public int hashCode()
    {
        return deviceId == null ? 0 : deviceId.hashCode();
    }

    @Override
    public void afterSave(PersistenceService persistenceService, HistoryService historyService)
    {
        // Need history?
    }

    @Override
    public void afterUpdate(PersistenceService persistenceService, HistoryService historyService, Resource original)
    {
        // Need history?
    }

    @Override
    public void afterDelete(PersistenceService persistenceService, HistoryService historyService)
    {
        // Need history?
    }

    public static DeviceRegistration queryById(Session session, Long id)
    {
        return (DeviceRegistration)session.get(DeviceRegistration.class, id);
    }

    @SuppressWarnings("unchecked")
    public static List<DeviceRegistration> queryBySubscriber(Session session, Subscriber subscriber, DeviceType type)
    {
        Criteria criteria = buildCriteriaForSubscriberQuery(session, subscriber);
        if(type != null)
        {
            criteria.add(Restrictions.eq("deviceType", type));
        }
        return (List<DeviceRegistration>)criteria.list();
    }
    
    public static DeviceRegistration queryByInfo(Session session, DeviceRegistration reg)
    {
        Criteria criteria = buildCriteriaForSubscriberQuery(session, reg.getSubscriber());
        
        criteria.add(Restrictions.eq("deviceId", reg.getDeviceId()));
        criteria.add(Restrictions.eq("deviceType", reg.getDeviceType()));
        criteria.setMaxResults(1);
        
        return (DeviceRegistration)criteria.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public static List<DeviceRegistration> queryByDevice(Session session, DeviceType type, String registrationId)
    {
        Criteria criteria = session.createCriteria(DeviceRegistration.class);
        criteria.add(Restrictions.eq("deviceType", type));
        
        if(registrationId != null)
        {
            criteria.add(Restrictions.eq("registrationToken", registrationId));
        }
        
        return (List<DeviceRegistration>)criteria.list();
    }

    public static Long count(Session session)
    {
        Criteria criteria = session.createCriteria(DeviceRegistration.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.setProjection(Projections.rowCount());
        return (Long)criteria.list().get(0);
    }

    private static Criteria buildCriteriaForSubscriberQuery(Session session, Subscriber subscriber)
    {
        Criteria criteria = session.createCriteria(DeviceRegistration.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.createAlias("subscriber", "subscriber_alias");
        criteria.add(Restrictions.eq("subscriber_alias.id", subscriber.getId()));
        return criteria;
    }

}
