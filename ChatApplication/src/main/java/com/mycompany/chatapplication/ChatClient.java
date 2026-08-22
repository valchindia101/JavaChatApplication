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
import java.util.Scanner;
public class ChatClient {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            System.out.println("Connected to the chat server!");

            // Thread to continuously read messages from the server
            new Thread(() -> {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    System.out.println("Connection to server closed.");
                }
            }).start();

            // Main thread reads terminal input and sends it to the server
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in);

            System.out.print("Enter your username: ");
            String username = scanner.nextLine();
            out.println(username + " has joined the room.");

            while (scanner.hasNextLine()) {
                String input = scanner.nextLine();
                out.println(username + ": " + input);
            }

            socket.close();
        } catch (IOException e) {
            System.err.println("Unable to connect to server: " + e.getMessage());
        }
    }
}
