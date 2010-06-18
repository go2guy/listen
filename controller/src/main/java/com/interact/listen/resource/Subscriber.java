package com.interact.listen.resource;

import com.interact.listen.util.ComparisonUtil;

import java.io.Serializable;
import java.util.*;

import javax.persistence.*;

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

    @Column(name = "NUMBER", unique = true, nullable = false)
    private String number;

    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.EAGER)
    private Set<AccessNumber> accessNumbers = new HashSet<AccessNumber>();

    @Column(name = "VOICEMAIL_GREETING_LOCATION")
    private String voicemailGreetingLocation;

    @Column(name = "VOICEMAIL_PIN")
    private String voicemailPin;

    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private List<Voicemail> voicemails = new ArrayList<Voicemail>();

    @Column(name = "USERNAME", nullable = false, unique = true)
    private String username;

    @Column(name = "PASSWORD", nullable = false)
    private String password;

    @Column(name = "LAST_LOGIN")
    private Date lastLogin;

    @Column(name = "IS_ADMINISTRATOR")
    private Boolean isAdministrator = Boolean.FALSE;

    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.EAGER)
    private Set<Conference> conferences = new HashSet<Conference>();

    public String getNumber()
    {
        return number;
    }

    public void setNumber(String number)
    {
        this.number = number;
    }

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

    public String getVoicemailGreetingLocation()
    {
        return voicemailGreetingLocation;
    }

    public void setVoicemailGreetingLocation(String voicemailGreetingLocation)
    {
        this.voicemailGreetingLocation = voicemailGreetingLocation;
    }

    public String getVoicemailPin()
    {
        return voicemailPin;
    }

    public void setVoicemailPin(String voicemailPin)
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

    @Override
    public boolean validate()
    {
        if(isAdministrator == null)
        {
            addToErrors("isAdministrator cannot be null");
        }

        if(number == null || number.trim().equals(""))
        {
            addToErrors("Subscriber must have a number");
        }

        if(password == null)
        {
            addToErrors("password cannot be null");
        }

        if(username == null)
        {
            addToErrors("username cannot be null");
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
        copy.setNumber(number);
        for(AccessNumber accessNumber : accessNumbers)
        {
            copy.addToAccessNumbers(accessNumber.copy(false));
        }
        copy.setPassword(password);
        copy.setUsername(username);
        copy.setVoicemailGreetingLocation(voicemailGreetingLocation);
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
}
