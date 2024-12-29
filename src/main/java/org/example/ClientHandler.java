package org.example;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientHandler implements Runnable {
    private static List<ClientHandler> clientHandlers = new CopyOnWriteArrayList<>();
    private Socket socket;
    private PrintWriter writer;
    private String username;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        clientHandlers.add(this);
    }

    @Override
    public void run() {
        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String message;

            System.out.println("[Server] 新客户端连接");

            while ((message = reader.readLine()) != null) {
                System.out.println("[Server] 收到消息: " + message);
                try {
                    if (message.startsWith("group ")) {
                        handleGroupMessage(message);
                    } else if (message.startsWith("auth ")) {
                        handleAuth(message);
                    } else if (message.startsWith("private ")) {
                        handlePrivateMessage(message);
                    } else if (message.startsWith("login")) {
                        handleLogin(message);
                    } else {
                        System.out.println("未知消息类型: " + message);
                    }
                } catch (Exception e) {
                    System.err.println("[Server] 处理消息异常: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("[Server] 连接异常: " + e.getMessage());
        } finally {
            handleDisconnection();
        }
    }

    private void handleAuth(String message) {
        String[] parts = message.split(" ", 2);
        if (parts.length == 2) {
            String requestedUsername = parts[1];
            // 验证用户名是否有效
            System.out.println("[Server] 处理认证请求: " + requestedUsername);
            if (isValidUsername(requestedUsername)) {
                this.username = requestedUsername;
                writer.println("认证成功");
                writer.flush(); // 确保立即发送
                System.out.println("[Server] 用户认证成功: " + username);
            } else {
                writer.println("认证失败");
                writer.flush();
                System.out.println("[Server] 用户认证失败: " + requestedUsername);
            }
        }
    }

    private boolean isValidUsername(String username) {
        // 检查是否是已登录的有效用户名
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        List<User> users = Database.getUsers();
        boolean isValid = users.stream()
                .anyMatch(user -> user.getUsername().equals(username));
        System.out.println("Username validation: " + username + " is " + (isValid ? "valid" : "invalid")); // 添加调试日志
        return isValid;
    }

    private void handleLogin(String message) {
        String[] parts = message.split(" ");
        if (parts.length == 3) {
            int id = Integer.parseInt(parts[1]);
            String password = parts[2];
            if (Database.validateUser(id, password)) {
                this.username = Database.getUsernameById(id);
                System.out.println("用户登录成功: " + username); // 添加调试日志
                writer.println("登录成功");
                ServerWindow.updateUserStatus(username, true); // 更新状态为在线
                sendUserList(writer);
            } else {
                System.out.println("用户登录失败: ID=" + id); // 添加调试日志
                writer.println("登录失败");
            }
        } else {
            writer.println("登录格式错误");
        }
    }

    private void handleRegister(String message) {
        String[] parts = message.split(" ");
        if (parts.length == 4) {
            try {
                int id = Integer.parseInt(parts[1]);
                String username = parts[2];
                String password = parts[3];

                System.out.println("处理注册请求: ID=" + id + ", 用户名=" + username); // 调试日志

                if (Database.registerUser(id, username, password)) {
                    writer.println("注册成功");
                    System.out.println("用户注册成功: " + username); // 调试日志
                } else {
                    writer.println("注册失败：用户ID或用户名已存在");
                    System.out.println("用户注册失败: " + username); // 调试日志
                }
            } catch (NumberFormatException e) {
                writer.println("注册失败：无效的ID格式");
                System.out.println("注册失败：无效的ID格式"); // 调试日志
            }
        } else {
            writer.println("注册失败：格式错误");
            System.out.println("注册失败：格式错误"); // 调试日志
        }
    }

    private void handleMessage(String message) {
        String[] parts = message.split(" ", 4);
        if (parts.length == 4) {
            String sourceUser = parts[1];
            String targetUser = parts[2];
            String msg = parts[3];
            for (ClientHandler clientHandler : clientHandlers) {
                if (targetUser.equals(clientHandler.getUsername())) {
                    clientHandler.writer.println("message " + sourceUser + ": " + msg);
                    break;
                }
            }
        } else {
            writer.println("Invalid message format");
        }
    }

    private void handlePrivateMessage(String message) {
        try {
            String[] parts = message.split(" ", 4);
            if (parts.length == 4) {
                String sender = parts[1];
                String recipient = parts[2];
                String content = parts[3];

                System.out.println("[Server] 处理私聊消息:");
                System.out.println("发送者: " + sender);
                System.out.println("接收者: " + recipient);
                System.out.println("内容: " + content);

                // 转发消息
                for (ClientHandler handler : clientHandlers) {
                    if (recipient.equals(handler.getUsername())) {
                        String forwardMsg = "private_msg " + sender + " " + content;
                        System.out.println("[Server] 转发消息: " + forwardMsg);
                        handler.writer.println(forwardMsg);
                        handler.writer.flush();
                        return;
                    }
                }

                // 未找到接收者
                System.out.println("[Server] 接收者不在线: " + recipient);
                writer.println("error 用户不在线");
                writer.flush();
            }
        } catch (Exception e) {
            System.err.println("[Server] 处理私聊消息异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleGroupMessage(String message) {
        String[] parts = message.split(" ", 3);
        if (parts.length == 3) {
            String sender = parts[1];
            String content = parts[2];

            // 修改验证逻辑，允许匿名发送
            if (!sender.equals(username) && !sender.startsWith("匿名用户")) {
                writer.println("error Invalid sender identity");
                return;
            }

            // 广播到所有客户端
            for (ClientHandler handler : clientHandlers) {
                if (!handler.equals(this)) {
                    handler.writer.println(sender + ": " + content);
                }
            }
        }
    }

    private void handleAnonymousMessage(String message) {
        String[] parts = message.split(" ", 3);
        if (parts.length == 3) {
            String anonymousId = parts[1];
            String content = parts[2];

            // 广播匿名消息给所有客户端
            for (ClientHandler handler : clientHandlers) {
                if (!handler.equals(this)) {
                    handler.writer.println(anonymousId + ": " + content);
                }
            }
        }
    }

    private void handleAnonymousJoin(String message) {
        String[] parts = message.split(" ", 2);
        if (parts.length == 2) {
            String anonymousId = parts[1];
            // 广播加入消息
            for (ClientHandler handler : clientHandlers) {
                if (!handler.equals(this)) {
                    handler.writer.println("系统: " + anonymousId + " 加入了匿名群聊");
                }
            }
        }
    }

    private void sendUserList(PrintWriter writer) {
        List<User> users = Database.getUsers();
        for (User user : users) {
            writer.println("user " + user.getUsername());
        }
        writer.println("end_of_list");
    }

    private void handleDisconnection() {
        if (username != null) {
            System.out.println("用户断开连接: " + username);
            ServerWindow.updateUserStatus(username, false);
        }
        clientHandlers.remove(this);
        try {
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getUsername() {
        return username;
    }
}