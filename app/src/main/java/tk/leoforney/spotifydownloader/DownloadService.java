package tk.leoforney.spotifydownloader;

import android.app.DownloadManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class DownloadService extends Service {
    PlaylistDownloadRequest playlist;
    Context context;

    YoutubeConnector yc;
    final static String TAG = DownloadService.class.getName();

    public DownloadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {

        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    List<Song> allSongs;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        Bundle b = intent.getExtras();
        playlist = new Gson().fromJson(intent.getStringExtra("playlist"), PlaylistDownloadRequest.class);
        if (MainActivity.requestList != null) {
            playlist = MainActivity.requestList.get(intent.getIntExtra("index", 0));
        } else {
            playlist = new PlaylistDownloadRequest();
        }

        allSongs = playlist.playlist.songs;

        new InitializeYoutube().execute();

        new DownloadFromYoutube().execute();

        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {

    }

    class DownloadFromYoutube extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                for (int i = 0; i < allSongs.size(); i++) {
                    Log.d(TAG, String.valueOf(allSongs.size()));
                    Double percentage = ((i+1) / (double) allSongs.size()) * 100;
                    Log.d(TAG, String.valueOf(percentage));
                    playlist.status = ((float) i + 1 / allSongs.size()) * 100; // percentage value for progress on the circular bar

                    Song currentSong = allSongs.get(i);
                    List<VideoItem> searchResults = null;

                    searchResults = yc.search(currentSong.name + " " + currentSong.artists.get(0));
                    VideoItem topVideo = searchResults.get(0);
                    String URL = "https://youtube.com/watch?v=" + topVideo.getId();

                    StringBuilder response = new StringBuilder();

                    String baseUrl = "https://www.youtubeinmp3.com/fetch/?format=JSON&video=" + URL;
                    Log.d(TAG, topVideo.getTitle() + ":" + baseUrl);
                    /*
                    URL obj = new URL(baseUrl);
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                    con.setRequestMethod("POST");

                    int responseCode = con.getResponseCode();
                    Log.d(TAG, "\nSending 'GET' request to URL : " + baseUrl);
                    Log.d(TAG, "Response Code : " + responseCode);

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(con.getInputStream()));
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    JSONObject jsonObject = new JSONObject(response.toString());
                    String downloadLink = jsonObject.getString("link");
                    Log.d(TAG, String.valueOf((i+1) + "/" + allSongs.size() + ": " + currentSong.name + ", URL: " + downloadLink));

                    File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);

                    File file = new File(path + File.separator +
                            playlist.playlist.name + File.separator
                            + currentSong.name.replace("/", "-") + ".mp3");

                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                        //imageUri is a valid Uri
                        DownloadManager.Request downloadRequest = new DownloadManager.Request(Uri.parse(downloadLink));
                        //without this line, it works
                        downloadRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
                        //subpath is valid
                        Log.d(TAG, file.getPath());
                        downloadRequest.setDestinationInExternalPublicDir(null, file.getPath());
                        downloadManager.enqueue(downloadRequest);

                    } else {
                        Log.d(TAG, "Folder/file exists!");
                    }
                    */
                    //MainActivity.rvAdapter.updateRequestList(MainActivity.requestList);

                }

                playlist.status = 100f;
                playlist.completed = true;

                stopSelf();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    class InitializeYoutube extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            yc = new YoutubeConnector(getBaseContext());
            return null;
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Destroying service");
    }

    public static void writeString(File file, String contents) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(contents);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null)
                    writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getText(String url) throws Exception {
        URL website = new URL(url);
        URLConnection connection = website.openConnection();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        connection.getInputStream()));

        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null)
            response.append(inputLine);

        in.close();

        return response.toString();
    }

}
