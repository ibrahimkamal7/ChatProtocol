/*CS 544
 * 06/03/2021
 * Channels
 * Class representing list of channel
 * used in manipulating properties of the channels
 * */
package src.main.java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

public class Channels {
    private static HashMap<String, Channel> database = new HashMap<>();
    Channels() {
        /*CONCURRENT
        * Multiple users
        * */
        database.put("Channel1", new Channel("Channel1", new ArrayList<String>(Arrays.asList("hg387", "mtk24", "Admin"))));
        database.put("Channel2", new Channel("Channel2", new ArrayList<String>(Arrays.asList("hg387", "ik363", "Admin"))));
        database.put("Channel3", new Channel("Channel3", new ArrayList<String>(Arrays.asList("hg387", "Admin"))));
    }

    /*get list of all channels*/
    public Set<String> getAllChannels(){
        return database.keySet();
    }

    /*check if channel exists*/
    public Boolean isExists(String name){return database.containsKey(name);}

    /*get channel ref for a channel*/
    public Channel getChannel(String name){ return database.get(name);}
}