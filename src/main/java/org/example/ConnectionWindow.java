package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;

public class ConnectionWindow extends JFrame {
    private JTextField ipText;
    private JTextField portText;

    public ConnectionWindow() {
        setTitle("连接到服务器");
        IconLoader.setWindowIcon(this);
        setSize(500, 300); // 增加窗口大小
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
        JLabel titleLabel = new JLabel("服务器连接", SwingConstants.CENTER);
        titleLabel.setFont(UIStyle.TITLE_FONT);
        titleLabel.setForeground(UIStyle.PRIMARY_COLOR);

        ipText = new JTextField(20); // 增加文本框宽度
        portText = new JTextField("8421", 20);
        UIStyle.decorateTextComponent(ipText);
        UIStyle.decorateTextComponent(portText);

        // 设置文本框的首选大小
        Dimension textFieldSize = new Dimension(250, 30); // 增加文本框尺寸
        ipText.setPreferredSize(textFieldSize);
        portText.setPreferredSize(textFieldSize);

        JButton connectButton = UIStyle.createStyledButton("连接");

        // 布局组件
        gbc.insets = new Insets(15, 20, 15, 20); // 增加间距
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL; // 水平填充
        mainPanel.add(new JLabel("IP地址:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(ipText, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("端口:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(portText, gbc);

        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(connectButton, gbc);

        add(mainPanel);

        // Add action listener to the connect button
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String ip = ipText.getText();
                int port = Integer.parseInt(portText.getText());
                try {
                    Socket socket = new Socket(ip, port);
                    SwingUtilities.invokeLater(() -> new LoginWindow(socket));
                    dispose(); // Close the connection window
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "无法连接到服务器");
                }
            }
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ConnectionWindow::new);
    }
}