package com.interact.listen.resource;

import java.util.ArrayList;
import javax.persistence.*;

import org.hibernate.Session;

public abstract class Resource
{
    @Transient
    private ArrayList<String> errors = new ArrayList<String>();

    public abstract void setId(Long id);

    public abstract Long getId();

    public abstract boolean validate();

    public boolean hasErrors()
    {
        return errors.size() > 0;
    }

    public void addToErrors(String errorMessage)
    {
        errors.add(errorMessage);
    }

    public ArrayList<String> errors()
    {
        return errors;
    }

    public void beforeSave(Session session)
    { }

    public void beforeUpdate(Session session, Resource original)
    { }

    public void beforeDelete(Session session)
    { }
}
