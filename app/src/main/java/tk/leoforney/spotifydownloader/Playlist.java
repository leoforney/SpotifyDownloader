package tk.leoforney.spotifydownloader;

import java.util.List;

/**
 * Created by Dynamic Signals on 11/28/2016.
 */

public class Playlist {

    public Playlist(String id, String name, User owner) {
        this.id = id;
        this.name = name;
        this.owner = owner;
    }

    public Playlist() {}

    List<Song> songs;
    String id;
    User owner;
    String name;


    @Override
    public String toString() {
        return "Name: " + name + ", ID: " + id + ", Owner: " + owner.toString();
    }
}
