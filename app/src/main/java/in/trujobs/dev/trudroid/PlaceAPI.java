package in.trujobs.dev.trudroid;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import in.trujobs.dev.trudroid.Helper.PlaceAPIHelper;
import in.trujobs.dev.trudroid.Util.Tlog;
import in.trujobs.dev.trudroid.api.ServerConstants;

/**
 * Created by zero on 1/8/16.
 */
public class PlaceAPI {

    private static final String TAG = PlaceAPI.class.getSimpleName();

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";

    private static final String SERVER_API_KEY = ServerConstants.GOOGLE_SERVER_API_KEY;

    public ArrayList<PlaceAPIHelper> autocomplete (String input) {
        ArrayList<PlaceAPIHelper> resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();

        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + SERVER_API_KEY);
            sb.append("&components=country:in");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Tlog.e("Error processing Places API URL", e);
            return resultList;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Tlog.e("Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Log.d(TAG, jsonResults.toString());

            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++){
                PlaceAPIHelper placeAPIHelper = new PlaceAPIHelper();
                String description = predsJsonArray.getJSONObject(i).getString("description");
                String subLocality_level = predsJsonArray.getJSONObject(i).getJSONArray("types").getString(0);
                System.out.print(subLocality_level);
                if((description.toLowerCase().contains("bengaluru") || description.toLowerCase().contains("bangalore"))) {
                    placeAPIHelper.setPlaceId(predsJsonArray.getJSONObject(i).getString("place_id"));
                    placeAPIHelper.setDescription(predsJsonArray.getJSONObject(i).getString("description"));
                    resultList.add(placeAPIHelper);
                }
            }
        } catch (JSONException e) {
            Tlog.e("Cannot process JSON results", e);
        }
        return resultList;
    }
}