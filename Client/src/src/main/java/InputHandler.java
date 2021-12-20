/*CS 544
 * 06/03/2021
 * InputHandler
 * Class helps in reading and parsing user command line input
 * */
package src.main.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class InputHandler {
    /*UI
    * Read Command line Inputs provided by the user
    * */
    public String askForCommand(String action) {
        System.out.println("\n"+action+"\n");
        String command = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            command = br.readLine();
        }
        catch (IOException e){
            System.out.println(e);
        }

        return command;
    }
}
