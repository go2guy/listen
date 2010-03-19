package com.interact.listen;

import com.interact.listen.db.DatabaseUtil;

import java.sql.*;

public class Bootstrap
{
    public static void init()
    {
        Connection connection = null;
        Statement statement = null;
        PreparedStatement preparedStatement = null;

        try
        {
            connection = DatabaseUtil.getConnection();

            statement = connection.createStatement();
            statement.executeUpdate("DROP TABLE IF EXISTS subscriber");
            statement.executeUpdate("CREATE TABLE subscriber (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, number VARCHAR(11) UNIQUE NOT NULL)");

            preparedStatement = connection.prepareStatement("INSERT INTO subscriber (number) values (?)");

            preparedStatement.setString(1, "14025604557");
            preparedStatement.addBatch();

            preparedStatement.setString(1, "14024768786");
            preparedStatement.addBatch();

            preparedStatement.setString(1, "18002428649");
            preparedStatement.addBatch();

            connection.setAutoCommit(false);
            preparedStatement.executeBatch();
            connection.setAutoCommit(true);
        }
        catch(SQLException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Could not create subscriber table");
        }
        finally
        {
            DatabaseUtil.closeSilently(connection);
            DatabaseUtil.closeSilently(statement);
            DatabaseUtil.closeSilently(preparedStatement);
        }

        // voicemail
        try
        {
            connection = DatabaseUtil.getConnection();

            statement = connection.createStatement();
            statement.executeUpdate("DROP TABLE IF EXISTS voicemail");
            statement.executeUpdate("CREATE TABLE voicemail (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, subscriber_id BIGINT NOT NULL, file_location VARCHAR(255) NOT NULL UNIQUE)");

            preparedStatement =connection.prepareStatement("INSERT INTO voicemail (subscriber_id, file_location) VALUES (?, ?)");

            preparedStatement.setLong(1, 1);
            preparedStatement.setString(2, "/var/listen/s1v1.wav");
            preparedStatement.addBatch();

            preparedStatement.setLong(1, 1);
            preparedStatement.setString(2, "/var/listen/s1v2.wav");
            preparedStatement.addBatch();

            preparedStatement.setLong(1, 2);
            preparedStatement.setString(2, "/var/listen/s2v1.wav");
            preparedStatement.addBatch();

            preparedStatement.setLong(1, 3);
            preparedStatement.setString(2, "/var/listen/s3v1.wav");
            preparedStatement.addBatch();

            preparedStatement.setLong(1, 2);
            preparedStatement.setString(2, "/var/listen/s3v2.wav");
            preparedStatement.addBatch();

            preparedStatement.setLong(1, 3);
            preparedStatement.setString(2, "/var/listen/s3v3.wav");
            preparedStatement.addBatch();

            preparedStatement.setLong(1, 3);
            preparedStatement.setString(2, "/var/listen/s3v4.wav");
            preparedStatement.addBatch();

            preparedStatement.setLong(1, 3);
            preparedStatement.setString(2, "/var/listen/s3v5.wav");
            preparedStatement.addBatch();

            connection.setAutoCommit(false);
            preparedStatement.executeBatch();
            connection.setAutoCommit(true);
        }
        catch(SQLException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Could not create voicemail table");
        }
        finally
        {
            DatabaseUtil.closeSilently(connection);
            DatabaseUtil.closeSilently(statement);
            DatabaseUtil.closeSilently(preparedStatement);
        }
    }
}
