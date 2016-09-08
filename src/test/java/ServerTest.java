/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.school.server.Server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;


/**
 *
 * @author kaspe
 */
public class ServerTest {
    
    PrintWriter outClient1;
    BufferedReader inClient1;
    PrintWriter outClient2;
    BufferedReader inClient2;
    Socket socket1;
    Socket socket2;

    public ServerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws InterruptedException {
        String[] args = {"localhost", "7777"};
        new Thread(new Runnable() {
            @Override
            public void run() {
                new Server().start("localhost", 7777);
            }
        }).start();
    }

    @AfterClass
    public static void tearDownClass() {
        Server.stopServer();
    }

    
    @Before
    public void setUp() throws IOException {
        socket2 = new Socket("localhost", 7777);
        socket1 = new Socket("localhost", 7777);
        outClient1 = new PrintWriter(socket1.getOutputStream());
        inClient1 = new BufferedReader( new InputStreamReader(socket2.getInputStream()));
        outClient2 = new PrintWriter(socket1.getOutputStream());
        inClient2 = new BufferedReader( new InputStreamReader(socket2.getInputStream()));
        
    }

    @After
    public void tearDown() throws IOException {
        socket1.close();
        socket2.close();
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    
    @Test
    public void connect() throws IOException{
        new Thread( () -> {
                Assert.assertEquals(socket1.isBound(), true);
                }).start();
        outClient1.write("MSG::Test");
    }
    
    @Test
    public void recieveClientlist() throws IOException{
        outClient1.write("LOGIN:Kasper");
        outClient2.write("LOGIN:Phillip");
        new Thread( () -> {
            try {
                String clientList = inClient2.readLine();
                Assert.assertEquals(clientList, "CLIENTLIST:Kasper,Phillip");
            } catch (IOException ex) {
                Logger.getLogger(ServerTest.class.getName()).log(Level.SEVERE, null, ex);
            }
                }).start();
    }
    
    @Test
    public void recieveMSG() throws IOException{
        outClient1.write("LOGIN:Kasper");
        outClient2.write("LOGIN:Phillip");
        outClient1.write("MSG:Phillip:Dette er en string");
        new Thread( () -> {
            try {
                String clientList = inClient2.readLine();
                Assert.assertEquals(clientList, "MSGRES:Kasper:Dette er en string");
            } catch (IOException ex) {
                Logger.getLogger(ServerTest.class.getName()).log(Level.SEVERE, null, ex);
            }
                }).start();
    }
    
}
