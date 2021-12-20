/*CS 544
 * 06/03/2021
 * Channel
 * Class representing channel containing list of authorized users
 * helps in manipulate properties on the users
 * */
package src.main.java;

import java.util.ArrayList;
import java.util.List;

public class Channel {
    String name;

    ArrayList<String> users = new ArrayList<>();
    public Channel(String name){
        this.name = name;
    }
    public Channel(String name, ArrayList<String> users){
        this.users = users;
        this.name = name;
    }

    /*add user to the channel*/
    public void addUsers(String user) {
        users.add(user);
    }

    /*get list of users*/
    public List<String> getUsers() {
        return users;
    }

    /*check if user exists*/
    public boolean ifUserExists(String user){
        return users.contains(user);
    }

    /*remove user from the channel*/
    public void removeUser(String user){
        if (ifUserExists(user)) users.remove(user);
    }

    /*check if two channels are equal*/
    @Override
    public boolean equals(Object o){
        if (!(o instanceof Channel)) {
            return false;
        }

        Channel c = (Channel) o;

        return this.name.equals(c.name);
    }
}

