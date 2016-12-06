package in.trujobs.dev.trudroid.Util;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.protobuf.InvalidProtocolBufferException;

import java.sql.Timestamp;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import in.trujobs.dev.trudroid.api.HttpRequest;
import in.trujobs.proto.JobRoleResponse;

/**
 * Created by batcoder1 on 25/7/16.
 */
public class Util {
    public static boolean isLoggedIn() {
        boolean loginStatus = false;

        if(Prefs.leadId.get() != 0L){
            loginStatus = true;
        }
        return loginStatus;
    }

    public static int isValidName(String name) {
        if (name.trim().length() == 0){
            return 0;
        } else if(!name.matches("^[ A-z]+$")){
            return 1;
        } else {
            return 2;
        }
    }

    public static boolean isValidEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return false;
        }

        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isValidPassword(String pass) {
        if (pass != null && pass.length() > 3) {
            return true;
        }
        return false;
    }

    public static boolean isValidMobile(String mobile) {
        if (mobile.length() != 10)
            return false;
        char first_digit = mobile.charAt(0);
        if ((first_digit < '7') || (first_digit > '9')) {
            return false;
        } else {
            return true;
        }
    }

    public static JobRoleResponse getJobRoleResponse(Activity context) {
        JobRoleResponse jobRoleResponse = null;
        jobRoleResponse = HttpRequest.getJobRoles();
        return jobRoleResponse;
    }

    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static String getMonth(int month) {
        String returnMonth = "";
        switch (month){
            case 1: returnMonth = "Jan"; break;
            case 2: returnMonth = "Feb"; break;
            case 3: returnMonth = "Mar"; break;
            case 4: returnMonth = "Apr"; break;
            case 5: returnMonth = "May"; break;
            case 6: returnMonth = "Jun"; break;
            case 7: returnMonth = "Jul"; break;
            case 8: returnMonth = "Aug"; break;
            case 9: returnMonth = "Sep"; break;
            case 0: returnMonth = "Oct"; break;
            case 11: returnMonth = "Nov"; break;
            case 12: returnMonth = "Dec"; break;
        }
        return returnMonth;
    }

    public static String getDay(int date) {
        String returnDay = "";
        switch (date){
            case 1: returnDay = "Sun"; break;
            case 2: returnDay = "Mon"; break;
            case 3: returnDay = "Tue"; break;
            case 4: returnDay = "Wed"; break;
            case 5: returnDay = "Thu"; break;
            case 6: returnDay = "Fri"; break;
            case 7: returnDay = "Sat"; break;
        }
        return returnDay;
    }

}