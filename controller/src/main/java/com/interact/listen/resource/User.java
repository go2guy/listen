package com.interact.listen.resource;

import com.interact.listen.util.ComparisonUtil;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

@Entity
@Table(name = "USER")
public class User extends Resource implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    @Column(name = "VERSION")
    @Version
    private Integer version = Integer.valueOf(0);

    @OneToOne
    @PrimaryKeyJoinColumn
    private Subscriber subscriber;

    @Column(name = "USERNAME", nullable = false, unique = true)
    private String username;

    @Column(name = "PASSWORD", nullable = false)
    private String password;

    @Column(name = "LAST_LOGIN")
    private Date lastLogin = new Date();

    @Column(name = "IS_ADMINISTRATOR")
    private Boolean isAdministrator = Boolean.FALSE;

    @JoinTable(name = "USER_CONFERENCE",
               joinColumns = @JoinColumn(name = "USER_ID", unique = true),
               inverseJoinColumns = @JoinColumn(name = "CONFERENCE_ID"))
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Conference> conferences = new HashSet<Conference>();

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

    public void setSubscriber(Subscriber subscriber)
    {
        this.subscriber = subscriber;
    }

    public Subscriber getSubscriber()
    {
        return subscriber;
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
    }

    public void addToConferences(Conference conference)
    {
        this.conferences.add(conference);
    }

    public void removeFromConferences(Conference conference)
    {
        this.conferences.remove(conference);
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

        if(subscriber == null)
        {
            addToErrors("subscriber cannot be null");
        }

        if(username == null)
        {
            addToErrors("username cannot be null");
        }

        return !hasErrors();
    }

    @Override
    public User copy(boolean withIdAndVersion)
    {
        User copy = new User();
        if(withIdAndVersion)
        {
            copy.setId(id);
            copy.setVersion(version);
        }

        copy.setPassword(password);
        copy.setSubscriber(subscriber);
        copy.setUsername(username);
        copy.setLastLogin(lastLogin == null ? null : new Date(lastLogin.getTime()));
        copy.setIsAdministrator(isAdministrator);

        for(Conference conference : conferences)
        {
            copy.addToConferences(conference);
        }
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

        if(!(that instanceof User))
        {
            return false;
        }

        final User user = (User)that;

        if(!ComparisonUtil.isEqual(user.getUsername(), getUsername()))
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
