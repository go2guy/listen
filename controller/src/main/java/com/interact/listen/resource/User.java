package com.interact.listen.resource;

import java.io.Serializable;

import javax.persistence.*;

@Entity
public class User extends Resource implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Version
    private Integer version = Integer.valueOf(0);

    @OneToOne
    private Subscriber subscriber;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

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

    @Override
    public boolean validate()
    {
        boolean isValid = true;
        if(subscriber == null)
        {
            addToErrors("subscriber cannot be null");
            isValid = false;
        }

        if(username == null)
        {
            addToErrors("username cannot be null");
            isValid = false;
        }

        if(password == null)
        {
            addToErrors("password cannot be null");
            isValid = false;
        }
        return isValid;
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
        return copy;
    }
}
