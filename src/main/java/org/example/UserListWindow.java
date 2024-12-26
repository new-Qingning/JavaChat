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

        // 添加群聊按钮
        JButton groupChatButton = new JButton("加入群聊");
        groupChatButton.addActionListener(e -> openGroupChat());
        add(groupChatButton, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void openChatWindow(String selectedUsername) {
        try {
            Socket chatSocket = new Socket(socket.getInetAddress(), socket.getPort());
            PrintWriter writer = new PrintWriter(chatSocket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(chatSocket.getInputStream()));

            // 发送身份验证
            writer.println("auth " + currentUsername);
            String response = reader.readLine();

            if ("auth_success".equals(response)) { // 修改这里，确保与服务器返回的消息匹配
                ChatWindow chatWindow = new ChatWindow(chatSocket, currentUsername, selectedUsername);
                startMessageListener(chatSocket, chatWindow);
            } else {
                chatSocket.close();
                JOptionPane.showMessageDialog(this, "身份验证失败");
            }
        } catch (IOException ex) {
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
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String message;
                while ((message = reader.readLine()) != null) {
                    final String msg = message;
                    SwingUtilities.invokeLater(() -> chatWindow.receiveMessage(msg));
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(chatWindow, "连接已断开"));
                chatWindow.dispose();
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