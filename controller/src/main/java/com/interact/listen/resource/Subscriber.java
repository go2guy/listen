package com.interact.listen.resource;

import com.interact.listen.db.DatabaseUtil;
import com.thoughtworks.xstream.XStream;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Subscriber implements Resource
{
    protected String href;
    protected Long id;
    private String number;

    public String getHref()
    {
        return href;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
        this.href = "/subscribers/" + id;
    }

    public String getNumber()
    {
        return number;
    }

    public void setNumber(String number)
    {
        this.number = number;
    }

    public String toXml()
    {
        XStream xstream = new XStream();
        xstream.alias("subscriber", Subscriber.class);
        xstream.useAttributeFor(Subscriber.class, "href");

        return xstream.toXML(this);
    }

    public static Subscriber get(Long id) throws SQLException
    {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try
        {
            connection = DatabaseUtil.getConnection();
            statement = connection.prepareStatement("SELECT id, number FROM subscriber WHERE id=?");
            statement.setLong(1, id);
            resultSet = statement.executeQuery();

            if(!resultSet.next())
            {
                return null;
            }

            Subscriber subscriber = fromResultSet(resultSet);
            return subscriber;
        }
        finally
        {
            DatabaseUtil.closeSilently(connection);
            DatabaseUtil.closeSilently(statement);
            DatabaseUtil.closeSilently(resultSet);
        }
    }

    public static List<Subscriber> list() throws SQLException
    {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        List<Subscriber> list = new ArrayList<Subscriber>();

        try
        {
            connection = DatabaseUtil.getConnection();
            statement = connection.prepareStatement("SELECT id, number FROM subscriber");
            resultSet = statement.executeQuery();

            while(resultSet.next())
            {
                Subscriber subscriber = fromResultSet(resultSet);
                list.add(subscriber);
            }
        }
        finally
        {
            DatabaseUtil.closeSilently(connection);
            DatabaseUtil.closeSilently(statement);
            DatabaseUtil.closeSilently(resultSet);
        }

        return list;
    }

    public static List<Subscriber> find(String number) throws SQLException
    {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        List<Subscriber> list = new ArrayList<Subscriber>();

        try
        {
            connection = DatabaseUtil.getConnection();
            statement = connection.prepareStatement("SELECT id, number FROM subscriber WHERE number = ?");
            statement.setString(1, number);
            resultSet = statement.executeQuery();

            while(resultSet.next())
            {
                Subscriber subscriber = fromResultSet(resultSet);
                list.add(subscriber);
            }
        }
        finally
        {
            DatabaseUtil.closeSilently(connection);
            DatabaseUtil.closeSilently(statement);
            DatabaseUtil.closeSilently(resultSet);
        }

        return list;
    }

    private static Subscriber fromResultSet(ResultSet resultSet) throws SQLException
    {
        Subscriber subscriber = new Subscriber();
        subscriber.setId(resultSet.getLong("id"));
        subscriber.setNumber(resultSet.getString("number"));
        return subscriber;
    }
}
