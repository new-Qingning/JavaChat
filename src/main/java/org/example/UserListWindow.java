package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.Socket;
import java.util.List;

public class UserListWindow extends JFrame {
    private JList<String> userList;
    private Socket socket;

    public UserListWindow(Socket socket) {
        this.socket = socket;
        setTitle("User List");
        setSize(300, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Fetch users from the server
        List<User> users = Database.getUsers();
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (User user : users) {
            listModel.addElement(user.getUsername());
        }

        userList = new JList<>(listModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String selectedUsername = userList.getSelectedValue();
                    SwingUtilities.invokeLater(() -> new ChatWindow(socket, selectedUsername));
                }
            }
        });

        add(new JScrollPane(userList), BorderLayout.CENTER);
        setVisible(true);
    }
}