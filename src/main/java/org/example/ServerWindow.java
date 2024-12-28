package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerWindow extends JFrame {
    private static final int PORT = 8421;
    private ExecutorService threadPool;
    private DefaultTableModel tableModel; // 移回类级别以便其他方法访问

    public ServerWindow() {
        setTitle("聊天服务器");
        IconLoader.setWindowIcon(this);
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create components
        JLabel ipLabel = new JLabel("服务器IP地址: " + getIpAddress());
        String[] columnNames = { "ID", "Username", "Password" };
        tableModel = new DefaultTableModel(columnNames, 0); // 初始化tableModel
        JTable userTable = new JTable(tableModel);

        // Set layout manager
        setLayout(new BorderLayout());

        // Add components to the frame
        add(ipLabel, BorderLayout.NORTH);
        add(new JScrollPane(userTable), BorderLayout.CENTER);

        // Load users from the database
        loadUsers();

        setVisible(true);

        // Initialize thread pool
        threadPool = Executors.newCachedThreadPool();

        // Start server socket
        new Thread(this::startServer).start();

        // 初始化数据库表
        Database.initializeTables();
    }

    private String getIpAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (networkInterface.isUp() && !networkInterface.isLoopback()
                        && networkInterface.getDisplayName().contains("Wi-Fi")) {
                    Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress inetAddress = inetAddresses.nextElement();
                        if (inetAddress instanceof Inet4Address) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
            return "未找到WLAN IP地址";
        } catch (SocketException e) {
            e.printStackTrace();
            return "未知";
        }
    }

    private void loadUsers() {
        List<User> users = Database.getUsers();
        for (User user : users) {
            tableModel.addRow(new Object[] { user.getId(), user.getUsername(), user.getPassword() });
        }
    }

    private void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                threadPool.execute(new ClientHandler(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ServerWindow();
    }
}