package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
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
    private static DefaultTableModel tableModel; // 改为静态字段
    private static ImageIcon onlineIcon;
    private static ImageIcon offlineIcon;

    public ServerWindow() {
        setTitle("聊天服务器");
        IconLoader.setWindowIcon(this);
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 加载状态图标
        onlineIcon = loadIcon("images/online.png");
        offlineIcon = loadIcon("images/offline.png");

        // Create components
        JLabel ipLabel = new JLabel("服务器IP地址: " + getIpAddress());
        String[] columnNames = { "ID", "Username", "Password", "Status" };
        tableModel = new DefaultTableModel(columnNames, 0); // 初始化tableModel
        JTable userTable = new JTable(tableModel);
        // 设置状态列的渲染器
        userTable.getColumnModel().getColumn(3).setCellRenderer(new StatusColumnRenderer());

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

    private ImageIcon loadIcon(String path) {
        try {
            URL iconURL = getClass().getClassLoader().getResource(path);
            if (iconURL != null) {
                ImageIcon icon = new ImageIcon(iconURL);
                // 缩放图标到合适大小
                return new ImageIcon(icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
            tableModel.addRow(new Object[] {
                    user.getId(),
                    user.getUsername(),
                    user.getPassword(),
                    "offline" // 使用字符串标记状态
            });
        }
    }

    // 添加新方法用于更新用户状态
    public static void updateUserStatus(String username, boolean online) {
        // 现在可以直接访问静态字段 tableModel
        if (tableModel != null) { // 添加空值检查
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (tableModel.getValueAt(i, 1).equals(username)) {
                    tableModel.setValueAt(online ? "online" : "offline", i, 3);
                    break;
                }
            }
        }
    }

    // 状态列的自定义渲染器
    private class StatusColumnRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, "", isSelected, hasFocus, row, column);

            if (value != null) {
                if ("online".equals(value)) {
                    label.setIcon(onlineIcon);
                } else {
                    label.setIcon(offlineIcon);
                }
            }
            label.setHorizontalAlignment(JLabel.CENTER);
            return label;
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