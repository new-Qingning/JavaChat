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
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            writer = new PrintWriter(socket.getOutputStream(), true);
            String message;
            while ((message = reader.readLine()) != null) {
                System.out.println("Received: " + message);
                if (message.startsWith("auth ")) {
                    handleAuth(message);
                } else if (message.startsWith("private ")) {
                    handlePrivateMessage(message);
                } else if (message.startsWith("message ")) {
                    handleMessage(message);
                } else if (message.startsWith("login")) {
                    handleLogin(message);
                } else if (message.startsWith("register")) {
                    handleRegister(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            clientHandlers.remove(this);
        }
    }

    private void handleAuth(String message) {
        String[] parts = message.split(" ", 2);
        if (parts.length == 2) {
            String requestedUsername = parts[1];
            // 验证用户名是否有效
            if (isValidUsername(requestedUsername)) {
                this.username = requestedUsername;
                writer.println("auth_success");
            } else {
                writer.println("auth_failed");
            }
        }
    }

    private boolean isValidUsername(String username) {
        // 检查是否是已登录的有效用户名
        List<User> users = Database.getUsers();
        return users.stream().anyMatch(user -> user.getUsername().equals(username));
    }

    private void handleLogin(String message) {
        String[] parts = message.split(" ");
        if (parts.length == 3) {
            int id = Integer.parseInt(parts[1]);
            String password = parts[2];
            if (Database.validateUser(id, password)) {
                this.username = Database.getUsernameById(id);
                writer.println("Login successful");
                sendUserList(writer);
            } else {
                writer.println("Login failed");
            }
        } else {
            writer.println("Invalid login format");
        }
    }

    private void handleRegister(String message) {
        String[] parts = message.split(" ");
        if (parts.length == 4) {
            int id = Integer.parseInt(parts[1]);
            String username = parts[2];
            String password = parts[3];
            if (Database.registerUser(id, username, password)) {
                writer.println("Register successful");
            } else {
                writer.println("Register failed");
            }
        } else {
            writer.println("Invalid register format");
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
        String[] parts = message.split(" ", 4);
        if (parts.length == 4) {
            String sender = parts[1];
            String recipient = parts[2];
            String content = parts[3];

            if (!sender.equals(username)) {
                writer.println("error Invalid sender identity");
                return;
            }

            boolean sent = false;
            for (ClientHandler handler : clientHandlers) {
                if (recipient.equals(handler.getUsername())) {
                    handler.writer.println("private_msg " + sender + " " + content);
                    sent = true;
                    break;
                }
            }

            if (!sent) {
                writer.println("error User offline or not found");
            }
        }
    }

    private void handlePublicMessage(String message) {
        String[] parts = message.split("\\|");
        if (parts.length == 4) {
            String sourceUser = parts[1];
            String targetUser = parts[2];
            String content = parts[3];

            // 发送消息给目标用户
            for (ClientHandler handler : clientHandlers) {
                if (targetUser.equals(handler.getUsername())) {
                    handler.writer.println("MESSAGE|" + sourceUser + "|" + content);
                    break;
                }
            }
        }
    }

    private void broadcastMessage(String message) {
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.writer.println(message);
        }
    }

    private void sendUserList(PrintWriter writer) {
        List<User> users = Database.getUsers();
        for (User user : users) {
            writer.println("user " + user.getUsername());
        }
        writer.println("end_of_list");
    }

    public String getUsername() {
        return username;
    }
}