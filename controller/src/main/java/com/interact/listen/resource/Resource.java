package com.interact.listen.resource;

import java.util.ArrayList;
import javax.persistence.*;

public abstract class Resource
{
    @Transient
    private ArrayList<String> errors = new ArrayList<String>(3);
    
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
}
