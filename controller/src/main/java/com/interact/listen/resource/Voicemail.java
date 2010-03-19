package com.interact.listen.resource;

import com.interact.listen.db.DatabaseUtil;
import com.thoughtworks.xstream.XStream;

import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Voicemail implements Resource
{
    protected String href;
    protected Long id;
    private Subscriber subscriber;
    private String fileLocation;

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

    public Subscriber getSubscriber()
    {
        return subscriber;
    }

    public void setSubscriber(Subscriber subscriber)
    {
        this.subscriber = subscriber;
    }

    public String getFileLocation()
    {
        return fileLocation;
    }

    public void setFileLocation(String fileLocation)
    {
        this.fileLocation = fileLocation;
    }

    public String toXml()
    {
        XStream xstream = new XStream();
        xstream.alias("voicemail", Voicemail.class);
        xstream.useAttributeFor(Voicemail.class, "href");

        xstream.alias("subscriber", Subscriber.class);
        xstream.useAttributeFor(Subscriber.class, "href");
        xstream.omitField(Subscriber.class, "id");
        xstream.omitField(Subscriber.class, "number");

        return xstream.toXML(this);
    }

    public static Voicemail fromXml(InputStream inputStream)
    {
        XStream xstream = new XStream();
        xstream.alias("voicemail", Voicemail.class);
        xstream.useAttributeFor(Voicemail.class, "href");

        xstream.alias("subscriber", Subscriber.class);
        xstream.useAttributeFor(Subscriber.class, "href");
        xstream.omitField(Subscriber.class, "id");
        xstream.omitField(Subscriber.class, "number");

        return (Voicemail)xstream.fromXML(inputStream);
    }

    public static Voicemail get(Long id) throws SQLException
    {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try
        {
            connection = DatabaseUtil.getConnection();

            String sql = "SELECT id, subscriber_id, file_location FROM voicemail WHERE id=?";
            statement = connection.prepareStatement(sql);
            statement.setLong(1, id);
            resultSet = statement.executeQuery();

            if(!resultSet.next())
            {
                return null;
            }

            Voicemail voicemail = fromResultSet(resultSet);
            return voicemail;
        }
        finally
        {
            DatabaseUtil.closeSilently(connection);
            DatabaseUtil.closeSilently(statement);
            DatabaseUtil.closeSilently(resultSet);
        }
    }

    public static List<Voicemail> list() throws SQLException
    {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        List<Voicemail> list = new ArrayList<Voicemail>();

        try
        {
            connection = DatabaseUtil.getConnection();
            statement = connection.prepareStatement("SELECT id, subscriber_id, file_location FROM voicemail");
            resultSet = statement.executeQuery();

            while(resultSet.next())
            {
                Voicemail voicemail = fromResultSet(resultSet);
                list.add(voicemail);
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

    public void save() throws SQLException
    {
        if(this.id == null)
        {
            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet resultSet = null;

            try
            {
                connection = DatabaseUtil.getConnection();
                String sql = "INSERT INTO voicemail (subscriber_id, file_location) VALUES (?, ?)";
                statement = connection.prepareStatement(sql);

                statement.setLong(1, this.subscriber.getId());
                statement.setString(2, this.fileLocation);

                statement.executeUpdate();

                resultSet = statement.getGeneratedKeys();
                if(resultSet == null || !resultSet.next())
                {
                    throw new SQLException("Could not get generated keys for insert");
                }

                Long id = resultSet.getLong(1);
                this.id = id;
            }
            finally
            {
                DatabaseUtil.closeSilently(connection);
                DatabaseUtil.closeSilently(statement);
                DatabaseUtil.closeSilently(resultSet);
            }
        }
        else
        {
            Connection connection = null;
            PreparedStatement statement = null;

            try
            {
                connection = DatabaseUtil.getConnection();
                String sql = "UPDATE voicemail SET subscriber_id = ?, file_location = ? WHERE id = ?";
                statement = connection.prepareStatement(sql);

                statement.setLong(1, this.subscriber.getId());
                statement.setString(2, this.fileLocation);
                statement.setLong(3, this.id);
            }
            finally
            {
                DatabaseUtil.closeSilently(connection);
                DatabaseUtil.closeSilently(statement);
            }
        }
    }

    private static Voicemail fromResultSet(ResultSet resultSet) throws SQLException
    {
        Voicemail voicemail = new Voicemail();
        voicemail.setId(resultSet.getLong("id"));

        Subscriber subscriber = Subscriber.get(resultSet.getLong("subscriber_id"));
        voicemail.setSubscriber(subscriber);

        voicemail.setFileLocation(resultSet.getString("file_location"));

        return voicemail;
    }
}
