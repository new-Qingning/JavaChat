package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatWindow extends JFrame {
    private JTextArea chatArea;
    private JTextArea inputArea;
    private String username;

    public ChatWindow(String username) {
        this.username = username;
        setTitle("Chat Window - " + username);
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create components
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);

        inputArea = new JTextArea(3, 20);
        JScrollPane inputScrollPane = new JScrollPane(inputArea);

        JButton sendButton = new JButton("Send");

        // Set layout manager
        setLayout(new BorderLayout());

        // Add components to the frame
        add(chatScrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputScrollPane, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        // Load chat history
        loadChatHistory();

        // Add action listener to the send button
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        setVisible(true);
    }

    private void loadChatHistory() {
        File file = new File("/src"+username + ".txt");
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    chatArea.append(line + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessage() {
        String message = inputArea.getText().trim();
        if (!message.isEmpty()) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String formattedMessage = username + " [" + timestamp + "]: " + message;
            chatArea.append(formattedMessage + "\n");
            saveMessage(formattedMessage);
            inputArea.setText("");
        }
    }

    private void saveMessage(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(username + ".txt", true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String username = JOptionPane.showInputDialog("Enter your username:");
        if (username != null && !username.trim().isEmpty()) {
            new ChatWindow(username.trim());
        }
    }
}