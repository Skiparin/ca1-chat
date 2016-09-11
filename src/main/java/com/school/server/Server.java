package com.school.server;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Orvur on 06/09/16.
 */
public class Server {

    private static final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();
    private static AtomicBoolean keepAlive = new AtomicBoolean(true);

    public static void main(String[] args) {
        try {
            Log.setLogFile("logFile.txt", "ServerLog");
            Logger.getLogger(Log.LOG_NAME).log(Level.INFO, "Starting the Server");
            new Server().start(args[0], Integer.parseInt(args[1]));
        } finally {
            Log.closeLogger();
        }
    }

    /**
     * Adds a Observer object to the List.
     *
     * @param handler
     */
    public void addObserver(ClientHandler handler) {
        clients.add(handler);
    }

    /**
     * Removes a Observer object from the List.
     *
     * @param handler
     */
    public void removeObserver(ClientHandler handler) {
        clients.remove(handler);
        updateClientList();
    }

    /**
     * Receives a incoming message from a client, and sends the message to the
     * appropriate recipient.
     *
     * @param data
     * @param username
     * @throws InterruptedException
     */
    public void handelMessage(String[] data, String username) throws InterruptedException {
        String[] users = data[1].split(","); //data is split on "," to get the recipients of the message.
        String command = "MSGRES"; //Variable contains MSGRES so we match the correct switch case in sendMessage().
        for (ClientHandler client : clients) {
            if (!data[1].isEmpty()) { //If the string of recipients is empty, the message will be sent to all.
                
                for (String user : users) {
                    if (client.getUsername().equals(user)) {
                        client.sendMessage(command, data[2], username);
                    }
                }
            } else {
                if (!client.getUsername().equals(username)) {
                client.sendMessage(command, data[2], username);
                }
            }
        }
        Logger.getLogger(Log.LOG_NAME).log(Level.INFO, data[0] + ":" + data[1] + ":" + data[2]);
    }

    public static void stopServer() {
        keepAlive.set(false);
    }

    /**
     * Starts the server with ip and portnumber specified in the parameters.
     *
     * @param ip
     * @param port
     */
    public void start(String ip, int port) {
        try {
            ServerSocket server = new ServerSocket();
            server.bind(new InetSocketAddress(ip, port));

            while (keepAlive.get()) {
                Socket socket = server.accept();
                ClientHandler handler = ClientHandler.setServer(socket, this);
                clients.add(handler);
                threadPool.submit(handler);
            }
        } catch (Exception ex) {
            Logger.getLogger(Log.LOG_NAME).log(Level.INFO, ex.getMessage());
        }
    }

    /**
     * Makes a list of the current clients connected to the server.
     */
    public void updateClientList() {
        String clientList = "";
        //Variable contains CLIENTLIST so we match the correct switch case in sendMessage().
        String command = "CLIENTLIST"; 
        for (ClientHandler observer : clients) {
            //A "," is added to follow the message protocol.
            clientList = clientList.concat(observer.getUsername() + (",")); 
        }
        clientList = clientList.substring(0, (clientList.length() - 1));
        for (ClientHandler o : clients) {
            o.sendMessage(command, clientList);
        }
        Logger.getLogger(Log.LOG_NAME).log(Level.INFO, "Clientlist updated: " + clientList);
    }

}
