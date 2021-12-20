/*CS 544
 * 06/03/2021
 * CommandParser
 * Class helps in command parsing of user
 * */
package src.main.java;
import java.util.Arrays;
import java.util.HashMap;

public class CommandParser {
    static private HashMap<String, String> commands = new HashMap<>();


    public CommandParser(){
        commands.put("\\CONNECT", "CONNECT");
        commands.put("\\PASS", "PASS");
        commands.put("\\LIST", "LIST");
        commands.put("\\JOIN", "JOIN");
        commands.put("\\MSG", "MSG");
        commands.put("\\PART", "PART");
        commands.put("\\EXIT", "EXIT");
        commands.put("\\ADD", "ADD");
        commands.put("\\REMOVE", "REMOVE");
        commands.put("\\VERSION", "VERSION");
        commands.put("\\NICK", "NICK");
        commands.put("\\EXIT", "EXIT");
        commands.put("\\EXITED", "EXITED");
    }

    /*check if command PDU is correctly formatted*/
    public String checkCommand(String command){
        if (commands.containsKey(command.split(" ")[0])){
            return commands.get(command.split(" ")[0]);
        }
        return "";
    }

    /*check if connect command is correct*/
    public String[] parseConnectCommand(String command) {
        String[] commandToArray = command.split("\\s+");

        String hostAndPort = commandToArray[1];
        String[] hostAndPortToArray = hostAndPort.split(":");

        String hostWithUserName = hostAndPortToArray[0];
        String[] hostWithUserNameToArray = hostWithUserName.split("@");

        String[] returnArray = {hostWithUserNameToArray[0],  hostAndPortToArray[1], commandToArray[2], commandToArray[3]};
        return returnArray;
    }

    /*parse commands other than connect and msg
    * get the CONNECT command arguments*/
    public String[] parseGenericCommands(String command){
        String[] commandToArray = command.split("\\s+");

        if (commandToArray.length > 1) return Arrays.copyOfRange(commandToArray, 1, commandToArray.length);
        else return new String[0];
    }

    /*parse commands for the msg command
    * get the message and the user*/
    public String[] parseMsgCommands(String command){
        String[] commandToArray = command.split("\\s+");

        if (commandToArray.length > 1){
            String checkIfUser = commandToArray[1];
            if (UserDatabase.database.containsKey(checkIfUser)){
                String msg = String.join(" ", Arrays.copyOfRange(commandToArray, 2, commandToArray.length));
                return new String[]{checkIfUser, msg};
            }
            else{
                String msg = String.join(" ", Arrays.copyOfRange(commandToArray, 1, commandToArray.length));
                return new String[]{msg};
            }
        }
        else return new String[0];
    }
}
