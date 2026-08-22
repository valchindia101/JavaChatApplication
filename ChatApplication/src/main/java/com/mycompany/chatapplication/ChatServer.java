/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.chatapplication;

/**
 *
 * @author DELL
 */
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static final String HISTORY_FILE = "chat_history.txt";
    
    // Set to store active client output streams for broadcasting
    private static final Set<PrintWriter> clientWriters = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {
        System.out.println("Chat server started on port " + PORT + "...");
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    // Broadcasts a message to all connected clients and writes it to file
    public static synchronized void broadcastAndSave(String message) {
        // Send to all connected clients
        for (PrintWriter writer : clientWriters) {
            writer.println(message);
        }

        // Append to history file
        try (FileWriter fw = new FileWriter(HISTORY_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(message);
        } catch (IOException e) {
            System.err.println("Error saving message to history: " + e.getMessage());
        }
    }

    // Reads previous messages from the log file and sends them to a newly connected client
    private static synchronized void loadHistory(PrintWriter out) {
        File file = new File(HISTORY_FILE);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            out.println("--- Loading Previous Chat History ---");
            while ((line = reader.readLine()) != null) {
                out.println(line);
            }
            out.println("--- End of History ---");
        } catch (IOException e) {
            System.err.println("Error reading history file: " + e.getMessage());
        }
    }

    // Handles individual client communication
    private static class ClientHandler implements Runnable {
        private final Socket socket;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
            ) {
                out = new PrintWriter(socket.getOutputStream(), true);

                // 1. Send past history to the new client first
                loadHistory(out);

                // 2. Add client output stream to the broadcast pool
                clientWriters.add(out);

                String clientAddress = socket.getRemoteSocketAddress().toString();
                System.out.println("Client connected: " + clientAddress);

                // 3. Process incoming messages
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Received: " + message);
                    broadcastAndSave(message);
                }
            } catch (IOException e) {
                System.out.println("Client disconnected.");
            } finally {
                if (out != null) {
                    clientWriters.remove(out);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    // Ignore on close
                }
            }
        }
    }
}
