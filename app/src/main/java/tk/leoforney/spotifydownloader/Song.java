package tk.leoforney.spotifydownloader;

import java.util.List;

/**
 * Created by Dynamic Signals on 11/28/2016.
 */

public class Song {
    List<Artist> artists;
    String name;
    String album;

    public Song(List<Artist> artists, String name, String album) {
        this.artists = artists;
        this.name = name;
        this.album = album;
    }

    @Override
    public String toString() {
        return "Name: " + name + ", Album: " + album + ", Artists: " + artists.toString();
    }
}
