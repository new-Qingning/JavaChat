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
        setTitle("User List - Logged in as: " + currentUsername);
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

            if ("auth_success".equals(response)) {
                ChatWindow chatWindow = new ChatWindow(chatSocket, currentUsername, selectedUsername);
                startMessageListener(chatSocket, chatWindow);
            } else {
                chatSocket.close();
                JOptionPane.showMessageDialog(this, "Authentication failed");
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Connection error: " + ex.getMessage());
        }
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
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(chatWindow, "Connection lost"));
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