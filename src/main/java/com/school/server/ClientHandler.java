/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.school.server;

import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 *
 * @author Mikkel
 */
public class ClientHandler implements Runnable{
    private final Socket socket;
    private String username;

    public ClientHandler(Socket socket){
        this.socket = socket;
        this.username = "";
    }
   
    
    public void update(HashMap<String, List<String>> data) {
        //switch(data)
    }

    @Override
    public void run() {
        
    }
    
    
}
