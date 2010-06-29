package com.interact.listen.resource;

import com.interact.listen.history.Channel;
import com.interact.listen.util.ComparisonUtil;

import java.io.Serializable;

import javax.persistence.*;

@Entity
@Table(name = "ACTION_HISTORY")
public class ActionHistory extends History implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Column(name = "ACTION", nullable = true)
    private String action;

    @JoinColumn(name = "ON_SUBSCRIBER_ID", nullable = true)
    @OneToOne
    private Subscriber onSubscriber;

    @Column(name = "CHANNEL", nullable = true)
    @Enumerated(EnumType.STRING)
    private Channel channel;

    @Column(name = "DESCRIPTION", nullable = true)
    private String description;

    public void setAction(String action)
    {
        this.action = action;
    }

    public String getAction()
    {
        return action;
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

        if(getDate() == null)
        {
            addToErrors("dateCreated cannot be null");
        }

        return !hasErrors();
    }

    @Override
    public ActionHistory copy(boolean withIdAndVersion)
    {
        ActionHistory copy = new ActionHistory();
        if(withIdAndVersion)
        {
            copy.setId(getId());
            copy.setVersion(getVersion());
        }

        copy.setAction(getAction());
        copy.setChannel(getChannel());
        copy.setDate(getDate());
        copy.setDescription(getDescription());
        copy.setOnSubscriber(getOnSubscriber());
        copy.setService(getService());
        copy.setSubscriber(getSubscriber());
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

        if(!(that instanceof ActionHistory))
        {
            return false;
        }

        ActionHistory history = (ActionHistory)that;

        if(!ComparisonUtil.isEqual(history.getAction(), getAction()))
        {
            return false;
        }

        if(!ComparisonUtil.isEqual(history.getDate(), getDate()))
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
        hash *= prime + (getDate() == null ? 0 : getDate().hashCode());
        hash *= prime + (getDescription() == null ? 0 : getDescription().hashCode());
        return hash;
    }
}
