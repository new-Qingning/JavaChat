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
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 应用全局UI样式
        UIStyle.setupGlobalUI();
        UIStyle.decorateFrame(this);

        // 创建主面板
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();

        // 创建带样式的组件
        JLabel titleLabel = new JLabel("用户登录", SwingConstants.CENTER);
        titleLabel.setFont(UIStyle.TITLE_FONT);
        titleLabel.setForeground(UIStyle.PRIMARY_COLOR);

        // 修改这部分 - 使用类成员变量而不是局部变量
        idText = new JTextField(15);
        passwordText = new JPasswordField(15);
        UIStyle.decorateTextComponent(idText);
        UIStyle.decorateTextComponent(passwordText);

        JButton loginButton = UIStyle.createStyledButton("登录");
        JButton registerButton = UIStyle.createStyledButton("注册");

        // 布局组件
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(idText, gbc); // 使用idText而不是idField

        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("密码:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(passwordText, gbc); // 使用passwordText而不是passwordField

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(buttonPanel, gbc);

        add(mainPanel);

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