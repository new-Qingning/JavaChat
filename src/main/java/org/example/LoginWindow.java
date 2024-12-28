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
        IconLoader.setWindowIcon(this);
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create components
        JLabel idLabel = new JLabel("账号:");
        idText = new JTextField(20);
        JLabel usernameLabel = new JLabel("用户名:");
        usernameField = new JTextField(15);
        JLabel passwordLabel = new JLabel("密码:");
        passwordText = new JPasswordField(20);
        JButton loginButton = new JButton("登录");
        JButton registerButton = new JButton("注册");

        // Set layout manager
        setLayout(new GridLayout(4, 2));

        // Add components to the frame
        add(idLabel);
        add(idText);
        add(usernameLabel);
        add(usernameField);
        add(passwordLabel);
        add(passwordText);
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
        try {
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            int id = Integer.parseInt(idText.getText());
            String password = new String(passwordText.getPassword());
            System.out.println("尝试登录: ID=" + id); // 添加调试日志

            writer.println("login " + id + " " + password);
            String response = reader.readLine();
            System.out.println("服务器响应: " + response); // 添加调试日志

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
                JOptionPane.showMessageDialog(null, "登录失败：" + response);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "连接错误: " + ex.getMessage());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "请输入有效的ID");
        }
    }
}