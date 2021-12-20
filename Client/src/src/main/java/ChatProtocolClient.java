/*CS 544
* 06/03/2021
* ChatProtocol Client
* Class representing client abstraction for the protocol
* */
package src.main.java;

import javax.net.ssl.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;

public class ChatProtocolClient {
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    /*CLIENT
    * User is able to specify the hostname or the IP address of the client
    * */
    public boolean connectToSever(String server, String port){
        try {
            this.socket = new Socket(InetAddress.getByName(server), Integer.parseInt(port));
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
            return true;
        } catch (Exception e) {
            //e.printStackTrace();
            System.out.println("Unable to Connect\n");
            return false;
        }
    }

    /* Once connection established, send commands using this methods*/
    public void sendCommandToServer(String command) throws IOException {
        try {

            oos.writeObject(command);
            oos.flush();

            Object serverResponse = new Object();

            serverResponse = ois.readObject();
            String response = (String) serverResponse;

            /*Two way closing, making sure sockets get closed on both server and client*/
            if (response.contains("250 Closing Connection") ||
                response.equals("520 User already exists, Closing Connection") ||
                response.equals("550 User Not Found, Closing Connection")){
                closeConnection();
            }
            else if (response.equals("599 Server Time Out, Closing")) forceClose();

            System.out.println(response);

        }
        catch(SocketException e){
            System.out.println("Internal Connection Error\n");
            forceClose();
        }
        catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    /*Two way closing close connection on the client side*/
    public void closeConnection() throws IOException {
        oos.writeObject("\\EXITED");
        oos.flush();

        ois.close();
        oos.close();
        socket.close();
    }

    /*In case of network error, force close the socket*/
    public void forceClose() throws IOException {
        try {
            ois.close();
            oos.close();
            socket.close();
        }
        catch (Exception e) {System.out.println("Force Closing Connection");}
    }

    /*check if socket is closed*/
    public boolean isClosed(){
        return socket.isClosed();
    }

    public void flushMsg() throws IOException, ClassNotFoundException {
        Object serverResponse = ois.readObject();
        while (ois.available() > 0){
            System.out.println("-----------");
            //serverResponse = ois.readObject();
            String response = (String) serverResponse;
            System.out.println(response);
            System.out.println("-----------");
        }
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
