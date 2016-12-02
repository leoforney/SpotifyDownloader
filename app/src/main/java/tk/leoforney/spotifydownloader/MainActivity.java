package tk.leoforney.spotifydownloader;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.UiThread;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

import static com.spotify.sdk.android.authentication.LoginActivity.REQUEST_CODE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Request code will be used to verify if result comes from the login activity. Can be set to any integer.
    private static final String REDIRECT_URI = "downloader://callback";
    String CLIENT_ID;
    private String token;
    final static String TAG = MainActivity.class.getName();
    Spinner playlistSpinner;
    List<Playlist> allUserPlaylists = new ArrayList<>();
    CoordinatorLayout coordinatorLayout;
    RecyclerView recyclerView;
    YoutubeConnector yc;
    private static final int REQUEST_WRITE_PERMISSION = 786;
    public static List<PlaylistDownloadRequest> requestList;
    static RVAdapter rvAdapter;

    User user;

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

        requestList = new ArrayList<>();

        Log.d(TAG, requestList.toString());
        rvAdapter = new RVAdapter(requestList);

        recyclerView.setAdapter(rvAdapter);

        CLIENT_ID = getResources().getString(R.string.spotify_client_id);

        authenicateSpotify();

        requestPermission();

        SpotifyApi api = new SpotifyApi();

        // Most (but not all) of the Spotify Web API endpoints require authorisation.
        // If you know you'll only use the ones that don't require authorisation you can skip this step
        api.setAccessToken(token);

        SpotifyService spotify = api.getService();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_bar_menu, menu);
        return true;
    }

    public void authenicateSpotify() {
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
                authenicateSpotify();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.download:
                final String currentPlaylistName = playlistSpinner.getSelectedItem().toString();
                boolean continueRunning = true;
                for (PlaylistDownloadRequest downloadRequest : requestList) {
                    if (currentPlaylistName.equals(downloadRequest.playlist.name))
                        continueRunning = false;
                }
                if (continueRunning) {
                    Snackbar.make(coordinatorLayout, "Downloading " + currentPlaylistName, Snackbar.LENGTH_SHORT).show();
                    Playlist selectedPlaylist = null;
                    for (Playlist currentPlaylist : allUserPlaylists) {
                        if (currentPlaylist.name.equals(currentPlaylistName)) {
                            selectedPlaylist = currentPlaylist;
                        }
                    }
                    selectedPlaylist.songs = listAllSongsInPlaylist(selectedPlaylist);
                    PlaylistDownloadRequest request = new PlaylistDownloadRequest();
                    request.playlist = selectedPlaylist;
                    request.status = 1f;
                    requestList.add(request);
                    Integer index = 0; // Initializer
                    for (int e = 0; e < requestList.size(); e++) {
                        if (request.playlist.name.equals(requestList.get(e).playlist.name))
                            index = e;
                    }
                    rvAdapter.notifyDataSetChanged();
                    Log.d(TAG, "User selected playlist " + selectedPlaylist.toString());
                    Intent launchServiceIntent = new Intent(getApplicationContext(), DownloadService.class);
                    launchServiceIntent.putExtra("index", index);
                    startService(launchServiceIntent);
                } else {
                    Snackbar.make(coordinatorLayout, "You already requested this playlist!", Snackbar.LENGTH_SHORT).show();
                }

                break;
        }

    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
        } else {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        final int RequestCode = requestCode;
        final int ResultCode = resultCode;
        final Intent Intent = intent;

        new Thread(new Runnable() {
            @Override
            public void run() {
                // Check if result comes from the correct activity
                if (RequestCode == REQUEST_CODE) {
                    AuthenticationResponse response = AuthenticationClient.getResponse(ResultCode, Intent);
                    if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                        token = response.getAccessToken();
                        user = getUser();
                        Snackbar.make(coordinatorLayout, "Hello " + user.id + "!", Snackbar.LENGTH_SHORT).show();
                        Log.d(TAG, token);
                    }
                }
            }
        }).run();
    }

    public User getUser() {
        try {
            SpotifyJsonAsync spotifyJsonAsync = new SpotifyJsonAsync(token);
            String UserJson = spotifyJsonAsync.execute("https://api.spotify.com/v1/me").get();
            Gson gson = new Gson();
            User spotifyUser = gson.fromJson(UserJson, User.class);
            updateSpinner(spotifyUser);
            return spotifyUser;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateSpinner(User user) {
        try {
            String UserPlaylistsJson = new SpotifyJsonAsync(token).execute("https://api.spotify.com/v1/users/" + user.id + "/playlists").get().replaceAll("public", "Public");
            JSONArray allPlaylistsArray = new JSONObject(UserPlaylistsJson).getJSONArray("items");
            List<String> playlistNames = new ArrayList<>();
            for (int i = 0; i < allPlaylistsArray.length(); i++) {
                JSONObject currentObject = allPlaylistsArray.getJSONObject(i);
                String id = currentObject.getString("id");
                String name = currentObject.getString("name");
                JSONObject ownerObject = currentObject.getJSONObject("owner");
                Gson gson = new Gson();
                Playlist currentPlaylist = new Playlist(id, name, gson.fromJson(ownerObject.toString(), User.class));
                Log.d(TAG, currentPlaylist.toString());
                allUserPlaylists.add(currentPlaylist);
                playlistNames.add(name);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, playlistNames);
            playlistSpinner.setAdapter(adapter);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Song> listAllSongsInPlaylist(Playlist playlist) {
        try {
            String spotifyPlaylistJson = new SpotifyJsonAsync(token).execute("https://api.spotify.com/v1/users/" + playlist.owner.id + "/playlists/" + playlist.id + "/tracks").get();
            JSONArray itemsArray = new JSONObject(spotifyPlaylistJson).getJSONArray("items");

            List<Song> allSongs = new ArrayList<>();

            for (int e = 0; e < itemsArray.length(); e++) {
                JSONObject trackObject = itemsArray.getJSONObject(e).getJSONObject("track");
                String songName = trackObject.getString("name");
                String artistsListObjectJson = trackObject.getJSONArray("artists").toString();
                Gson gson = new Gson();
                List<Artist> artistList = gson.fromJson(artistsListObjectJson, new TypeToken<List<Artist>>() {
                }.getType());
                String albumName = trackObject.getJSONObject("album").getString("name");
                allSongs.add(new Song(artistList, songName, albumName));
            }

            return allSongs;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}

