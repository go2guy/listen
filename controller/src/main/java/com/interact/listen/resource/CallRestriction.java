package com.interact.listen.resource;

import java.io.Serializable;

import javax.persistence.*;

@Entity
@Table(name = "CALL_RESTRICTION")
public class CallRestriction extends Resource implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    @Column(name = "VERSION")
    @Version
    private Integer version = Integer.valueOf(0);
    
    @Column(name = "DIRECTIVE")
    @Enumerated(EnumType.STRING)
    private Directive directive;
    
    @Column(name = "FOR_EVERYONE")
    private Boolean forEveryone = false;
    
    @JoinColumn(name = "SUBSCRIBER_ID")
    @ManyToOne
    private Subscriber subscriber;
    
    @Column(name = "DESTINATION", nullable = false)
    private String destination;
    
    public enum Directive
    {
        ALLOW, DENY;
    }

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

    public Directive getDirective()
    {
        return directive;
    }

    public void setDirective(Directive directive)
    {
        this.directive = directive;
    }

    public Boolean getForEveryone()
    {
        return forEveryone;
    }

    public void setForEveryone(Boolean forEveryone)
    {
        this.forEveryone = forEveryone;
    }

    public Subscriber getSubscriber()
    {
        return subscriber;
    }

    public void setSubscriber(Subscriber subscriber)
    {
        this.subscriber = subscriber;
    }

    public String getDestination()
    {
        return destination;
    }

    public void setDestination(String destination)
    {
        this.destination = destination;
    }
    
    @Override
    public boolean validate()
    {
        if(destination == null || destination.equals(""))
        {
            addToErrors("destination cannot be blank or null");
        }
        
        if(forEveryone.equals(Boolean.FALSE) && subscriber ==  null)
        {
            addToErrors("subscriber cannot be null when forEveryone is false");
        }
        
        return !hasErrors();
    }
    
    @Override
    public CallRestriction copy(boolean withIdAndVersion)
    {
        CallRestriction copy = new CallRestriction();
        if(withIdAndVersion)
        {
            copy.setId(getId());
            copy.setVersion(getVersion());
        }

        copy.setDirective(getDirective());
        copy.setForEveryone(forEveryone);
        copy.setSubscriber(subscriber);
        copy.setDestination(destination);
        return copy;
    }
}
