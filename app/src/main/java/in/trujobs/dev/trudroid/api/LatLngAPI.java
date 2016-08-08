package in.trujobs.dev.trudroid.api;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import in.trujobs.dev.trudroid.Helper.LatLngAPIHelper;
import in.trujobs.dev.trudroid.Util.Tlog;

/**
 * Created by zero on 5/8/16.
 */
public class LatLngAPI {

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api";
    private static final String TYPE_GEOCODE = "/geocode";
    private static final String OUT_JSON = "/json";

    private static final String SERVER_API_KEY = "AIzaSyBNM0b5j-qfS-foVPNviZjjSO5EXHxNdrA";

    public static LatLngAPIHelper getLatLngFor(String placeId) {
        LatLngAPIHelper result = new LatLngAPIHelper();

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();

        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_GEOCODE + OUT_JSON);
            sb.append("?key=" + SERVER_API_KEY);
            sb.append("&place_id="+URLEncoder.encode(placeId, "utf8"));

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
            return result;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Tlog.e("Error connecting to Places API", e);
            return result;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Log.d(TAG, jsonResults.toString());

            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONObject predsJsonObj = jsonObj.getJSONArray("results").getJSONObject(0);
            Tlog.i(""+predsJsonObj);
            if(jsonObj.getString("status").equalsIgnoreCase("OK")){
                // Extract the Locality lat/lng from the results
                JSONObject predsJsonLatLng = predsJsonObj.getJSONObject("geometry").getJSONObject("location");
                result.setLatitude( Double.parseDouble(predsJsonLatLng.getString("lat")));
                result.setLongitude( Double.parseDouble(predsJsonLatLng.getString("lng")));
                result.setPlaceId( predsJsonObj.getString("place_id"));
            }
        } catch (JSONException e) {
            Tlog.e("Cannot process JSON results", e);
        }
        Tlog.i("fetched lat : " + result.getLatitude());
        return result;
    }
}
