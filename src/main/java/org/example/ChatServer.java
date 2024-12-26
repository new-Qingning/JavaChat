package org.example;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static Set<ClientHandler> clientHandlers = new HashSet<>();

    public static void main(String[] args) {
        System.out.println("Chat server started...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket);
                clientHandlers.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void broadcastMessage(String message, ClientHandler senderHandler) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler != senderHandler) {
                clientHandler.sendMessage(message);
            }
        }
    }

    private static void handleMessage(String rawMessage, ClientHandler senderHandler) {
        if (rawMessage.startsWith("PRIVATE|")) {
            String[] parts = rawMessage.split("\\|", 3);
            if (parts.length == 3) {
                String targetUser = parts[1];
                String content = parts[2];
                // 在当前 clientHandlers 中找到对应的 ClientHandler
                for (ClientHandler clientHandler : clientHandlers) {
                    if (clientHandler.username != null && clientHandler.username.equals(targetUser)) {
                        clientHandler.sendMessage("私聊来自 " + senderHandler.username + ": " + content);
                        break;
                    }
                }
            }
        } else {
            broadcastMessage(rawMessage, senderHandler);
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        public String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Handle client connection and messages
                out.println("Enter your username:");
                username = in.readLine();
                String message;
                while ((message = in.readLine()) != null) {
                    ChatServer.handleMessage(message, this);
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

        public void sendMessage(String message) {
            out.println(message);
        }
    }
}
