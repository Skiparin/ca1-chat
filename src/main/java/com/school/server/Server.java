package com.school.server;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Orvur on 06/09/16.
 */
public class Server {

    private static final List<ClientHandler> observers = new CopyOnWriteArrayList<>();
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        try {
            Log.setLogFile("logFile.txt", "ServerLog");
            //Start the server here
            Logger.getLogger(Log.LOG_NAME).log(Level.INFO, "Starting the Server");
            new Server().start(args[0], Integer.parseInt(args[1]));
        } finally {
            Log.closeLogger();
        }
    }

    public void addObserver(ClientHandler handler) {
        observers.add(handler);
    }

    public void removeObserver(ClientHandler handler) {
        observers.remove(handler);
        updateClientList();
    }

    public void handelMessage(String[] data, String username) throws InterruptedException {
        String[] users = data[1].split(",");
        String command = "MSGRES";
        for (ClientHandler client : observers) {
            if (!data[1].isEmpty()) {
                if (client.getUsername().equals(username)) {
                    client.sendMessage(command, data[2], username);
                }
                for (String user : users) {
                    if (client.getUsername().equals(user)) {
                        client.sendMessage(command, data[2], username);
                    }
                }
            } else {
                client.sendMessage(command, data[2], username);
            }
        }
        Logger.getLogger(Log.LOG_NAME).log(Level.INFO, data[0] + data[1] + data[2]);
    }

    public void start(String ip, int port) {
        try {
            ServerSocket server = new ServerSocket();
            server.bind(new InetSocketAddress(ip, port));
            while (true) {
                Socket socket = server.accept();
                ClientHandler handler = ClientHandler.setServer(socket, this);
                observers.add(handler);
                threadPool.submit(handler);
            }
        } catch (Exception ex) {
            Logger.getLogger(Log.LOG_NAME).log(Level.INFO, ex.getMessage());
        }
    }

    public void updateClientList() {
        String clientList = "";

        //TODO: Why do we have two for loops here?
        for (ClientHandler observer : observers) {
            String command = "CLIENTLIST";
            for (ClientHandler o : observers) {
                clientList = clientList + o.getUsername() + ",";
            }
            clientList = clientList.substring(0, (clientList.length() - 1));
            observer.sendMessage(command, "", clientList);
        }
        Logger.getLogger(Log.LOG_NAME).log(Level.INFO, "Clientlist updated: " + clientList);
    }

}
