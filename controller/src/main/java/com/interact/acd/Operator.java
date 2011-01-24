package com.interact.acd;

import com.interact.acd.router.Destination;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Person expecting to receive phone calls routed to them from the IVR.
 */
public class Operator implements Destination
{
    private final String phoneNumber;

    public Operator(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String getAddress()
    {
        return phoneNumber;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj == null)
        {
            return false;
        }

        if(obj == this)
        {
            return true;
        }

        if(obj.getClass() != this.getClass())
        {
            return false;
        }

        Operator that = (Operator)obj;

        EqualsBuilder eqb = new EqualsBuilder();
        eqb.append(phoneNumber, that.phoneNumber);
        return eqb.isEquals();
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder hcb = new HashCodeBuilder(617, 1279);
        hcb.append(phoneNumber);
        return hcb.toHashCode();
    }
}
