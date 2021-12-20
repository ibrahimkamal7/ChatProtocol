/*CS 544
 * 06/03/2021
 * Main
 * Class helps in abstracting Client and the interface to run
 * This is the main class to run
 * */
package src.main.java;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        System.out.println("--- Welcome to Chat Protocol Client ---\n");
        ChatProtocolClient client = new ChatProtocolClient();
        CommandParser commandParser = new CommandParser();
        InputHandler input = new InputHandler();
        boolean done = false;

        /*Client should be enter commands at any cost
        * Errors are handled elegantly to ensure this behavior
        * */
        while (!done) {
            String command = input.askForCommand("Please enter a connect command:");
            if(command == null) return;

            if (command.length() ==0) {
                return;
            }

            String[] commandToArray = command.split("\\s+");

            if (commandToArray.length == 3) {
                command += " -u";
            }

            /*UI
            * User interacts using the command line*/
            if (commandParser.isConnectCommandValid(command)) {
                String[] hostAndPort = commandParser.parseConnectCommand(command);

                if (client.connectToSever(hostAndPort[0], hostAndPort[1])) {
                    client.sendCommandToServer(command);

                    //while (!command.equalsIgnoreCase("e") && (!client.isClosed())) {
                    while (!client.isClosed()) {
                        command = input.askForCommand("Please enter a command.");
                        client.sendCommandToServer(command);
                    }
                }
            } else {
                System.out.println("Invalid Command");
            }
        }
    }
}
