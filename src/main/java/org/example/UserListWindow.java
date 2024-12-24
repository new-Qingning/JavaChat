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

    public UserListWindow(Socket socket, String currentUsername, List<String> users) {
        this.socket = socket;
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
        userList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String selectedUsername = userList.getSelectedValue();
                    try {
                        Socket chatSocket = new Socket(socket.getInetAddress(), socket.getPort());
                        SwingUtilities.invokeLater(() -> new ChatWindow(chatSocket, selectedUsername));
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, "Error initializing chat: " + ex.getMessage());
                    }
                }
            }
        });

        add(new JScrollPane(userList), BorderLayout.CENTER);
        setVisible(true);
    }
}