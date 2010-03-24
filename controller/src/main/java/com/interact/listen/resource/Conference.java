package com.interact.listen.resource;

import javax.persistence.*;

@Entity
public class Conference implements Resource
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Version
    private Integer version = Integer.valueOf(0);

    private Boolean isStarted;
    private String adminPin;
    private String number;

    public Boolean getIsStarted()
    {
        return isStarted;
    }

    public void setIsStarted(Boolean isStarted)
    {
        this.isStarted = isStarted;
    }

    public String getAdminPin()
    {
        return adminPin;
    }

    public void setAdminPin(String adminPin)
    {
        this.adminPin = adminPin;
    }

    public String getNumber()
    {
        return number;
    }

    public void setNumber(String number)
    {
        this.number = number;
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

    @Override
    public boolean validate()
    {
        if(isStarted != null && adminPin != null && !adminPin.trim().equals("") && number != null &&
           !number.trim().equals(""))
        {
            return true;
        }

        return false;
    }
}
