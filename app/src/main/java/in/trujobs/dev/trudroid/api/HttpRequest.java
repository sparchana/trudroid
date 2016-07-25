package in.trujobs.dev.trudroid.api;

import android.util.Base64;
import android.util.Log;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import in.trujobs.proto.LogInRequest;
import in.trujobs.proto.LogInResponse;

/**
 * Created by batcoder1 on 25/7/16.
 */
public class HttpRequest {
    public static LogInResponse loginRequest(LogInRequest logInRequest) {
        String responseString = postToServer(Config.URL_LOGIN,
                Base64.encodeToString(logInRequest.toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        LogInResponse logInResponse = null;
        try {
            logInResponse = LogInResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {
            Log.w(String.valueOf(e), "Cannot parse response");
        }
        return logInResponse;
    }

    public static String postToServer(String requestUrl, String request) {
        URL url = null;
        try {
            url = new URL(requestUrl);
        } catch (MalformedURLException e) {
            Log.e(String.valueOf(e), "URL is malformed " + requestUrl);
            return "";
        }

        InputStream is = null;

        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);

            conn.setRequestProperty("Content-Type", "text/plain");

            byte[] outputInBytes = request.getBytes("UTF-8");
            OutputStream os = conn.getOutputStream();
            os.write(outputInBytes);
            os.close();
            conn.connect();
            int response = conn.getResponseCode();
            is = conn.getInputStream();

            String contentAsString = readIt(is);
            return contentAsString;
        } catch (ProtocolException e) {
            Log.e(String.valueOf(e), "Exception");
        } catch (IOException e) {
            Log.e(String.valueOf(e), "Exception");
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                Log.e(String.valueOf(e), "Exception");
            }
        }
        return "";
    }

    public static String getFromServer(String requestUrl) {
        URL url = null;
        try {
            url = new URL(requestUrl);
        } catch (MalformedURLException e) {
            Log.e(String.valueOf(e), "URL is malformed " + requestUrl);
            return "";
        }

        InputStream is = null;

        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            conn.setRequestProperty("Content-Type", "application/json");
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is);
            return contentAsString;

        } catch (ProtocolException e) {
            Log.e(String.valueOf(e), "Exception");
        } catch (IOException e) {
            Log.e(String.valueOf(e), "Exception");
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                Log.e(String.valueOf(e), "Exception");
            }
        }
        return "";
    }

    public static String readIt(InputStream stream)
            throws IOException, UnsupportedEncodingException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line = null;

        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }

        return sb.toString();
    }
}
