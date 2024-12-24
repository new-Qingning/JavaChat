package org.example;

import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (InputStream input = socket.getInputStream();
             OutputStream output = socket.getOutputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(input));
             PrintWriter writer = new PrintWriter(output, true)) {

            String message;
            while ((message = reader.readLine()) != null) {
                System.out.println("Received: " + message);
                if (message.contains("login")) {
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
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}