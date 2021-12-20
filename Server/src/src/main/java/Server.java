/*CS 544
 * 06/03/2021
 * Server
 * Class representing server
 * SERVICE AND CONCURRENT
 * initializes sever with config,
 * waits for clients to connect
 * send client into background thread on connect
 * */
package src.main.java;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;


public class Server extends Thread{
    ServerSocket serverSocket;
    private int port;
    private Boolean isRunning = false;
    public static ArrayList<String> versionNums = new ArrayList<>(){{add("V1.0");}};
    private static  HashMap<String, RequestHandler> connectedClients = new HashMap<>();
    public Server(int port){
        this.port = port;
    }

    public void stopServer(){
        this.isRunning = false;
        this.interrupt();
    }

    /*SERVICE
    * established server on port 8081*/
    public void startServer(){
        try {
            serverSocket = new ServerSocket(port);
            this.start();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        this.isRunning = true;

        /*CONCURRENT
        * As open to accept calls for multiple clients
        * Each client is then executed in background by Request Handler
        * */
        while (isRunning){
            System.out.println("--- Connection open on port: " + this.port + " ---\n");
            try {
                Socket socket = serverSocket.accept();
                RequestHandler requestHandler = new RequestHandler(socket);
                requestHandler.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*Maintain list of connected clients
    * Add client to that list*/
    public void addConnectedClient(String user, RequestHandler handler){
        connectedClients.put(user, handler);
    }

    /*get ref of connected client*/
    public RequestHandler getConnectedClient(String user){
        if (ifClientExists(user)) return connectedClients.get(user);
        return null;
    }

    /*get list of connected clients*/
    public Set<String> getConnectedClients(){
        return connectedClients.keySet();
    }

    /*check if client is in connected list*/
    public Boolean ifClientExists(String user){
        return connectedClients.containsKey(user);
    }

    /*remove client from the connected list*/
    public void removeConnectedClient(String user){
        connectedClients.remove(user);
    }

    /*self-signed cert used in TLS connection*/
    private static final TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }
            }
    };
}
