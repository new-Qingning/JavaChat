
package org.example;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            String message;
            while ((message = reader.readLine()) != null) {
                System.out.println("Received: " + message);
                if (message.startsWith("login")) {
                    String[] parts = message.split(" ");
                    if (parts.length == 3) {
                        int id = Integer.parseInt(parts[1]);
                        String password = parts[2];
                        if (Database.validateUser(id, password)) {
                            writer.println("Login successful");
                            // Open chat window logic here
                        } else {
                            writer.println("Login failed");
                        }
                    } else {
                        writer.println("Invalid login format");
                    }
                } else if (message.startsWith("register")) {
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
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}