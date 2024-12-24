package org.example;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class ChatWindow extends JFrame {
    private JTextArea chatArea;
    private JTextArea inputArea;
    private JButton sendButton;
    private PrintWriter writer;

    public ChatWindow(Socket socket, String username) {
        setTitle("Chat - " + username);
        setSize(600, 400); // Increased size
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        inputArea = new JTextArea(3, 20); // 3 rows, 20 columns
        sendButton = new JButton("Send");

        setLayout(new BorderLayout());
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(new JScrollPane(inputArea), BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        sendButton.addActionListener(e -> sendMessage(username));
        inputArea.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER && evt.isShiftDown()) {
                    inputArea.append("\n");
                } else if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    sendMessage(username);
                    evt.consume();
                }
            }
        });

        setVisible(true);
    }

    private void sendMessage(String username) {
        String message = inputArea.getText().trim();
        if (!message.isEmpty()) {
            writer.println(username + ": " + message);
            chatArea.append("Me: " + message + "\n");
            inputArea.setText("");
        }
    }
}