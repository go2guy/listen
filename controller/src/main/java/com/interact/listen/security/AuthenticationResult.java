package com.interact.listen.security;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AuthenticationResult
{
    private Set<String> groups = new HashSet<String>();
    private boolean successful = false;
    private String displayName = "";
    private String telephoneNumber;
    private String mail;

    public Set<String> getGroups()
    {
        return groups;
    }

    public void setGroups(Set<String> groups)
    {
        this.groups = groups;
    }

    public boolean isSuccessful()
    {
        return successful;
    }

    public void setSuccessful(boolean successful)
    {
        this.successful = successful;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    public String getTelephoneNumber()
    {
        return telephoneNumber;
    }

    public void setTelephoneNumber(String telephoneNumber)
    {
        this.telephoneNumber = telephoneNumber;
    }

    public void setMail(String mail)
    {
        this.mail = mail;
    }

    public String getMail()
    {
        return mail;
    }

    @Override
    public String toString()
    {
        StringBuilder string = new StringBuilder();
        string.append("successful = [").append(successful).append("], ");
        string.append("displayName = [").append(displayName).append("], ");
        string.append("telephoneNumber = [").append(telephoneNumber).append("], ");
        string.append("mail = [").append(mail).append("], ");
        string.append("groups = [").append(Arrays.toString(groups.toArray())).append("]");
        return string.toString();
    }
}
