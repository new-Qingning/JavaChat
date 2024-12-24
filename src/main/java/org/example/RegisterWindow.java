package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class RegisterWindow extends JFrame {
    private JTextField idText;
    private JTextField usernameText;
    private JPasswordField passwordText;
    private Socket socket;

    public RegisterWindow(Socket socket) {
        this.socket = socket;
        setTitle("Register");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create components
        JLabel idLabel = new JLabel("ID:");
        idText = new JTextField(20);
        JLabel usernameLabel = new JLabel("Username:");
        usernameText = new JTextField(20);
        JLabel passwordLabel = new JLabel("Password:");
        passwordText = new JPasswordField(20);
        JButton registerButton = new JButton("Register");

        // Set layout manager
        setLayout(new GridLayout(4, 2));

        // Add components to the frame
        add(idLabel);
        add(idText);
        add(usernameLabel);
        add(usernameText);
        add(passwordLabel);
        add(passwordText);
        add(new JLabel()); // Empty cell
        add(registerButton);

        // Add action listener to the register button
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int id = Integer.parseInt(idText.getText());
                String username = usernameText.getText();
                String password = new String(passwordText.getPassword());
                try (PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    writer.println("register " + id + " " + username + " " + password);
                    String response = reader.readLine();
                    if ("Register successful".equals(response)) {
                        JOptionPane.showMessageDialog(null, "Register successful");
                        SwingUtilities.invokeLater(() -> new LoginWindow(socket));
                        dispose(); // Close the register window
                    } else {
                        JOptionPane.showMessageDialog(null, "Register failed");
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        setVisible(true);
    }
}