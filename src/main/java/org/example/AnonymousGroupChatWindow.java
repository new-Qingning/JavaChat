package org.example;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Random;

public class AnonymousGroupChatWindow extends JFrame {
    private JTextArea chatArea;
    private JTextArea inputArea;
    private JButton sendButton;
    private PrintWriter writer;
    private String anonymousId;

    public AnonymousGroupChatWindow(Socket socket) {
        // 生成随机匿名ID
        this.anonymousId = "匿名用户" + new Random().nextInt(10000);
        setTitle("匿名群聊 - " + anonymousId);
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        inputArea = new JTextArea(3, 20);
        inputArea.setLineWrap(true);

        sendButton = new JButton("发送");
        sendButton.addActionListener(e -> sendMessage());

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(new JScrollPane(inputArea), BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        add(inputPanel, BorderLayout.SOUTH);

        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
            // 发送匿名群聊加入消息
            writer.println("anonymous_join " + anonymousId);
        } catch (IOException e) {
            e.printStackTrace();
        }

        setVisible(true);
    }

    private void sendMessage() {
        String message = inputArea.getText().trim();
        if (!message.isEmpty()) {
            writer.println("anonymous " + anonymousId + " " + message);
            chatArea.append("我: " + message + "\n");
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
