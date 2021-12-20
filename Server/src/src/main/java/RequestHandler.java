/*CS 544

 * 06/03/2021
 * Request Handler
 * CONCURRENT
 * A thread for every individual user that connects
 * STATEFUL Requirement Covered by this class
 * */
package src.main.java;

import src.main.java.states.DFA;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/*Request Handler
 * CONCURRENT
 * A thread for every individual user that connects
 * STATEFUL Requirement Covered by this class
 * */
public class RequestHandler extends Thread {
    ArrayList<String> pendingMsgs = new ArrayList<>();
    boolean AdminMode = false;
    String userName = "";
    public Channel activeChannel = null;
    static private Response response = new Response();
    static private UserDatabase database = new UserDatabase();
    static private Channels channels = new Channels();
    private CommandParser commandParser = new CommandParser();
    private String clientConnected;
    private Socket socket;
    private ObjectInputStream is;
    private ObjectOutputStream os;
    public RequestHandler(Socket socket){this.socket = socket;}
    private DFA DFA = new DFA("Idle");

    /*Two way closing, close stream and make sure connection is closed*/
    public void closeConnection(ObjectInputStream is, ObjectOutputStream os){
        try{
            //close streams
            System.out.println("\n--- Closing Connection with "+ userName + " ---\n");
            socket.setSoTimeout(0);
            is.close();
            os.close();
            socket.close();
            Main.server.removeConnectedClient(userName);
            AdminMode = false;
            activeChannel = null;
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void run(){
        try {

            System.out.println("Checking Connection with " + socket.getInetAddress().getHostName() + "\n");
            clientConnected = socket.getInetAddress().getHostName();

            is = new ObjectInputStream(socket.getInputStream());
            os = new ObjectOutputStream(socket.getOutputStream());
            Object PDU = null;
            DFA.state = "ReqSent";

            socket.setSoTimeout(180000); // wait for couple of minutes for response
            /*STATEFUL (While loop below)
             * As DFA changes state according to the commands
             * Next state transitions are made using DFA diagram
             * DFA diagram have been fixed according to the remarks/fixes made in the meeting with Professor
             * Checks are established in place to ensure smooth transitions such as:
             * Timeout are placed in action and varies according to the state
             * Two way closing, client first send \EXIT and server initializes closing by ACK
             * After that client closes its connection, just before that informing Server about it
             * Then, Server closes it own connection
             * This made sure connection is properly closed
             * */
            while ((PDU = is.readObject()) != null) {
                String tmp = (String) PDU;
                String cmd = commandParser.checkCommand(tmp);

                /*For all the commands
                 * if command is formatted right, send 511
                 * if state is not correct, send 512
                 * These are common between the transitions*/
                switch (cmd) {
                    case "CONNECT":
                        /*Connect command
                         * checks for version number if accepted (send 205) otherwise (send 501)
                         * admin mode requested or not
                         * user already connected or not if yes (send 520)*/
                        if (DFA.state.equals("ReqSent")) {
                            String user = commandParser.parseConnectCommand(tmp)[0];
                            if (database.isExist(user) && (!Main.server.ifClientExists(user))) {
                                userName = user;
                                if (Server.versionNums.contains(commandParser.parseConnectCommand(tmp)[2])) {
                                    if (commandParser.parseConnectCommand(tmp)[3].equals("-a")
                                            && database.isAdmin(user)) {
                                        AdminMode = true;
                                    }

                                    sendResponse(response.get(205)+"\n"+response.get(301));
                                    //os.writeObject(response.get(205)+"\n"+response.get(301));
                                    //os.writeObject(response.get(301));
                                    DFA.state = "ReqReceived";
                                    Main.server.addConnectedClient(user, this);
                                    //clientConnected = user;
                                }
                                else{
                                    sendResponse(response.get(501));
                                    //os.writeObject(response.get(501));
                                }
                            }
                            else if (Main.server.ifClientExists(user)) {
                                sendResponse(response.get(520));
                                //os.writeObject(response.get(520));
                                //closeConnection(is, os);
                                DFA.state = "OnCloseResp";
                                socket.setSoTimeout(120000);
                            }
                            else {
                                sendResponse(response.get(550) + ", Closing Connection");
                                //os.writeObject(response.get(550) + ", Closing Connection");
                                DFA.state = "OnCloseResp";
                                socket.setSoTimeout(120000);
                            }
                        }
                        else sendResponse(response.get(512));
                        break;
                    case "PASS":
                        /*Handles Pass command
                         * check if password matches
                         * if matches send 220
                         * otherwise send 503
                         * let user re-try
                         * */
                        if (DFA.state.equals("ReqReceived")) {
                            String[] arguments = commandParser.parseGenericCommands(tmp);
                            if (arguments.length == 1) {
                                String pass = arguments[0];
                                if (database.getValue(userName).equals(pass)) {
                                    sendResponse(response.get(220));
                                    //os.writeObject(response.get(220));

                                    if (AdminMode) DFA.state = "ConnectedAdmin";
                                    else DFA.state = "Connected";
                                    socket.setSoTimeout(240000);
                                }
                                else{
                                    sendResponse(response.get(503));
                                    //os.writeObject(response.get(503));
                                    DFA.state = "ReqReceived";
                                }
                            }
                            else sendResponse(response.get(511));//os.writeObject(response.get(511));
                        }
                        else sendResponse(response.get(512));//os.writeObject(response.get(512));
                        break;
                    case "LIST":
                        /*To handle list command
                         * return list of users -u or channels -c
                         * */
                        if (DFA.state.equals("Connected") || DFA.state.equals("ConnectedAdmin")){
                            String[] arguments = commandParser.parseGenericCommands(tmp);

                            if (arguments.length == 1 && arguments[0].equals("-u")){
                                ArrayList<String> toSend = new ArrayList<>();
                                toSend.add(response.get(201));
                                toSend.add("\n");
                                //os.writeObject(response.get(201));
                                Main.server.getConnectedClients().forEach((u) -> {
                                    toSend.add(u);
                                    toSend.add("\n");
                                    //os.writeObject(u);
                                });
                                sendResponse(String.join("", toSend));
                                //os.writeObject(String.join("", toSend));
                            }
                            else if (arguments.length == 1 && arguments[0].equals("-c")){
                                ArrayList<String> toSend = new ArrayList<>();
                                toSend.add(response.get(201));
                                toSend.add("\n");
                                //os.writeObject(response.get(201));
                                channels.getAllChannels().forEach(c -> {
                                    toSend.add(c);
                                    toSend.add("\n");
                                    //os.writeObject(c);
                                });
                                sendResponse(String.join("", toSend));
                                //os.writeObject(String.join("", toSend));
                            }
                            else sendResponse(response.get(511));//os.writeObject(response.get(511));
                        }
                        else sendResponse(response.get(500));//os.writeObject(response.get(500));
                        break;
                    case "JOIN":
                        /*Handles join channel command
                         * check if user has right to join the channel
                         * check if channel exists (if not send 551)
                         * check if user is already joined another channel (send 530)
                         * */
                        if (DFA.state.equals("Connected") || DFA.state.equals("ConnectedAdmin")){
                            String[] arguments = commandParser.parseGenericCommands(tmp);

                            if (arguments.length == 1){
                                String channelName = arguments[0];
                                if (channels.isExists(channelName) && (activeChannel == null)){
                                    if (channels.getChannel(channelName).users.contains(userName)) {
                                        sendResponse(response.get(221) + " " + channelName);//
                                        //os.writeObject(response.get(221) + " " + channelName);
                                        activeChannel = channels.getChannel(channelName);
                                    }
                                    else{
                                        sendResponse(response.get(504));
                                        //os.writeObject(response.get(504));
                                    }
                                }
                                else if (activeChannel != null){
                                    sendResponse(response.get(530) + " " + activeChannel.name);
                                    //os.writeObject(response.get(530) + " " + activeChannel.name);
                                }
                                else sendResponse(response.get(551));//os.writeObject(response.get(551));
                            }
                            else sendResponse(response.get(511));//os.writeObject(response.get(511));
                        }
                        else sendResponse(response.get(500));//os.writeObject(response.get(500));
                        break;
                    case "MSG":
                        /*Handles msg command
                         * check if msg is sent to the another user or broadcast in the channel
                         * checks if user exists or channel is joined in case of broadcast
                         * In broadcast, check other users of the channel are connected
                         * checks if msg is correctly formatted
                         * */
                        if (DFA.state.equals("Connected") || DFA.state.equals("ConnectedAdmin")){
                            String[] arguments = commandParser.parseMsgCommands(tmp);

                            if (arguments.length == 2){
                                String sendTo = arguments[0];
                                String message = arguments[1];
                                if (database.isExist(sendTo) && (Main.server.ifClientExists(sendTo))){
                                    Main.server.getConnectedClient(sendTo).sendMessage(userName, message);
                                    sendResponse(response.get(210) + " " + sendTo);
                                    //os.writeObject(response.get(210) + " " + sendTo);
                                }
                                else sendResponse(response.get(550));//os.writeObject(response.get(550));
                            }
                            else if (arguments.length ==  1 && (activeChannel != null)){
                                activeChannel.users.forEach(u -> {
                                    if (Main.server.ifClientExists(u)){
                                        if (Main.server.getConnectedClient(u).activeChannel.equals(activeChannel)){
                                            try {
                                                if (userName.equals(u)) {
                                                    String toSend = "";
                                                    toSend += response.get(201) + "\n";
                                                    toSend += response.get(202)+ " " + u;
                                                    toSend += "\n";
                                                    toSend += arguments[0];
                                                    sendResponse(toSend);
                                                    //os.writeObject(toSend);
                                                }
                                                else Main.server.getConnectedClient(u).sendMessage(userName, arguments[0]);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                    }
                                });
                                //os.writeObject(response.get(201));
                            }
                            else if (arguments.length ==  1 && (activeChannel == null)){
                                sendResponse(response.get(552));
                                //os.writeObject(response.get(552));
                            }
                            else sendResponse(response.get(511));//os.writeObject(response.get(511));
                        }
                        else sendResponse(response.get(500));//os.writeObject(response.get(500));
                        break;
                    case "PART":
                        /*Handles Part command to disconnect from the channel
                         * checks if user is joined to the channel if not send (552)
                         * reset active channel
                         * */
                        if ((DFA.state.equals("Connected") || DFA.state.equals("ConnectedAdmin"))
                                && activeChannel != null)
                        {
                            String[] arguments = commandParser.parseGenericCommands(tmp);

                            if (arguments.length == 1){
                                if (activeChannel.name.equals(arguments[0])){
                                    sendResponse(response.get(230) + " " + arguments[0]);
                                    //os.writeObject(response.get(230) + " " + arguments[0]);
                                    activeChannel = null;
                                }
                                else sendResponse(response.get(552));//os.writeObject(response.get(552));
                            }
                            else sendResponse(response.get(511));//os.writeObject(response.get(511));
                        }
                        else if ((DFA.state.equals("Connected") || DFA.state.equals("ConnectedAdmin"))
                                && activeChannel == null) sendResponse(response.get(552));//os.writeObject(response.get(552));
                        else sendResponse(response.get(500));//os.writeObject(response.get(500));
                        break;
                    case "ADD":
                        /*Handles Add command
                         * only valid in admin mode
                         * add user to the channel
                         * check if user and channel exists
                         * */
                        if (DFA.state.equals("ConnectedAdmin")) {
                            String[] arguments = commandParser.parseGenericCommands(tmp);

                            if (arguments.length == 2){
                                String newUser = arguments[0];
                                String chName = arguments[1];

                                if (!database.isExist(newUser)) sendResponse(response.get(550));//os.writeObject(response.get(550));
                                else if (!channels.isExists(chName)) sendResponse(response.get(551));//os.writeObject(response.get(551));
                                else{
                                    if (Main.server.ifClientExists(newUser)) sendResponse(response.get(570));//os.writeObject(response.get(570));
                                    else{
                                        Channel ch = channels.getChannel(chName);
                                        if (ch.ifUserExists(newUser)) sendResponse(response.get(450));//os.writeObject(response.get(450));
                                        else{
                                            channels.getChannel(chName).addUsers(newUser);
                                            sendResponse(response.get(201));
                                            //os.writeObject(response.get(201));
                                            System.out.println(channels.getChannel(chName).users);
                                        }
                                    }
                                }
                            }
                            else sendResponse(response.get(511));//os.writeObject(response.get(511));
                        }
                        else sendResponse(response.get(500));//os.writeObject(response.get(500));
                        break;
                    case "REMOVE":
                        /*Handles remove command
                         * only valid in admin mode
                         * remove user to the channel
                         * check if user and channel exists
                         * */
                        if (DFA.state.equals("ConnectedAdmin")) {
                            String[] arguments = commandParser.parseGenericCommands(tmp);

                            if (arguments.length == 2){
                                String newUser = arguments[0];
                                String chName = arguments[1];

                                if (!database.isExist(newUser)) sendResponse(response.get(550));//os.writeObject(response.get(550));
                                else if (!channels.isExists(chName)) sendResponse(response.get(551));//os.writeObject(response.get(551));
                                else{
                                    if (Main.server.ifClientExists(newUser)) sendResponse(response.get(570));//os.writeObject(response.get(570));
                                    else{
                                        Channel ch = channels.getChannel(chName);
                                        if (!ch.ifUserExists(newUser)) sendResponse(response.get(555));//os.writeObject(response.get(555));
                                        else{
                                            channels.getChannel(chName).removeUser(newUser);
                                            sendResponse(response.get(201));
                                            //os.writeObject(response.get(201));
                                            System.out.println(channels.getChannel(chName).users);
                                        }
                                    }
                                }
                            }
                            else sendResponse(response.get(511));//os.writeObject(response.get(511));
                        }
                        else sendResponse(response.get(500));//os.writeObject(response.get(500));
                        break;
                    case "VERSION":
                        /*Handles Version command
                         * returns list of accepted versions
                         * */
                        if (DFA.state.equals("Connected") || DFA.state.equals("ConnectedAdmin")){
                            String[] arguments = commandParser.parseGenericCommands(tmp);

                            if (arguments.length == 0){
                                ArrayList<String> toSend = new ArrayList<>();
                                toSend.add(response.get(201));
                                toSend.add("\n");
                                //os.writeObject(response.get(201));
                                Server.versionNums.forEach(v -> {
                                    toSend.add(v);
                                    toSend.add("\n");
                                    //os.writeObject(v);
                                });
                                sendResponse(String.join("", toSend));
                                //os.writeObject(String.join("", toSend));
                            }
                            else sendResponse(response.get(511));//os.writeObject(response.get(511));
                        }
                        else sendResponse(response.get(500));//os.writeObject(response.get(500));
                        break;
                    case "EXIT":
                        /*Handles exit command
                         * Two way closing client first
                         * if has received any response from client, timeout to close connection*/
                        if (DFA.state.equals("Connected") || DFA.state.equals("ConnectedAdmin")) {
                            String[] arguments = commandParser.parseGenericCommands(tmp);
                            if (arguments.length == 0) {
                                sendResponse(response.get(250));
                                //os.writeObject(response.get(250));
                                DFA.state = "OnCloseResp";
                                socket.setSoTimeout(120000);
                                //done = true;
                            }
                            else sendResponse(response.get(511));//os.writeObject(response.get(511));
                        }
                        else sendResponse(response.get(500));//os.writeObject(response.get(500));
                        break;
                    case "EXITED":
                        /*ACK from the client about its closing
                         * Once ACK, server can close as well
                         * Ensuring Two-way closing*/
                        if (DFA.state.equals("OnCloseResp")) {
                            String[] arguments = commandParser.parseGenericCommands(tmp);
                            if (arguments.length == 0) {
                                DFA.state = "Closing";
                                closeConnection(is, os);
                                return;
                            }
                            else sendResponse(response.get(511));//os.writeObject(response.get(511));
                        }
                        else sendResponse(response.get(512));//os.writeObject(response.get(512));
                        break;
                    default:
                        sendResponse(response.get(511));
                        //os.writeObject(response.get(511));
                        break;
                }

                os.flush();
                /*Print what client sends*/
                if (!userName.equals("")) {
                    System.out.println("Client " + userName + " sent " + tmp);
                } else {
                    System.out.println("Client " + clientConnected + " sent " + tmp);
                }
            }

        }
        catch (SocketTimeoutException | SocketException e){
            /*Close connection smoothly in case of network error or timeout*/
            DFA.state = "Closing";
            closeConnection(is, os);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    /*Used by msg command to send message to other user*/
    public void sendMessage(String user, String message) throws IOException {
        String toSend = "";
        toSend += response.get(202)+ " " + user;
        toSend += "\n";
        toSend += message;
        this.addPendingMsg(toSend);
        //os.writeObject(toSend);
        //os.writeObject(response.get(202)+ " " + user);
        //os.writeObject(message);
    }

    public void addPendingMsg(String msg){
        String m = "---------------\n";
        m += msg;
        m += "\n---------------\n";

        this.pendingMsgs.add(m);
    }

    public void sendResponse(String msg) throws IOException {
        if (this.pendingMsgs.size() > 0){
            String totalResponse = String.join("\n", this.pendingMsgs);
            totalResponse += msg;
            os.writeObject(totalResponse);
        }
        else os.writeObject(msg);

        this.pendingMsgs.clear();
    }
}