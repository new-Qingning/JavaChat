package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.Socket;
import java.util.List;

public class UserListWindow extends JFrame {
    private JList<String> userList;
    private Socket socket;
    private String currentUsername;

    public UserListWindow(Socket socket, String currentUsername, List<String> users) {
        this.socket = socket;
        this.currentUsername = currentUsername;
        setTitle("在线用户 - 当前用户: " + currentUsername);
        IconLoader.setWindowIcon(this);
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (String user : users) {
            listModel.addElement(user);
        }

        userList = new JList<>(listModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.addMouseListener(new MouseListener());

        add(new JScrollPane(userList), BorderLayout.CENTER);

        // 创建按钮面板，只保留群聊按钮
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton groupChatButton = new JButton("加入群聊");

        groupChatButton.addActionListener(e -> openGroupChat());

        buttonPanel.add(groupChatButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void openChatWindow(String selectedUsername) {
        try {
            // 使用现有的socket连接，而不是创建新的
            ChatWindow chatWindow = new ChatWindow(socket, currentUsername, selectedUsername);
            System.out.println("[Client] 开始监听消息");
            startMessageListener(socket, chatWindow);
            System.out.println("[Client] 聊天窗口已创建");

            // 发送认证消息
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println("auth " + currentUsername);

        } catch (IOException ex) {
            System.err.println("[Client] 连接错误: " + ex.getMessage());
            JOptionPane.showMessageDialog(null, "连接错误: " + ex.getMessage());
        }
    }

    private void openGroupChat() {
        try {
            Socket groupChatSocket = new Socket(socket.getInetAddress(), socket.getPort());
            PrintWriter writer = new PrintWriter(groupChatSocket.getOutputStream(), true);

            // 发送群聊认证
            writer.println("auth " + currentUsername);

            GroupChatWindow groupChatWindow = new GroupChatWindow(groupChatSocket, currentUsername);
            startGroupMessageListener(groupChatSocket, groupChatWindow);

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error joining group chat: " + ex.getMessage());
        }
    }

    private void startGroupMessageListener(Socket socket, GroupChatWindow window) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String message;
                while ((message = reader.readLine()) != null) {
                    window.receiveMessage(message);
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(window, "群聊连接已断开");
                    window.dispose();
                });
            }
        }).start();
    }

    private void startMessageListener(Socket socket, ChatWindow chatWindow) {
        new Thread(() -> {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                System.out.println("[MessageListener] 开始监听消息");
                String message;

                // 保持监听状态
                while ((message = reader.readLine()) != null) {
                    final String msg = message;
                    System.out.println("[MessageListener] 收到消息: " + msg);

                    // 将所有消息转发到聊天窗口
                    SwingUtilities.invokeLater(() -> {
                        if (msg.startsWith("private_msg") || msg.startsWith("error")) {
                            chatWindow.receiveMessage(msg);
                        }
                    });
                }
            } catch (IOException e) {
                System.err.println("[MessageListener] 异常: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    class MouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                String selectedUsername = userList.getSelectedValue();
                if (selectedUsername != null && !selectedUsername.equals(currentUsername)) {
                    openChatWindow(selectedUsername);
                }
            }
        }
    }
}