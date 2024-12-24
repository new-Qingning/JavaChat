package org.example;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatWindow extends JFrame {
    private JTextArea chatArea;
    private JTextArea inputArea;
    private JButton sendButton;
    private PrintWriter writer;
    private BufferedReader reader;

    public ChatWindow(Socket socket, String username) {
        setTitle("Chat - " + username);
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        inputArea = new JTextArea(3, 20);
        sendButton = new JButton("Send");

        setLayout(new BorderLayout());
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(new JScrollPane(inputArea), BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error initializing writer: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

        new Thread(this::receiveMessages).start();

        setVisible(true);
    }

    private void sendMessage(String username) {
        if (writer == null) {
            JOptionPane.showMessageDialog(this, "Writer is not initialized", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String message = inputArea.getText().trim();
        if (!message.isEmpty()) {
            String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
            writer.println(username + ": " + message);
            chatArea.append("[" + timestamp + "] " + username + ": " + message + "\n");
            inputArea.setText("");
        }
    }

    private void receiveMessages() {
        try {
            String message;
            while ((message = reader.readLine()) != null) {
                chatArea.append(message + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}