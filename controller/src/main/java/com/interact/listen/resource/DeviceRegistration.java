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
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.hibernate.criterion.*;

@Entity
@Table(name = "device_registration")
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

    @CollectionOfElements
    @Enumerated(EnumType.STRING)
    @Sort(type = SortType.NATURAL)
    @CollectionTable(name = "DEVICE_REGISTRATION_TYPE",
               joinColumns = { @JoinColumn(name = "REGISTRATION_ID") },
               uniqueConstraints = { @UniqueConstraint(columnNames = { "REGISTRATION_ID", "TYPE" }) })
    @Fetch(FetchMode.SELECT)
    @Column(name = "TYPE", nullable = false)
    private Set<RegisteredType> registeredTypes = new TreeSet<RegisteredType>();
    
    public enum DeviceType
    {
        ANDROID;
    }
    
    public enum RegisteredType
    {
        VOICEMAIL,
        CONTACTS
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

    public Set<RegisteredType> getRegisteredTypes()
    {
        return registeredTypes;
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

    public void setRegisteredTypes(Set<RegisteredType> registeredTypes)
    {
        this.registeredTypes = registeredTypes;
    }
    
    public boolean isRegistered(DeviceType dType, RegisteredType rType)
    {
        if(registrationToken == null || registrationToken.length() == 0)
        {
            return false;
        }
        return deviceType == dType && (rType == null || registeredTypes.contains(rType));
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
        if(registeredTypes == null)
        {
            addToErrors("registered types must be set");
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
        copy.setRegisteredTypes(new TreeSet<RegisteredType>(getRegisteredTypes()));
        
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

    public static DeviceRegistration queryByInfo(Session session, DeviceRegistration reg)
    {
        Criteria criteria = session.createCriteria(DeviceRegistration.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.createAlias("subscriber", "subscriber_alias");
        criteria.add(Restrictions.conjunction()
                     .add(Restrictions.eq("subscriber_alias.id", reg.getSubscriber().getId()))
                     .add(Restrictions.eq("deviceId", reg.getDeviceId()))
                     .add(Restrictions.eq("deviceType", reg.getDeviceType())));
        criteria.setMaxResults(1);
        
        return (DeviceRegistration)criteria.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public static List<DeviceRegistration> queryByDeviceType(Session session, DeviceType type)
    {
        Criteria criteria = session.createCriteria(DeviceRegistration.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.add(Restrictions.eq("deviceType", type));
        return (List<DeviceRegistration>)criteria.list();
    }

    @SuppressWarnings("unchecked")
    public static List<DeviceRegistration> queryByRegistrationId(Session session, DeviceType type, String registrationId)
    {
        Criteria criteria = session.createCriteria(DeviceRegistration.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.add(Restrictions.conjunction()
                     .add(Restrictions.eq("deviceType", type))
                     .add(Restrictions.eq("registrationToken", registrationId)));
        return (List<DeviceRegistration>)criteria.list();
    }
    
}
