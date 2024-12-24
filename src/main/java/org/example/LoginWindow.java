package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class LoginWindow extends JFrame {
    private JTextField idText;
    private JPasswordField passwordText;
    private Socket socket;

    public LoginWindow(Socket socket) {
        this.socket = socket;
        setTitle("Login");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create components
        JLabel idLabel = new JLabel("ID:");
        idText = new JTextField(20);
        JLabel passwordLabel = new JLabel("Password:");
        passwordText = new JPasswordField(20);
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        // Set layout manager
        setLayout(new GridLayout(3, 2));

        // Add components to the frame
        add(idLabel);
        add(idText);
        add(passwordLabel);
        add(passwordText);
        add(registerButton);
        add(loginButton);

        // Add action listener to the login button
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int id = Integer.parseInt(idText.getText());
                String password = new String(passwordText.getPassword());
                try (PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    writer.println("login " + id + " " + password);
                    String response = reader.readLine();
                    if ("Login successful".equals(response)) {
                        JOptionPane.showMessageDialog(null, "Login successful");
                        List<String> users = new ArrayList<>();
                        String line;
                        while (!(line = reader.readLine()).equals("end_of_list")) {
                            if (line.startsWith("user ")) {
                                users.add(line.substring(5));
                            }
                        }
                        SwingUtilities.invokeLater(() -> new UserListWindow(socket, idText.getText(), users)); // Pass the username and user list
                        dispose(); // Close the login window
                    } else {
                        JOptionPane.showMessageDialog(null, "Login failed");
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Add action listener to the register button
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(() -> new RegisterWindow(socket));
                dispose(); // Close the login window
            }
        });

        setVisible(true);
    }
}