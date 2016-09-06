package com.school.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Mikkel on 06/09/16.
 */
public class Server{
    private static final List<ClientHandler> observers = new CopyOnWriteArrayList<>();
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();
    
    public static void main(String[] args) {
        start(args[0], Integer.parseInt(args[1]));
    }

    public void addObserver(ClientHandler handler){
        observers.add(handler);
    }

    public void notifyObservers(HashMap<String, List<String>> data){
        for(ClientHandler handler : observers) handler.update(data);
    }

    
    private static void start(String ip, int port){
        try{
            ServerSocket server = new ServerSocket();
            server.bind(new InetSocketAddress(ip, port));
            
            while(true){
                Socket socket = server.accept();
                ClientHandler handler = new ClientHandler(socket);
                observers.add(handler);
                threadPool.submit(handler);
            }
        } catch (Exception ex) {
            System.out.println(ex); 
        }
    }
   
}
