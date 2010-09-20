package com.interact.listen;

import com.interact.listen.resource.Subscriber;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.Session;

public final class TestUtil
{
    private TestUtil()
    {
        throw new AssertionError("Cannot instantiate utility class TestUtil");
    }

    public static Subscriber setSessionSubscriber(HttpServletRequest request, Boolean isAdministrator, Session session)
    {
        Subscriber subscriber = new Subscriber();
        subscriber.setPassword(randomString());
        subscriber.setUsername(randomString());
        subscriber.setVoicemailPin(TestUtil.randomNumeric(4).toString());
        subscriber.setIsAdministrator(isAdministrator);
        session.save(subscriber);

        request.getSession().setAttribute("subscriber", subscriber);
        return subscriber;
    }

    public static String randomString()
    {
        return UUID.randomUUID().toString();
    }

    public static Long randomNumeric(int maxlength)
    {
        return Long.valueOf(String.valueOf(System.currentTimeMillis()).substring(0, maxlength));
    }
}
