package me.rukon0621.guardians.helper;

import me.rukon0621.guardians.main;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DataBase {

    private final Connection connection;

    public DataBase(String databaseName) {
        connection = DBStatic.getConnection(databaseName);
    }

    public DataBase() {
        connection = DBStatic.getConnection(main.DB_NAME);
    }

    public Connection getConnection() {
        return connection;
    }

    public void executeClose(String sql) {
        execute(sql);
    }

    private Statement statement;
    private ResultSet resultSet;

    public ResultSet executeQuery(String sql) {
        close();
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            return resultSet;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void execute(String sql) {
        close();
        try {
            statement = connection.createStatement();
            statement.execute(sql);
            statement.close();
        } catch (SQLException ignored) {
        }
    }

    public void close() {
        try {
            if (statement != null) statement.close();
            if (resultSet != null) resultSet.close();
            statement = null;
            resultSet = null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
