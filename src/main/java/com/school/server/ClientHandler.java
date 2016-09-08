/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.school.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mikkel
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final Server server;
    private String username;
    private final BufferedReader input;
    private final PrintWriter output;

    /**
     * Makes a new object of the type ClientHandler.
     *
     * @param socket
     * @param server
     * @throws IOException
     */
    public ClientHandler(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        this.username = "";
        this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.output = new PrintWriter(socket.getOutputStream());
    }

    /**
     * Is called when we make a new ClientHandler object, and is made this way
     * so the server can be passed in a static context.
     *
     * @param socket
     * @param server
     * @return
     * @throws IOException
     */
    public static ClientHandler setServer(Socket socket, Server server) throws IOException {
        return new ClientHandler(socket, server);
    }

    public String getUsername() {
        return username;
    }

    /**
     * Checks which command is sent from the client and calls the appropriate
     * method.
     *
     * @param data
     * @throws InterruptedException
     * @throws IOException
     */

    synchronized private void checkHeader(String[] data) throws InterruptedException, IOException {
        switch (data[0]) {
            case "LOGIN":
                if (username.isEmpty()) {
                    this.username = data[1];
                    server.updateClientList();
                    break;
                }
                break;

            case "MSG":
                if (!username.isEmpty()) {
                    server.handelMessage(data, this.username);
                    break;
                }
                break;

            case "LOGOUT":
                if (!username.isEmpty()) {
                    socket.close();
                    server.removeObserver(this);
                    break;
                }
                break;
        }
    }

    /**
     * Sends the message to the client.
     *
     * @param command
     * @param message
     * @param username
     */
    public void sendMessage(String command, String message, String username) {
        output.println(command + ":" + username + ":" + message);
        output.flush();
    }

    /**
     * Sends the message to the client.
     * @param command
     * @param username 
     */
    public void sendMessage(String command, String username) {
        output.println(command + ":" + username);
        output.flush();
    }

    /**
     * Listens for a message from the client and sends it to the checkHeader
     * method.
     */
    @Override
    public void run() {
        while (true) {
            try {
                String message = input.readLine();
                String[] headers = message.split(":");
                checkHeader(headers);
            } catch (IOException ex) {
                Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                Logger.getLogger(Log.LOG_NAME).log(Level.INFO, ex.getMessage());
            } catch (InterruptedException ex) {
                Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                Logger.getLogger(Log.LOG_NAME).log(Level.INFO, ex.getMessage());
            }
        }
    }
}
