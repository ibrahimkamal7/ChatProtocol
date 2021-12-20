/*CS 544
 * 06/03/2021
 * UserDatabase
 * Class representing database of user info
 * used in check if user admin, exists, etc.
 * */
package src.main.java;

import java.util.ArrayList;
import java.util.HashMap;

public class UserDatabase {
    static HashMap<String, String> database = new HashMap<>();
    static ArrayList<String> admins = new ArrayList<>(){{add("Admin");}};
    public UserDatabase(){
        database.put("hg387", "123456");
        database.put("mtk24", "123456");
        database.put("ik363", "123456");
        database.put("Admin", "123456");
    }
    /*check if user exists*/
    public boolean isExist(String user){
        return database.containsKey(user);
    }

    /*get value of pass for a user*/
    public String getValue(String user){
        if (isExist(user)) return database.get(user);

        return null;
    }

    /*check if user is admin*/
    public Boolean isAdmin(String user){
        return admins.contains(user);
    }
}
