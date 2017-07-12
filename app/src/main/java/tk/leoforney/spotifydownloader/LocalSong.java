package tk.leoforney.spotifydownloader;

import java.io.File;
import java.net.URL;
import java.util.List;

import kaaes.spotify.webapi.android.models.PlaylistTrack;

/**
 * Created by Dynamic Signals on 12/3/2016.
 */

public class LocalSong {
    public transient List<VideoItem> searchItems;
    public PlaylistTrack track;
    public File downloadLocaiton;
    public transient String fileContent;
    public transient boolean correctlyDownloaded;
    public transient String downloadURL;
}
