package org.example;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.List;

public class GroupChatWindow extends JFrame {
    private JTextArea chatArea;
    private JTextArea inputArea;
    private JButton sendButton;
    private PrintWriter writer;
    private String username;

    public GroupChatWindow(Socket socket, String username) {
        this.username = username;
        setTitle("群聊 - " + username);
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        inputArea = new JTextArea(3, 20);
        inputArea.setLineWrap(true);

        sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(new JScrollPane(inputArea), BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        add(inputPanel, BorderLayout.SOUTH);

        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 加载群聊历史记录
        loadGroupHistory();
        setVisible(true);
    }

    private void loadGroupHistory() {
        List<String> history = ChatHistory.getGroupHistory();
        for (String msg : history) {
            chatArea.append(msg + "\n");
        }
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    private void sendMessage() {
        String message = inputArea.getText().trim();
        if (!message.isEmpty()) {
            writer.println("group " + username + " " + message);
            chatArea.append("Me: " + message + "\n");
            // 保存群聊消息到数据库
            ChatHistory.saveGroupMessage(username, message);
            inputArea.setText("");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        }
    }

    public void receiveMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }
}
