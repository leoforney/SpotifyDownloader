package tk.leoforney.spotifydownloader;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Dynamic Signals on 11/28/2016.
 */

public class User {
    String birthdate;
    String country;
    String display_name;
    String email;
    HashMap<String, String> external_urls;
    Follower followers;
    String href;
    String id;
    List<Image> images;
    String product;
    String type;
    String uri;

    @Override
    public String toString() {
        return id;
    }
}
