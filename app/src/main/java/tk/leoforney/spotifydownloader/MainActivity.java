package tk.leoforney.spotifydownloader;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.UserPrivate;

import static com.spotify.sdk.android.authentication.LoginActivity.REQUEST_CODE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    final static String TAG = MainActivity.class.getName();

    // Request code will be used to verify if result comes from the login activity. Can be set to any integer.
    private static final String REDIRECT_URI = "downloader://callback";
    String CLIENT_ID, token;
    Spinner playlistSpinner;
    CoordinatorLayout coordinatorLayout;
    RecyclerView recyclerView;
    static RVAdapter rvAdapter;
    static Pager<PlaylistSimple> allPlaylists;
    static YoutubeConnector yc;
    static SpotifyService spotify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.download).setOnClickListener(this);
        playlistSpinner = (Spinner) findViewById(R.id.playlistSpinner);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.snackbarPosition);

        recyclerView = (RecyclerView) findViewById(R.id.progressRecyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(llm);
        rvAdapter = new RVAdapter();

        recyclerView.setAdapter(rvAdapter);

        CLIENT_ID = getResources().getString(R.string.spotify_client_id);

        authenticateSpotify();
        requestPermission();


    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_bar_menu, menu);
        return true;
    }

    public void authenticateSpotify() {
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reauthenticateButton:
                authenticateSpotify();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.download:
                PlaylistSimple selectedPlaylist = null;
                for (PlaylistSimple iteratedPlaylist : allPlaylists.items)
                    if (playlistSpinner.getSelectedItem().equals(iteratedPlaylist.name))
                        selectedPlaylist = iteratedPlaylist;

                // Database.json contains all of the downloaded songs and te relationship to a spotifytrack and youtubeitem
                File playlistPath = new File(Environment.getExternalStorageDirectory() + "/Music/" + selectedPlaylist.name + "/Database.json");
                if (!playlistPath.exists()) {
                    Snackbar.make(coordinatorLayout, "Downloading " + selectedPlaylist.name, Snackbar.LENGTH_SHORT).show();
                    playlistPath.getParentFile().mkdir();
                    PlaylistDownload downloadRequest = new PlaylistDownload();
                    downloadRequest.playlist = selectedPlaylist;
                    yc = new YoutubeConnector(this);
                    new AsyncTasks.downloadSongs().execute(downloadRequest);
                } else {
                    int downloadedLength = playlistPath.listFiles().length;
                    Log.d(TAG, String.valueOf(downloadedLength + "-" + selectedPlaylist.tracks.total));
                    if (selectedPlaylist.tracks.total != downloadedLength) {
                        Snackbar.make(coordinatorLayout, "Playlist partially downloaded, updating now", Snackbar.LENGTH_SHORT).show();
                    } else {
                        Log.d(TAG, String.valueOf(downloadedLength));
                        Snackbar.make(coordinatorLayout, "Playlist already downloaded!", Snackbar.LENGTH_SHORT).show();
                    }
                }

                break;
        }

    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 786);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                try {
                    token = response.getAccessToken();

                    SpotifyApi api = new SpotifyApi();
                    api.setAccessToken(token);
                    spotify = api.getService();
                    UserPrivate me = new AsyncTasks.getUser().execute().get();

                    Snackbar.make(coordinatorLayout, "Hello " + me.id + "!", Snackbar.LENGTH_SHORT).show();
                    Log.d(TAG, token);

                    updateSpinner();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void updateRV(List<PlaylistDownload> downloads) {}
    public static void setRV(PlaylistDownload download, int position) {}


    public void updateSpinner() throws Exception {
        allPlaylists = new AsyncTasks.getPlaylists().execute().get();
        List<String> names = new ArrayList<>();
        for (PlaylistSimple playlist : allPlaylists.items) {
            names.add(playlist.name);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, names);
        playlistSpinner.setAdapter(adapter);
    }
}

