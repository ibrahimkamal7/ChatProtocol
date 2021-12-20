/*CS 544
 * 06/03/2021
 * CommandParser
 * Class helps in parsing connect command and
 * making sure it is correct
 * */
package src.main.java;

public class CommandParser {

    /* CLIENT
    * Used to check connect command is correct
    * has Version number or not
    * has -u or -a flag or not
    * arguments are in order or not
    * */
    public boolean isConnectCommandValid(String command) {
        String[] commandToArray = command.split("\\s+");

        if(commandToArray[0].equalsIgnoreCase("\\CONNECT") && (commandToArray.length >= 3 && commandToArray.length <= 4)) {
            if(commandToArray.length == 4) {
                if(!commandToArray[3].equalsIgnoreCase("-u") && !commandToArray[3].equalsIgnoreCase("-a")) {
                    return false;
                }
            }
            String hostAndPort = commandToArray[1];
            String[] hostAndPortToArray = hostAndPort.split(":");
            if(hostAndPortToArray.length < 2) {
                return false;
            }
            String hostWithUserName = hostAndPortToArray[0];
            String[] hostWithUserNameToArray = hostWithUserName.split("@");
            if(hostWithUserNameToArray.length < 2) {
                return false;
            }

            if (!command.contains(" V") || !commandToArray[2].contains("V")) return false;
            return true;
        }

        return false;
    }

    /* CLIENT
    * After checking the command,
    * this method is used to get the various arguments from the command*/
    public String[] parseConnectCommand(String command) {
        String[] commandToArray = command.split("\\s+");

        String hostAndPort = commandToArray[1];
        String[] hostAndPortToArray = hostAndPort.split(":");

        String hostWithUserName = hostAndPortToArray[0];
        String[] hostWithUserNameToArray = hostWithUserName.split("@");

        String[] returnArray = {hostWithUserNameToArray[1],  hostAndPortToArray[1]};
        return returnArray;
    }

}
