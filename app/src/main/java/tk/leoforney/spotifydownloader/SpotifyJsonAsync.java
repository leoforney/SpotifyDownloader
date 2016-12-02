package tk.leoforney.spotifydownloader;

import android.os.AsyncTask;

/**
 * Created by Dynamic Signals on 11/27/2016.
 */

public class SpotifyJsonAsync extends AsyncTask<String, Integer, String> {

    final static String TAG = SpotifyJsonAsync.class.getName();
    private String token;

    public SpotifyJsonAsync(String token) {
        this.token = token;
    }

    @Override
    protected String doInBackground(String... strings) {

        HttpGetFunction httpGetFunction = new HttpGetFunction(token);

        return httpGetFunction.getFromUrl(strings[0]);
    }
}
