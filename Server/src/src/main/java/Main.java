/*CS 544
 * 06/03/2021
 * Main
 * Class acting as a portal to run
 * This is the main class to run
 * */
package src.main.java;

public class Main {
    public static Server server;

    /* SERVICE
    * Fixed port number 8081
    * */
    public static void main(String[] args) {
        server = new Server(8081);
	    server.startServer();
    }
}
