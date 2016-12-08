package tk.leoforney.spotifydownloader;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.UserPrivate;

import static tk.leoforney.spotifydownloader.MainActivity.spotify;
import static tk.leoforney.spotifydownloader.MainActivity.yc;

/**
 * Created by Dynamic Signals on 12/2/2016.
 */

public class AsyncTasks {
    final static String TAG = AsyncTasks.class.getName();

    public static class getUser extends AsyncTask<Void, Void, UserPrivate> {

        @Override
        protected UserPrivate doInBackground(Void... voids) {
            return spotify.getMe();
        }
    }

    public static class getPlaylists extends AsyncTask<Void, Void, Pager<PlaylistSimple>> {

        @Override
        protected Pager<PlaylistSimple> doInBackground(Void... voids) {
            return spotify.getMyPlaylists();
        }
    }

    public static class getTracksInPlaylistAsync extends AsyncTask<String, Void, Pager<PlaylistTrack>> {

        @Override
        protected Pager<PlaylistTrack> doInBackground(String... strings) {
            return spotify.getPlaylistTracks(strings[0], strings[1]);
        }
    }

    public static class downloadSongs extends AsyncTask<PlaylistDownload, Float, Void> {

        String user, id;
        HashMap<String, Object> options;
        Pager<PlaylistTrack> trackPager;

        @Override
        protected Void doInBackground(PlaylistDownload... downloads) {
            user = downloads[0].playlist.owner.id;
            id = downloads[0].playlist.id;
            options = new HashMap<>();
            trackPager = spotify.getPlaylistTracks(user, id);
            int total = downloads[0].playlist.tracks.total;
            int amountOfHundreds = Math.round(total / 100);
            List<PlaylistTrack> allTracksInPlaylist = new ArrayList<>();

            allTracksInPlaylist.addAll(spotify.getPlaylistTracks(user, id).items);
            for (int i = 0; i < amountOfHundreds; i++) {
                Log.d(TAG, "Code Ran!");
                options.put("offset", 100*amountOfHundreds);
                allTracksInPlaylist.addAll(spotify.getPlaylistTracks(user, id, options).items);
            }
            Log.d(TAG, String.valueOf(total + " - " + allTracksInPlaylist.size()));

            List<LocalSong> allLocalSongs = new ArrayList<>();
            for (PlaylistTrack playlistTrack : allTracksInPlaylist) {
                LocalSong song = new LocalSong();
                song.track = playlistTrack;
                song.searchItems = Lists.newArrayList(Iterables.limit(yc.search(playlistTrack.track.name + playlistTrack.track.artists.get(0).name), 5));
                Log.d(TAG, song.track.track.name + ": " + song.searchItems.get(0).getTitle() + " - "
                        + "https://youtube.com/watch?v=" + song.searchItems.get(0).getId());
                song.track = playlistTrack;
                allLocalSongs.add(song);
            }

            for (int e = 0; e < allLocalSongs.size(); e++) {
                LocalSong song = allLocalSongs.get(e);
                try {
                    File downloadFile = new File(Environment.getExternalStorageDirectory() + "/Music/" + downloads[0].playlist.name);
                    URL downloadURL = new URL("https://www.youtube.com/watch?v=" + song.searchItems.get(0).getId());
                    //new VGet(downloadURL, downloadFile).download();
                } catch (Exception q) {
                    q.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Float... values) {
            super.onProgressUpdate(values);

        }
    }

}
