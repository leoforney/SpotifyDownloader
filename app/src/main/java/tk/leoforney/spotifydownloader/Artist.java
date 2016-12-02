package tk.leoforney.spotifydownloader;

import java.util.HashMap;

/**
 * Created by Dynamic Signals on 11/28/2016.
 */

public class Artist {
    HashMap<String, String> external_urls;
    String href;
    String id;
    String name;
    String type;
    String uri;

    @Override
    public String toString() {
        return name;
    }
}
