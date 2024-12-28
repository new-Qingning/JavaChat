package org.example;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.List; // 添加这个导入

public class GroupChatWindow extends JFrame {
    private JTextArea chatArea;
    private JTextArea inputArea;
    private JButton sendButton;
    private PrintWriter writer;
    private String username;
    private JCheckBox anonymousCheckBox; // 新增匿名选项

    public GroupChatWindow(Socket socket, String username) {
        this.username = username;
        setTitle("群聊室 - " + username);
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // 创建底部面板
        JPanel bottomPanel = new JPanel(new BorderLayout());

        // 创建输入区域面板
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputArea = new JTextArea(3, 20);
        inputArea.setLineWrap(true);
        inputPanel.add(new JScrollPane(inputArea), BorderLayout.CENTER);

        // 创建右侧按钮面板
        JPanel rightPanel = new JPanel(new BorderLayout());
        sendButton = new JButton("发送");
        anonymousCheckBox = new JCheckBox("匿名发送");
        rightPanel.add(anonymousCheckBox, BorderLayout.NORTH);
        rightPanel.add(sendButton, BorderLayout.CENTER);

        inputPanel.add(rightPanel, BorderLayout.EAST);
        bottomPanel.add(inputPanel);

        add(bottomPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());

        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
            String displayName = anonymousCheckBox.isSelected() ? "匿名用户" : username;
            writer.println("group " + displayName + " " + message);
            // 修复消息显示格式
            chatArea.append(anonymousCheckBox.isSelected() ? "我(匿名): " + message + "\n" : "我: " + message + "\n");
            ChatHistory.saveGroupMessage(displayName, message);
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
