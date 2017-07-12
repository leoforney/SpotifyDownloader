package tk.leoforney.spotifydownloader;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.UserPrivate;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static tk.leoforney.spotifydownloader.MainActivity.rvAdapter;
import static tk.leoforney.spotifydownloader.MainActivity.spotify;

/**
 * Created by Leo on 12/2/2016.
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

    public static class downloadSongs extends AsyncTask<List<Object>, PlaylistDownload, Void> {

        String user, id;
        HashMap<String, Object> options;
        Pager<PlaylistTrack> trackPager;
        int index;
        Gson gson;

        @Override
        protected Void doInBackground(List<Object>... downloads) {
            PlaylistDownload downloadRequest = (PlaylistDownload) downloads[0].get(0);
            Context context = (Context) downloads[0].get(1);
            index = (Integer) downloads[0].get(2);
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getCanonicalName());

            YoutubeConnector yc = new YoutubeConnector(context);
            gson = new Gson();
            user = downloadRequest.playlist.owner.id;
            id = downloadRequest.playlist.id;
            options = new HashMap<>();
            trackPager = spotify.getPlaylistTracks(user, id);
            int total = downloadRequest.playlist.tracks.total;
            int amountOfHundreds = Math.round(total / 100);
            List<PlaylistTrack> allTracksInPlaylist = new ArrayList<>();
            allTracksInPlaylist.addAll(spotify.getPlaylistTracks(user, id).items);
            for (int i = 0; i < amountOfHundreds; i++) {
                options.put("offset", 100 * amountOfHundreds);
                allTracksInPlaylist.addAll(spotify.getPlaylistTracks(user, id, options).items);
            }
            Log.d(TAG, String.valueOf(total + " - " + allTracksInPlaylist.size()));
            File playlistDirectory = new File(Environment.getExternalStorageDirectory() +
                    File.separator +
                    "Music" +
                    File.separator +
                    downloadRequest.playlist.name +
                    File.separator);

            List<LocalSong> allLocalSongs = new ArrayList<>();
            wl.acquire(total*30*1000L);
            Log.d(TAG, "WakeLock: " + wl.isHeld());
            for (PlaylistTrack playlistTrack : allTracksInPlaylist) {
                LocalSong song = new LocalSong();
                song.track = playlistTrack;
                File downloadFile = new File(playlistDirectory + File.separator +
                        song.track.track.artists.get(0).name + " - " + song.track.track.name.replace("/", "") + ".mp3");
                song.downloadLocaiton = downloadFile;
                if (!song.downloadLocaiton.exists()) {
                    song.searchItems = Lists.newArrayList(Iterables.limit(yc.search(playlistTrack.track.name + "" + playlistTrack.track.artists.get(0).name), 5));
                    if (song.searchItems.size() == 0) {
                        song.searchItems = Lists.newArrayList(Iterables.limit(yc.search(playlistTrack.track.name + "" + playlistTrack.track.artists.get(0).name), 5));
                        if (song.searchItems.size() == 0) {
                            Log.d(TAG, "Could not find " + song.track.track.name + " on youtube");
                        }
                    }

                    if (song.searchItems.size() != 0 && !song.searchItems.get(0).getTitle().toLowerCase().contains("cover")) {
                        Log.d(TAG, song.track.track.name + " - "
                                + "https://youtube.com/watch?v=" + song.searchItems.get(0).getId());
                        String videoDownloadURL = "https://www.youtube.com/watch?v=" + song.searchItems.get(0).getId();
                        String totalDownloadURL = "http://www.youtubeinmp3.com/fetch/?video=" + videoDownloadURL;

                        OkHttpClient client = new OkHttpClient();

                        int attempts = 0;

                        while (Thread.currentThread().isAlive()) {

                            boolean downloaded = downloadSong(client, song, downloadFile, totalDownloadURL);
                            attempts++;
                            if (downloaded) {
                                break;
                            }
                            if (attempts >= 10){
                                song.correctlyDownloaded = false;
                                break;
                            }
                            Log.d(TAG, "Attempt " + attempts);
                        }

                    }
                } else {
                    Log.d(TAG, song.track.track.name + " is already downloaded!");
                }

                float current = (allTracksInPlaylist.indexOf(playlistTrack) + 1);
                Log.d(TAG, "Current: " + current + ", total: " + total + ", dec " + (current / total));
                downloadRequest.status = (current/total) * 100;
                publishProgress(downloadRequest);

                song.track = playlistTrack;
                if (song.correctlyDownloaded) allLocalSongs.add(song);

                try {
                    Files.write(gson.toJson(allLocalSongs).getBytes(), new File(playlistDirectory + File.separator + "Database.json"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (wl.isHeld()) {
                wl.release();
            }

            Log.d(TAG, "WakeLock: " + wl.isHeld());
            return null;
        }

        private boolean downloadSong(OkHttpClient client, LocalSong song, File downloadFile, String totalDownloadURL) {
            song.correctlyDownloaded = false;
            try {
                Request request = new Request.Builder().url(totalDownloadURL).build();
                Response response = client.newCall(request).execute();

                byte[] bytes = response.body().bytes();

                String fileContent = new String(bytes);
                if (fileContent.contains("meta")) {
                    Log.d(TAG, "Could not download song from youtube2mp3, retrying...");
                    song.correctlyDownloaded = false;
                } else {
                    Log.d(TAG, "Properly downloaded song!");
                    song.correctlyDownloaded = true;
                    Files.write(bytes, downloadFile);
                }
                if (client.cache() != null) {
                    client.cache().evictAll();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return song.correctlyDownloaded;
        }

        @Override
        protected void onProgressUpdate(PlaylistDownload... download) {

            for (PlaylistDownload iteratedDownload : rvAdapter.downloads) {
                if (iteratedDownload.playlist.name.equals(download[0].playlist.name)) {
                    iteratedDownload.status = download[0].status;
                }
            }

            rvAdapter.notifyDataSetChanged();
            super.onProgressUpdate(download);
        }
    }

}
