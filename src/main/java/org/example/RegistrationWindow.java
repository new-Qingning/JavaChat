// src/main/java/org/example/RegistrationWindow.java
package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegistrationWindow extends JFrame {
    public RegistrationWindow() {
        setTitle("Register");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create components
        JLabel userLabel = new JLabel("Username:");
        JTextField userText = new JTextField(20);
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordText = new JPasswordField(20);
        JButton registerButton = new JButton("Register");

        // Set layout manager
        setLayout(new GridLayout(3, 2));

        // Add components to the frame
        add(userLabel);
        add(userText);
        add(passwordLabel);
        add(passwordText);
        add(new JLabel()); // empty cell
        add(registerButton);

        // Add action listener to the register button
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = userText.getText();
                String password = new String(passwordText.getPassword());
                // Perform registration logic here
                if (register(username, password)) {
                    JOptionPane.showMessageDialog(null, "Registration successful");
                } else {
                    JOptionPane.showMessageDialog(null, "Registration failed");
                }
            }
        });

        setVisible(true);
    }

    private boolean register(String username, String password) {
        // Replace with actual registration logic
        // For example, save the user data to a database
        return !username.isEmpty() && !password.isEmpty();
    }

    public static void main(String[] args) {
        new RegistrationWindow();
    }
}