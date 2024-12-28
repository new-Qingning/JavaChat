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
        setSize(300, 150);
        IconLoader.setWindowIcon(this);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create components
        JLabel ipLabel = new JLabel("服务器地址:");
        ipText = new JTextField(20);
        JLabel portLabel = new JLabel("端口:");
        portText = new JTextField(20);
        JButton connectButton = new JButton("连接");

        // Set layout manager
        setLayout(new GridLayout(3, 2));

        // Add components to the frame
        add(ipLabel);
        add(ipText);
        add(portLabel);
        add(portText);
        add(new JLabel()); // Empty cell
        add(connectButton);

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