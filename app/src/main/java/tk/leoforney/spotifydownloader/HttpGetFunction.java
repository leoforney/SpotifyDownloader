package tk.leoforney.spotifydownloader;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Dynamic Signals on 11/27/2016.
 */

public class HttpGetFunction {

    private final static String TAG = HttpGetFunction.class.getName();

    private String token;
    public HttpGetFunction(String token) {
        this.token = token;
    }

    public String getFromUrl(String baseUrl) {
        StringBuilder response = new StringBuilder();

        try {

            URL obj = new URL(baseUrl);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            con.addRequestProperty("Authorization", " Bearer " + token);

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

            //print result
            Log.d(TAG, response.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return response.toString();
    }

}
