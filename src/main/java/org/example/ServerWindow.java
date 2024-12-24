package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.List;

public class ServerWindow extends JFrame {
    private DefaultTableModel tableModel;
    private static final int PORT = 8421;

    public ServerWindow() {
        setTitle("Server");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create components
        JLabel ipLabel = new JLabel("IP Address: " + getIpAddress());
        String[] columnNames = {"ID", "Username", "Password"};
        tableModel = new DefaultTableModel(columnNames, 0);
        JTable userTable = new JTable(tableModel);

        // Set layout manager
        setLayout(new BorderLayout());

        // Add components to the frame
        add(ipLabel, BorderLayout.NORTH);
        add(new JScrollPane(userTable), BorderLayout.CENTER);

        // Load users from the database
        loadUsers();

        setVisible(true);

        // Start server socket
        new Thread(this::startServer).start();
    }

    private String getIpAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (networkInterface.isUp() && !networkInterface.isLoopback() && networkInterface.getDisplayName().contains("Wi-Fi")) {
                    Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress inetAddress = inetAddresses.nextElement();
                        if (inetAddress instanceof Inet4Address) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
            return "No WLAN IP found";
        } catch (SocketException e) {
            e.printStackTrace();
            return "Unknown";
        }
    }

    private void loadUsers() {
        List<User> users = Database.getUsers();
        for (User user : users) {
            tableModel.addRow(new Object[]{user.getId(), user.getUsername(), user.getPassword()});
        }
    }

    private void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ServerWindow();
    }
}