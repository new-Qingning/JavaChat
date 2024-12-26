package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=User;encrypt=false";
    private static final String DB_USER = "adminjava";
    private static final String DB_PASSWORD = "1234";

    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static boolean validateUser(int id, String password) {
        String query = "SELECT * FROM users WHERE id = ? AND password = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean registerUser(int id, String username, String password) {
        String query = "INSERT INTO users (id, username, password) VALUES (?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.setString(2, username);
            statement.setString(3, password);
            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<User> getUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT id, username, password FROM users";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");
                users.add(new User(id, username, password));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public static String getUsernameById(int id) {
        String query = "SELECT username FROM users WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // 初始化数据库表
    public static void initializeTables() {
        try (Connection conn = getConnection()) {
            // 创建私聊消息表
            String createPrivateMessagesTable = "IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[PrivateMessages]') AND type in (N'U'))\n"
                    +
                    "BEGIN\n" +
                    "CREATE TABLE [dbo].[PrivateMessages] (\n" +
                    "    [Id] BIGINT IDENTITY(1,1) PRIMARY KEY,\n" +
                    "    [Sender] VARCHAR(50),\n" +
                    "    [Receiver] VARCHAR(50),\n" +
                    "    [Message] NVARCHAR(MAX),\n" +
                    "    [Timestamp] DATETIME DEFAULT GETDATE()\n" +
                    ")\n" +
                    "END";

            // 创建群聊消息表
            String createGroupMessagesTable = "IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[GroupMessages]') AND type in (N'U'))\n"
                    +
                    "BEGIN\n" +
                    "CREATE TABLE [dbo].[GroupMessages] (\n" +
                    "    [Id] BIGINT IDENTITY(1,1) PRIMARY KEY,\n" +
                    "    [Sender] VARCHAR(50),\n" +
                    "    [Message] NVARCHAR(MAX),\n" +
                    "    [Timestamp] DATETIME DEFAULT GETDATE()\n" +
                    ")\n" +
                    "END";

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createPrivateMessagesTable);
                stmt.execute(createGroupMessagesTable);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}