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
        setTitle("注册");
        IconLoader.setWindowIcon(this); // 添加图标设置
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        IconLoader.setWindowIcon(this);

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
                handleRegister();
            }
        });

        setVisible(true);
    }

    private void handleRegister() {
        try {
            int id = Integer.parseInt(idText.getText().trim());
            String username = usernameText.getText().trim();
            String password = new String(passwordText.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "用户名和密码不能为空");
                return;
            }

            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            writer.println("register " + id + " " + username + " " + password);
            String response = reader.readLine();

            if (response.startsWith("注册成功")) {
                JOptionPane.showMessageDialog(this, "注册成功！");
                SwingUtilities.invokeLater(() -> new LoginWindow(socket));
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, response);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效的数字ID");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "连接错误: " + e.getMessage());
        }
    }
}