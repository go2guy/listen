package com.interact.listen.resource;

import com.interact.listen.history.Channel;
import com.interact.listen.util.ComparisonUtil;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name = "HISTORY")
public class History extends Resource implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    @Column(name = "VERSION")
    @Version
    private Integer version = Integer.valueOf(0);

    @Column(name = "ACTION", nullable = false)
    private String action;

    @JoinColumn(name = "PERFORMED_BY_SUBSCRIBER_ID", nullable = true)
    @OneToOne
    private Subscriber performedBySubscriber;

    @JoinColumn(name = "ON_SUBSCRIBER_ID", nullable = true)
    @OneToOne
    private Subscriber onSubscriber;

    @Column(name = "CHANNEL", nullable = false)
    @Enumerated(EnumType.STRING)
    private Channel channel;

    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

    @Column(name = "DATE_CREATED", nullable = false)
    private Date dateCreated = new Date();

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

    public void setAction(String action)
    {
        this.action = action;
    }

    public String getAction()
    {
        return action;
    }

    public Subscriber getPerformedBySubscriber()
    {
        return performedBySubscriber;
    }

    public void setPerformedBySubscriber(Subscriber performedBySubscriber)
    {
        this.performedBySubscriber = performedBySubscriber;
    }

    public Subscriber getOnSubscriber()
    {
        return onSubscriber;
    }

    public void setOnSubscriber(Subscriber onSubscriber)
    {
        this.onSubscriber = onSubscriber;
    }

    public Channel getChannel()
    {
        return channel;
    }

    public void setChannel(Channel channel)
    {
        this.channel = channel;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Date getDateCreated()
    {
        return dateCreated == null ? null : new Date(dateCreated.getTime());
    }

    public void setDateCreated(Date dateCreated)
    {
        this.dateCreated = dateCreated == null ? null : new Date(dateCreated.getTime());
    }

    @Override
    public boolean validate()
    {
        if(action == null || action.trim().equals(""))
        {
            addToErrors("action cannot be null or blank");
        }

        if(description == null || description.trim().equals(""))
        {
            addToErrors("description cannot be null or blank");
        }

        if(dateCreated == null)
        {
            addToErrors("dateCreated cannot be null");
        }

        return !hasErrors();
    }

    @Override
    public History copy(boolean withIdAndVersion)
    {
        History copy = new History();
        if(withIdAndVersion)
        {
            copy.setId(id);
            copy.setVersion(version);
        }

        copy.setAction(getAction());
        copy.setChannel(getChannel());
        copy.setDateCreated(getDateCreated());
        copy.setDescription(getDescription());
        copy.setOnSubscriber(getOnSubscriber());
        copy.setPerformedBySubscriber(getPerformedBySubscriber());
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

        if(!(that instanceof History))
        {
            return false;
        }

        History history = (History)that;

        if(!ComparisonUtil.isEqual(history.getAction(), getAction()))
        {
            return false;
        }

        if(!ComparisonUtil.isEqual(history.getDateCreated(), getDateCreated()))
        {
            return false;
        }

        if(!ComparisonUtil.isEqual(history.getDescription(), getDescription()))
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
        hash *= prime + (getAction() == null ? 0 : getAction().hashCode());
        hash *= prime + (getDateCreated() == null ? 0 : getDateCreated().hashCode());
        hash *= prime + (getDescription() == null ? 0 : getDescription().hashCode());
        return hash;
    }
}
