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
    private JTextField usernameField;
    private Socket socket;

    public LoginWindow(Socket socket) {
        this.socket = socket;
        setTitle("登录");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create components
        JLabel idLabel = new JLabel("ID:");
        idText = new JTextField(20);
        JLabel passwordLabel = new JLabel("Password:");
        passwordText = new JPasswordField(20);
        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(15);
        JButton loginButton = new JButton("登录");
        JButton registerButton = new JButton("注册");

        // Set layout manager
        setLayout(new GridLayout(4, 2));

        // Add components to the frame
        add(idLabel);
        add(idText);
        add(passwordLabel);
        add(passwordText);
        add(usernameLabel);
        add(usernameField);
        add(registerButton);
        add(loginButton);

        // Add action listener to the login button
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
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

    private void handleLogin() {
        try (PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            int id = Integer.parseInt(idText.getText());
            String password = new String(passwordText.getPassword());
            String username = usernameField.getText().trim();

            writer.println("login " + id + " " + password);
            String response = reader.readLine();

            if ("登录成功".equals(response)) {
                String actualUsername = Database.getUsernameById(id);
                List<String> users = new ArrayList<>();
                String line;
                while (!(line = reader.readLine()).equals("end_of_list")) {
                    if (line.startsWith("user ")) {
                        users.add(line.substring(5));
                    }
                }
                SwingUtilities.invokeLater(() -> new UserListWindow(socket, actualUsername, users));
                dispose();
            } else {
                JOptionPane.showMessageDialog(null, "登录失败");
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "连接错误: " + ex.getMessage());
        }
    }
}