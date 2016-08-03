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

import in.trujobs.dev.trudroid.Util.Prefs;
import in.trujobs.proto.HomeLocalityRequest;
import in.trujobs.proto.HomeLocalityResponse;
import in.trujobs.proto.JobPostResponse;
import in.trujobs.proto.JobRoleResponse;
import in.trujobs.proto.LogInRequest;
import in.trujobs.proto.LogInResponse;
import in.trujobs.proto.ResetPasswordRequest;
import in.trujobs.proto.ResetPasswordResponse;
import in.trujobs.proto.SignUpRequest;
import in.trujobs.proto.SignUpResponse;

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

    public static SignUpResponse signUpRequest(SignUpRequest signUpRequest) {
        String responseString = postToServer(Config.URL_SIGN_UP,
                Base64.encodeToString(signUpRequest.toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        SignUpResponse signUpResponse = null;
        try {
            signUpResponse = SignUpResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {
            Log.w(String.valueOf(e), "Cannot parse response");
        }
        return signUpResponse;
    }

    public static LogInResponse addPassword(LogInRequest logInRequest) {
        String responseString = postToServer(Config.URL_ADD_PASSWORD,
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

    public static ResetPasswordResponse findUserAndSendOtp(ResetPasswordRequest resetPasswordRequest) {
        String responseString = postToServer(Config.URL_FORGOT_PASSWORD_SEND_OTP,
                Base64.encodeToString(resetPasswordRequest.toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        ResetPasswordResponse resetPasswordResponse = null;
        try {
            resetPasswordResponse = ResetPasswordResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {
            Log.w(String.valueOf(e), "Cannot parse response");
        }
        return resetPasswordResponse;
    }

    public static JobRoleResponse getJobRoles() {
        JobRoleResponse.Builder requestBuilder =
                JobRoleResponse.newBuilder();

        String responseString = postToServer(Config.URL_ALL_JOB_ROLES,
                Base64.encodeToString(requestBuilder.build().toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        JobRoleResponse jobRoleResponse = null;
        try {
            jobRoleResponse =
                    jobRoleResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {}

        if (jobRoleResponse != null && jobRoleResponse.getJobRoleCount() != 0) {
            return jobRoleResponse;
        } else {
            return null;
        }
    }

    public static JobPostResponse getJobPosts() {
        JobPostResponse.Builder requestBuilder =
                JobPostResponse.newBuilder();

        String responseString = postToServer(Config.URL_MATCHING_JOB_POSTS + "/"+ Prefs.candidateMobile.get(),
                Base64.encodeToString(requestBuilder.build().toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        JobPostResponse jobPostResponse = null;
        try {
            jobPostResponse =
                    jobPostResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {}

        if (jobPostResponse != null && jobPostResponse.getJobPostCount() > 0) {
            return jobPostResponse;
        } else {
            return null;
        }
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

    public static HomeLocalityResponse addHomeLocality(HomeLocalityRequest homeLocalityRequest) {
        String responseString = postToServer(Config.URL_ADD_HOMELOCALITY,
                Base64.encodeToString(homeLocalityRequest.toByteArray(), Base64.DEFAULT));

        byte[] responseByteArray = Base64.decode(responseString, Base64.DEFAULT);
        if (responseByteArray == null) {
            return null;
        }
        HomeLocalityResponse homeLocalityResponse = null;
        try {
            homeLocalityResponse = HomeLocalityResponse.parseFrom(responseByteArray);
        } catch (InvalidProtocolBufferException e) {
            Log.w(String.valueOf(e), "Cannot parse response");
        }
        return homeLocalityResponse;
    }
}
