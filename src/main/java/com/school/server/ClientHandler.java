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

    public ClientHandler(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        this.username = "";
        this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.output = new PrintWriter(socket.getOutputStream());
    }

    public static ClientHandler setServer(Socket socket, Server server) throws IOException {
        return new ClientHandler(socket, server);
    }

    public String getUsername() {
        return username;
    }

    synchronized private void checkHeader(String[] data) throws InterruptedException, IOException {
        switch (data[0]) {
            case "LOGIN":
                if (username.equals("")) {
                    this.username = data[1];
                    server.updateClientList();
                    break;
                }
                //TODO: Fejlbesked - Logger?
                break;

            case "MSG":
                if (!username.equals("")) {
                    server.handelMessage(data, this.username);
                    break;
                }
                //TODO: Fejlbesked - Logger?
                break;

            case "LOGOUT":
                if (!username.equals("")) {
                    socket.close();
                    server.removeObserver(this);
                    break;
                }
                //TODO: Fejlbesked - Logger?
                break;
        }
    }

    public void sendMessage(String command, String message, String username) {
        switch (command) {
            case "MSGRES":
                output.println(command + ":" + username + ":" + message);
                output.flush();
                break;
            case "CLIENTLIST":
                output.println(command + ":" + username);
                output.flush();
                break;
        }
    }

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
