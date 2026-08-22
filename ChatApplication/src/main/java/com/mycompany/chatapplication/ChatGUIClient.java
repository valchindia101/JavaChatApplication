/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.chatapplication;

/**
 *
 * @author DELL
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ChatGUIClient extends JFrame {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 12345;

    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;

    public ChatGUIClient() {
        super("Java Swing Chat Client");

        // Ask for username before launching UI
        username = JOptionPane.showInputDialog(
                this,
                "Enter your username:",
                "Username Selection",
                JOptionPane.PLAIN_MESSAGE
        );

        if (username == null || username.trim().isEmpty()) {
            username = "User" + (int)(Math.random() * 1000);
        }

        // Setup UI components
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        inputField = new JTextField();
        sendButton = new JButton("Send");

        // Layout panel for the bottom controls
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        // Frame arrangement
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen

        // Event listeners for sending messages
        ActionListener sendListener = e -> sendMessage();
        sendButton.addActionListener(sendListener);
        inputField.addActionListener(sendListener);

        // Connect to server
        connectToServer();
    }

    private void connectToServer() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Announce user joining
            out.println(username + " joined the chat.");

            // Thread to listen for incoming messages
            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        final String msg = serverMessage;
                        SwingUtilities.invokeLater(() -> chatArea.append(msg + "\n"));
                    }
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> chatArea.append("Disconnected from server.\n"));
                }
            }).start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Unable to connect to server at " + SERVER_ADDRESS + ":" + SERVER_PORT + "\nPlease start ChatServer first.", 
                "Connection Error", 
                JOptionPane.ERROR_MESSAGE);
            // Close window gracefully instead of calling System.exit(1)
            dispose();
        }
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (!text.isEmpty() && out != null) {
            out.println(username + ": " + text);
            inputField.setText("");
        }
    }

    public static void main(String[] args) {
        // Launch UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            ChatGUIClient client = new ChatGUIClient();
            client.setVisible(true);
        });
    }
}
