package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List; // 添加这个导入

public class ChatWindow extends JFrame {
    private JTextArea chatArea;
    private JTextArea inputArea;
    private JButton sendButton;
    private PrintWriter writer;
    private String sourceUser;
    private String targetUser;
    private Socket socket;

    public ChatWindow(Socket socket, String sourceUser, String targetUser) {
        this.socket = socket;
        this.sourceUser = sourceUser;
        this.targetUser = targetUser;
        setTitle("与 " + targetUser + " 聊天中");
        IconLoader.setWindowIcon(this);
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // 应用全局UI样式
        UIStyle.setupGlobalUI();
        UIStyle.decorateFrame(this);

        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(UIStyle.BACKGROUND_COLOR);

        // 聊天区域
        chatArea = new JTextArea();
        UIStyle.decorateTextComponent(chatArea);
        chatArea.setEditable(false);
        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setBorder(UIStyle.createRoundedBorder());

        // 输入区域
        inputArea = new JTextArea(3, 20);
        UIStyle.decorateTextComponent(inputArea);
        JScrollPane inputScroll = new JScrollPane(inputArea);

        // 发送按钮
        sendButton = UIStyle.createStyledButton("发送");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        // 底部面板
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        bottomPanel.add(inputScroll, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        mainPanel.add(chatScroll, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        try {
            // 重用现有的socket连接
            this.writer = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("[ChatWindow] 使用现有连接");
        } catch (IOException e) {
            System.err.println("[ChatWindow] 输出流创建失败: " + e.getMessage());
            e.printStackTrace();
        }

        // 加载历史记录
        loadChatHistory();
        setVisible(true);
    }

    private void loadChatHistory() {
        List<String> history = ChatHistory.getPrivateHistory(sourceUser, targetUser);
        for (String msg : history) {
            chatArea.append(msg + "\n");
        }
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    public void receiveMessage(String message) {
        System.out.println("[ChatWindow] 收到消息: " + message);

        // 确保只处理发给当前聊天窗口的消息
        if (message.startsWith("private_msg")) {
            String[] parts = message.split(" ", 3);
            if (parts.length == 3) {
                String sender = parts[1];
                // 只处理来自目标用户的消息
                if (sender.equals(targetUser)) {
                    SwingUtilities.invokeLater(() -> {
                        String timestamp = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
                        chatArea.append(String.format("[%s] %s -> 我: %s\n", timestamp, sender, parts[2]));
                        chatArea.setCaretPosition(chatArea.getDocument().getLength());
                    });
                }
            }
        }
    }

    private void sendMessage() {
        String msg = inputArea.getText().trim();
        if (!msg.isEmpty()) {
            String command = "private " + sourceUser + " " + targetUser + " " + msg;
            System.out.println("[ChatWindow] 发送消息: " + command);
            writer.println(command);
            writer.flush();
            chatArea.append("我 -> " + targetUser + ": " + msg + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
            ChatHistory.savePrivateMessage(sourceUser, targetUser, msg);
            inputArea.setText("");
        }
    }
}