package com.interact.listen.resource;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "HISTORY")
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
}
