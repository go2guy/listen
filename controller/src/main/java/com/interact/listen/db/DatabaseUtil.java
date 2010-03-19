package com.interact.listen.db;

import java.sql.*;

public class DatabaseUtil
{
    public static Connection getConnection() throws SQLException
    {
        try
        {
            Class.forName("org.sqlite.JDBC");
        }
        catch(ClassNotFoundException e)
        {
            throw new AssertionError("Could not load JDBC driver");
        }

        return DriverManager.getConnection("jdbc:sqlite:mydatabase");
    }

    public static void closeSilently(Connection connection)
    {
        if(connection == null)
        {
            return;
        }

        try
        {
            connection.close();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
    }

    public static void closeSilently(ResultSet resultSet)
    {
        if(resultSet == null)
        {
            return;
        }

        try
        {
            resultSet.close();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
    }

    public static void closeSilently(Statement statement)
    {
        if(statement == null)
        {
            return;
        }

        try
        {
            statement.close();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
    }
}
