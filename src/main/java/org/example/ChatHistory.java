package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChatHistory {
    // 获取私聊历史记录
    public static List<String> getPrivateHistory(String sender, String receiver) {
        List<String> history = new ArrayList<>();
        String query = "SELECT Sender, Message, Timestamp FROM PrivateMessages " +
                "WHERE (Sender = ? AND Receiver = ?) OR (Sender = ? AND Receiver = ?) " +
                "ORDER BY Timestamp";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, sender);
            stmt.setString(2, receiver);
            stmt.setString(3, receiver);
            stmt.setString(4, sender);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String msgSender = rs.getString("sender");
                String message = rs.getString("message");
                Timestamp timestamp = rs.getTimestamp("timestamp");
                String formattedMsg = String.format("[%s] %s: %s",
                        timestamp.toString(),
                        msgSender.equals(sender) ? "Me" : msgSender,
                        message);
                history.add(formattedMsg);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    // 获取群聊历史记录
    public static List<String> getGroupHistory() {
        List<String> history = new ArrayList<>();
        String query = "SELECT Sender, Message, Timestamp FROM GroupMessages ORDER BY Timestamp";

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String sender = rs.getString("sender");
                String message = rs.getString("message");
                Timestamp timestamp = rs.getTimestamp("timestamp");
                history.add(String.format("[%s] %s: %s",
                        timestamp.toString(), sender, message));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    // 保存私聊消息
    public static void savePrivateMessage(String sender, String receiver, String message) {
        String query = "INSERT INTO PrivateMessages (Sender, Receiver, Message, Timestamp) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, sender);
            stmt.setString(2, receiver);
            stmt.setString(3, message);
            stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 保存群聊消息
    public static void saveGroupMessage(String sender, String message) {
        String query = "INSERT INTO GroupMessages (Sender, Message, Timestamp) VALUES (?, ?, ?)";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, sender);
            stmt.setString(2, message);
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
