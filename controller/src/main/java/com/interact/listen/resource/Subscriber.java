package com.interact.listen.resource;

import com.interact.listen.PersistenceService;
import com.interact.listen.history.HistoryService;
import com.interact.listen.util.ComparisonUtil;

import java.io.Serializable;
import java.util.*;

import javax.persistence.*;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.*;

@Entity
@Table(name = "SUBSCRIBER")
public class Subscriber extends Resource implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    @Column(name = "VERSION")
    @Version
    private Integer version = Integer.valueOf(0);

    @OneToMany(mappedBy = "subscriber", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.EAGER)
    private Set<AccessNumber> accessNumbers = new HashSet<AccessNumber>();

    @Column(name = "VOICEMAIL_PIN")
    private Long voicemailPin;

    @OneToMany(mappedBy = "subscriber", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE })
    private List<Voicemail> voicemails = new ArrayList<Voicemail>();

    @Column(name = "USERNAME", nullable = false, unique = true)
    private String username;

    @Column(name = "PASSWORD", nullable = false)
    private String password;

    @Column(name = "LAST_LOGIN")
    private Date lastLogin;

    @Column(name = "IS_ADMINISTRATOR")
    private Boolean isAdministrator = Boolean.FALSE;

    @OneToMany(mappedBy = "subscriber", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.EAGER)
    private Set<Conference> conferences = new HashSet<Conference>();
    
    @Column(name = "EMAIL_NOTIFICATION_ENABLED")
    private boolean isEmailNotificationEnabled = Boolean.FALSE;
    
    @Column(name = "SMS_NOTIFICATION_ENABLED")
    private boolean isSmsNotificationEnabled = Boolean.FALSE;
    
    @Column(name = "EMAIL_ADDRESS")
    private String emailAddress = "";
    
    @Column(name = "SMS_ADDRESS")
    private String smsAddress = "";

    public Set<AccessNumber> getAccessNumbers()
    {
        return accessNumbers;
    }

    public void setAccessNumbers(Set<AccessNumber> accessNumbers)
    {
        this.accessNumbers = accessNumbers;
        for(AccessNumber accessNumber : accessNumbers)
        {
            accessNumber.setSubscriber(this);
        }
    }

    public void addToAccessNumbers(AccessNumber accessNumber)
    {
        accessNumber.setSubscriber(this);
        this.accessNumbers.add(accessNumber);
    }

    public void removeFromAccessNumbers(AccessNumber accessNumber)
    {
        this.accessNumbers.remove(accessNumber);
        accessNumber.setSubscriber(null);
    }

    public String accessNumberString()
    {
        StringBuilder numbers = new StringBuilder();
        for(AccessNumber accessNumber : accessNumbers)
        {
            numbers.append(accessNumber.getNumber()).append(",");
        }
        if(accessNumbers.size() > 0)
        {
            numbers.deleteCharAt(numbers.length() - 1); // last comma
        }
        return numbers.toString();
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

    public Long getVoicemailPin()
    {
        return voicemailPin;
    }

    public void setVoicemailPin(Long voicemailPin)
    {
        this.voicemailPin = voicemailPin;
    }

    public List<Voicemail> getVoicemails()
    {
        return voicemails;
    }

    public void setVoicemails(List<Voicemail> voicemails)
    {
        this.voicemails = voicemails;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public Date getLastLogin()
    {
        return lastLogin == null ? null : new Date(lastLogin.getTime());
    }

    public void setLastLogin(Date lastLogin)
    {
        this.lastLogin = lastLogin == null ? null : new Date(lastLogin.getTime());
    }

    public Boolean getIsAdministrator()
    {
        return isAdministrator;
    }

    public void setIsAdministrator(Boolean isAdministrator)
    {
        this.isAdministrator = isAdministrator;
    }

    public Set<Conference> getConferences()
    {
        return conferences;
    }

    public void setConferences(Set<Conference> conferences)
    {
        this.conferences = conferences;
        for(Conference conference : this.conferences)
        {
            conference.setSubscriber(this);
        }
    }

    public void addToConferences(Conference conference)
    {
        conference.setSubscriber(this);
        this.conferences.add(conference);
    }

    public void removeFromConferences(Conference conference)
    {
        conference.setSubscriber(null);
        this.conferences.remove(conference);
    }
    
    public Boolean getIsEmailNotificationEnabled()
    {
        return isEmailNotificationEnabled;
    }

    public void setIsEmailNotificationEnabled(Boolean isEmailNotificationEnabled)
    {
        this.isEmailNotificationEnabled = isEmailNotificationEnabled;
    }
    
    public Boolean getIsSmsNotificationEnabled()
    {
        return isSmsNotificationEnabled;
    }

    public void setIsSmsNotificationEnabled(Boolean isSmsNotificationEnabled)
    {
        this.isSmsNotificationEnabled = isSmsNotificationEnabled;
    }
    
    public String getEmailAddress()
    {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress)
    {
        this.emailAddress = emailAddress;
    }
    
    public String getSmsAddress()
    {
        return smsAddress;
    }

    public void setSmsAddress(String smsAddress)
    {
        this.smsAddress = smsAddress;
    }

    @Override
    public boolean validate()
    {
        if(isAdministrator == null)
        {
            addToErrors("isAdministrator cannot be null");
        }

        if(password == null)
        {
            addToErrors("password cannot be null");
        }

        if(username == null)
        {
            addToErrors("username cannot be null");
        }
        
        if(voicemailPin ==  null)
        {
            addToErrors("voicemail pin cannot be null");
        }
        
        if(voicemailPin != null && String.valueOf(voicemailPin).length() > 10)
        {
            addToErrors("voicemailPin cannot be more than ten digits");
        }
        
        if(isEmailNotificationEnabled && (emailAddress == null || emailAddress.equals("")))
        {
            addToErrors("must provide an E-mail address when E-mail notifications are enabled");
        }
        
        if(isSmsNotificationEnabled && (smsAddress == null || smsAddress.equals("")))
        {
            addToErrors("must provide an SMS address when SMS notifications are enabled");
        }

        return !hasErrors();
    }

    @Override
    public Subscriber copy(boolean withIdAndVersion)
    {
        Subscriber copy = new Subscriber();
        if(withIdAndVersion)
        {
            copy.setId(id);
            copy.setVersion(version);
        }

        copy.setIsAdministrator(isAdministrator);
        copy.setLastLogin(lastLogin);
        for(AccessNumber accessNumber : accessNumbers)
        {
            copy.addToAccessNumbers(accessNumber.copy(false));
        }
        copy.setPassword(password);
        copy.setUsername(username);
        copy.setVoicemailPin(voicemailPin);
        copy.setVoicemails(voicemails);
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

        if(!(that instanceof Subscriber))
        {
            return false;
        }

        Subscriber subscriber = (Subscriber)that;

        if(!ComparisonUtil.isEqual(subscriber.getUsername(), getUsername()))
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
        hash *= prime + (getUsername() == null ? 0 : getUsername().hashCode());
        return hash;
    }

    @Override
    public void afterUpdate(PersistenceService persistenceService, Resource original)
    {
        Subscriber subscriber = (Subscriber)original;
        if(!ComparisonUtil.isEqual(subscriber.getVoicemailPin(), getVoicemailPin()))
        {
            HistoryService historyService = new HistoryService(persistenceService);
            historyService.writeChangedVoicemailPin(this, subscriber.getVoicemailPin(), getVoicemailPin());
        }
    }

    public static Long count(Session session)
    {
        Criteria criteria = session.createCriteria(Subscriber.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.setProjection(Projections.rowCount());
        return (Long)criteria.list().get(0);
    }

    public static List<Subscriber> queryAllPaged(Session session, int first, int max)
    {
        DetachedCriteria subquery = DetachedCriteria.forClass(Subscriber.class);
        subquery.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        subquery.setProjection(Projections.id());

        Criteria criteria = session.createCriteria(Subscriber.class);
        criteria.add(Subqueries.propertyIn("id", subquery));
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);

        criteria.setFirstResult(first);
        criteria.setMaxResults(max);
        criteria.addOrder(Order.asc("id"));

        criteria.setFetchMode("accessNumbers", FetchMode.SELECT);
        criteria.setFetchMode("conferences", FetchMode.SELECT);

        return (List<Subscriber>)criteria.list();
    }
}
