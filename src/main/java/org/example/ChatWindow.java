package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatWindow extends JFrame {
    private JTextArea chatArea;
    private JTextArea inputArea;
    private JButton sendButton;
    private PrintWriter writer;
    private String sourceUser;
    private String targetUser;
    private Socket socket;
    private String username;

    public ChatWindow(Socket socket, String sourceUser, String targetUser) {
        this.socket = socket;
        this.sourceUser = sourceUser;
        this.targetUser = targetUser;
        setTitle("Chat with " + targetUser);
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        inputArea = new JTextArea(3, 20);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);

        sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(new JScrollPane(inputArea), BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        add(inputPanel, BorderLayout.SOUTH);

        try {
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        setVisible(true);
    }

    public void receiveMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            if (message.startsWith("private_msg ")) {
                String[] parts = message.split(" ", 3);
                if (parts.length == 3) {
                    chatArea.append(String.format("%s -> Me: %s\n", parts[1], parts[2]));
                }
            } else if (message.startsWith("error ")) {
                String error = message.substring(6);
                JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
            }
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    private void sendMessage() {
        String message = inputArea.getText().trim();
        if (!message.isEmpty()) {
            writer.println("private " + sourceUser + " " + targetUser + " " + message);
            chatArea.append(String.format("Me -> %s: %s\n", targetUser, message));
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
            inputArea.setText("");
        }
    }
}