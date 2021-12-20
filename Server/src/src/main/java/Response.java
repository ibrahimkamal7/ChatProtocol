/*CS 544
 * 06/03/2021
 * Response
 * Class representing list of response
 * helps in calling response from the key (integer value of response)
 * */
package src.main.java;

import java.util.HashMap;

public class Response {
    static HashMap<Integer, String> responses = new HashMap<>();

    public Response(){
        responses.put(201, "201 Command successful");
        responses.put(205, "205 Version Number Accepted");
        responses.put(230, "230 Exited Channel");
        responses.put(220, "220 Connection established");
        responses.put(221, "221 Joined Channel");
        responses.put(290, "290 Valid Versions");
        responses.put(301, "301 Password Required");
        responses.put(503, "510 Password Incorrect");
        responses.put(550, "550 User Not Found");
        responses.put(551, "551 Channel Not Found");
        responses.put(555, "555 User Not Authorized for this channel");
        responses.put(552, "552 Channel Not Joined");
        responses.put(250, "250 Closing Connection");
        responses.put(500, "500 Connection has not established");
        responses.put(504, "504 Not Authorized");
        responses.put(511, "511 Invalid Command");
        responses.put(512, "512 Wrong State");
        responses.put(520, "520 User already exists, Closing Connection");
        responses.put(202, "202 Message sent by");
        responses.put(210, "210 Message delivered to");
        responses.put(530, "530 Already in a channel");
        responses.put(570, "570 User Already Connected");
        responses.put(501, "501 Invalid Version Number");
        responses.put(450, "450 User Already Exists");
        responses.put(599, "599 Server Time Out, Closing");
    }

    /*get response message from the response digit number*/
    public String get(int number){
        if (responses.containsKey(number)) return responses.get(number);

        return null;
    }

}
